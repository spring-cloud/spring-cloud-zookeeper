/*
 * Copyright 2015-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.zookeeper.discovery.configclient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServerInstanceProvider;
import org.springframework.cloud.zookeeper.CuratorFactory;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.cloud.zookeeper.discovery.ConditionalOnZookeeperDiscoveryEnabled;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.cloud.zookeeper.support.DefaultServiceDiscoveryCustomizer;
import org.springframework.cloud.zookeeper.support.ServiceDiscoveryCustomizer;
import org.springframework.util.ClassUtils;

public class ZookeeperConfigServerBootstrapper implements BootstrapRegistryInitializer {

	private static boolean isEnabled(Binder binder) {
		return binder.bind(ConfigClientProperties.CONFIG_DISCOVERY_ENABLED, Boolean.class).orElse(false) &&
				binder.bind(ConditionalOnZookeeperDiscoveryEnabled.PROPERTY, Boolean.class).orElse(true) &&
				binder.bind("spring.cloud.discovery.enabled", Boolean.class).orElse(true);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initialize(BootstrapRegistry registry) {
		if (!ClassUtils.isPresent("org.springframework.cloud.config.client.ConfigServerInstanceProvider", null) ||
				// don't run if bootstrap enabled, how to check the property?
				ClassUtils.isPresent("org.springframework.cloud.bootstrap.marker.Marker", null)) {
			return;
		}
		// create curator
		CuratorFactory.registerCurator(registry, null, true, bootstrapContext -> isEnabled(bootstrapContext.get(Binder.class)));

		// create discovery
		registry.registerIfAbsent(ZookeeperDiscoveryProperties.class, context -> {
			Binder binder = context.get(Binder.class);
			if (!isEnabled(binder)) {
				return null;
			}
			return binder.bind(ZookeeperDiscoveryProperties.PREFIX, Bindable
							.of(ZookeeperDiscoveryProperties.class), getBindHandler(context))
					.orElseGet(() -> new ZookeeperDiscoveryProperties(new InetUtils(new InetUtilsProperties())));
		});
		registry.registerIfAbsent(InstanceSerializer.class, context -> {
			if (!isEnabled(context.get(Binder.class))) {
				return null;
			}
			return new JsonInstanceSerializer<>(ZookeeperInstance.class);
		});
		registry.registerIfAbsent(ServiceDiscoveryCustomizer.class, context -> {
			if (!isEnabled(context.get(Binder.class))) {
				return null;
			}
			CuratorFramework curator = context.get(CuratorFramework.class);
			ZookeeperDiscoveryProperties properties = context.get(ZookeeperDiscoveryProperties.class);
			InstanceSerializer<ZookeeperInstance> serializer = context.get(InstanceSerializer.class);
			return new DefaultServiceDiscoveryCustomizer(curator, properties, serializer);
		});
		registry.registerIfAbsent(ServiceDiscovery.class, context -> {
			if (!isEnabled(context.get(Binder.class))) {
				return null;
			}
			ServiceDiscoveryCustomizer customizer = context.get(ServiceDiscoveryCustomizer.class);
			return customizer.customize(ServiceDiscoveryBuilder.builder(ZookeeperInstance.class));
		});
		registry.registerIfAbsent(ZookeeperDiscoveryClient.class, context -> {
			Binder binder = context.get(Binder.class);
			if (!isEnabled(binder)) {
				return null;
			}
			ServiceDiscovery<ZookeeperInstance> serviceDiscovery = context.get(ServiceDiscovery.class);
			ZookeeperDependencies dependencies = binder.bind(ZookeeperDependencies.PREFIX, Bindable
							.of(ZookeeperDependencies.class), getBindHandler(context))
					.orElseGet(ZookeeperDependencies::new);
			ZookeeperDiscoveryProperties discoveryProperties = context.get(ZookeeperDiscoveryProperties.class);

			return new ZookeeperDiscoveryClient(serviceDiscovery, dependencies, discoveryProperties);
		});

		// create instance provider
		// We need to pass the lambda here so we do not create a new instance of ConfigServerInstanceProvider.Function
		// which would result in a ClassNotFoundException when Spring Cloud Config is not on the classpath
		registry.registerIfAbsent(ConfigServerInstanceProvider.Function.class, ZookeeperFunction::create);

		// promote beans to context
		registry.addCloseListener(event -> {
			ZookeeperDiscoveryClient discoveryClient = event.getBootstrapContext().get(ZookeeperDiscoveryClient.class);
			if (discoveryClient != null) {
				event.getApplicationContext().getBeanFactory().registerSingleton("zookeeperServiceDiscovery",
						discoveryClient);
			}
		});
	}

	private BindHandler getBindHandler(org.springframework.boot.BootstrapContext context) {
		return context.getOrElse(BindHandler.class, null);
	}

	/*
	 * This Function is executed when loading config data.  Because of this we cannot rely on the
	 * BootstrapContext because Boot has not finished loading all the configuration data so if we
	 * ask the BootstrapContext for configuration data it will not have it.  The apply method in this function
	 * is passed the Binder and BindHandler from the config data context which has the configuration properties that
	 * have been loaded so far in the config data process.
	 *
	 * We will create many of the same beans in this function as we do above in the initializer above.  We do both
	 * to maintain compatibility since we are promoting those beans to the main application context.
	 */
	static final class ZookeeperFunction implements ConfigServerInstanceProvider.Function {

		private final BootstrapContext context;

		private ZookeeperFunction(BootstrapContext context) {
			this.context = context;
		}

		static ZookeeperFunction create(BootstrapContext context) {
			return new ZookeeperFunction(context);
		}

		@Override
		public List<ServiceInstance> apply(String serviceId) {
			return apply(serviceId, null, null, null);
		}

		@Override
		public List<ServiceInstance> apply(String serviceId, Binder binder, BindHandler bindHandler, Log log) {
			if (binder == null || !isEnabled(binder)) {
				return Collections.emptyList();
			}

			ZookeeperProperties properties = context.getOrElse(ZookeeperProperties.class, binder.bind(ZookeeperProperties.PREFIX, Bindable.of(ZookeeperProperties.class))
					.orElse(new ZookeeperProperties()));
			RetryPolicy retryPolicy = context.getOrElse(RetryPolicy.class, new ExponentialBackoffRetry(properties.getBaseSleepTimeMs(), properties.getMaxRetries(),
					properties.getMaxSleepMs()));
			try {
				CuratorFramework curatorFramework = context.getOrElse(CuratorFramework.class, CuratorFactory.curatorFramework(properties, retryPolicy, Stream::of,
						() -> null, () -> null));
				InstanceSerializer<ZookeeperInstance> serializer = context.getOrElse(InstanceSerializer.class, new JsonInstanceSerializer<>(ZookeeperInstance.class));
				ZookeeperDiscoveryProperties discoveryProperties = context.getOrElse(ZookeeperDiscoveryProperties.class, binder.bind(ZookeeperDiscoveryProperties.PREFIX, Bindable
								.of(ZookeeperDiscoveryProperties.class), bindHandler)
						.orElseGet(() -> new ZookeeperDiscoveryProperties(new InetUtils(new InetUtilsProperties()))));
				ServiceDiscoveryCustomizer customizer = context.getOrElse(ServiceDiscoveryCustomizer.class, new DefaultServiceDiscoveryCustomizer(curatorFramework, discoveryProperties, serializer));
				ServiceDiscovery<ZookeeperInstance> serviceDiscovery = customizer.customize(ServiceDiscoveryBuilder.builder(ZookeeperInstance.class));
				ZookeeperDependencies dependencies = context.getOrElse(ZookeeperDependencies.class, binder.bind(ZookeeperDependencies.PREFIX, Bindable
								.of(ZookeeperDependencies.class), bindHandler)
						.orElseGet(ZookeeperDependencies::new));
				return new ZookeeperDiscoveryClient(serviceDiscovery, dependencies, discoveryProperties).getInstances(serviceId);
			}
			catch (Exception e) {
				log.warn("Error fetching config server instance from Zookeeper", e);
				return Collections.emptyList();
			}

		}
	}

}
