/*
 * Copyright 2015-present the original author or authors.
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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServerConfigDataLocationResolver.PropertyResolver;
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

	private static PropertyResolver getPropertyResolver(BootstrapContext context) {
		return context.getOrElseSupply(PropertyResolver.class, () -> new PropertyResolver(
				context.get(Binder.class), context.getOrElse(BindHandler.class, null)));
	}

	public static boolean isEnabled(BootstrapContext bootstrapContext) {
		PropertyResolver propertyResolver = getPropertyResolver(bootstrapContext);
		return propertyResolver.get(ConfigClientProperties.CONFIG_DISCOVERY_ENABLED, Boolean.class, false) &&
				propertyResolver.get(ConditionalOnZookeeperDiscoveryEnabled.PROPERTY, Boolean.class, true) &&
				propertyResolver.get("spring.cloud.discovery.enabled", Boolean.class, true);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initialize(BootstrapRegistry registry) {
		if (!ClassUtils.isPresent("org.springframework.cloud.config.client.ConfigServerInstanceProvider", null) ||
				// don't run if bootstrap enabled, how to check the property?
				ClassUtils.isPresent("org.springframework.cloud.bootstrap.marker.Marker", null)) {
			return;
		}

		//Register ZookeeperProperties instead of in CuratorFactor.registerCurator here so we can properly resolve the properties.
		//We need to use the PropertyResolver to do so but that is not available in CuratorFactory since it is a class
		//from Spring Cloud Config.
		registry.registerIfAbsent(ZookeeperProperties.class, context -> {
			if (!isEnabled(context)) {
				return null;
			}
			PropertyResolver propertyResolver = getPropertyResolver(context);
			return propertyResolver.resolveOrCreateConfigurationProperties(ZookeeperProperties.PREFIX, ZookeeperProperties.class);
		});

		// create curator
		CuratorFactory.registerCurator(registry, null, true, ZookeeperConfigServerBootstrapper::isEnabled);

		// create discovery
		registry.registerIfAbsent(ZookeeperDiscoveryProperties.class, context -> {
			if (!isEnabled(context)) {
				return null;
			}
			PropertyResolver propertyResolver = getPropertyResolver(context);
			return propertyResolver.resolveConfigurationProperties(ZookeeperDiscoveryProperties.PREFIX,
					ZookeeperDiscoveryProperties.class,
					() -> new ZookeeperDiscoveryProperties(new InetUtils(new InetUtilsProperties())));
		});
		registry.registerIfAbsent(InstanceSerializer.class, context -> {
			if (!isEnabled(context)) {
				return null;
			}
			return new JsonInstanceSerializer<>(ZookeeperInstance.class);
		});
		registry.registerIfAbsent(ServiceDiscoveryCustomizer.class, context -> {
			if (!isEnabled(context)) {
				return null;
			}
			CuratorFramework curator = context.get(CuratorFramework.class);
			ZookeeperDiscoveryProperties properties = context.get(ZookeeperDiscoveryProperties.class);
			InstanceSerializer<ZookeeperInstance> serializer = context.get(InstanceSerializer.class);
			return new DefaultServiceDiscoveryCustomizer(curator, properties, serializer);
		});
		registry.registerIfAbsent(ServiceDiscovery.class, context -> {
			if (!isEnabled(context)) {
				return null;
			}
			ServiceDiscoveryCustomizer customizer = context.get(ServiceDiscoveryCustomizer.class);
			return customizer.customize(ServiceDiscoveryBuilder.builder(ZookeeperInstance.class));
		});
		registry.registerIfAbsent(ZookeeperDiscoveryClient.class, context -> {
			if (!isEnabled(context)) {
				return null;
			}
			PropertyResolver propertyResolver = getPropertyResolver(context);
			ServiceDiscovery<ZookeeperInstance> serviceDiscovery = context.get(ServiceDiscovery.class);
			ZookeeperDependencies dependencies = propertyResolver.resolveConfigurationProperties(ZookeeperDependencies.PREFIX, ZookeeperDependencies.class, ZookeeperDependencies::new);
			ZookeeperDiscoveryProperties discoveryProperties = context.get(ZookeeperDiscoveryProperties.class);

			return new ZookeeperDiscoveryClient(serviceDiscovery, dependencies, discoveryProperties);
		});

		// create instance provider
		registry.registerIfAbsent(ConfigServerInstanceProvider.Function.class, context -> {
			if (!isEnabled(context)) {
				return (id) -> Collections.emptyList();
			}
			return context.get(ZookeeperDiscoveryClient.class)::getInstances;
		});

		// promote beans to context
		registry.addCloseListener(event -> {
			ZookeeperDiscoveryClient discoveryClient = event.getBootstrapContext().get(ZookeeperDiscoveryClient.class);
			if (discoveryClient != null) {
				event.getApplicationContext().getBeanFactory().registerSingleton("zookeeperServiceDiscovery",
						discoveryClient);
			}
		});
	}
}
