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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.Bootstrapper;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServerInstanceProvider;
import org.springframework.cloud.zookeeper.CuratorFactory;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.cloud.zookeeper.support.DefaultServiceDiscoveryCustomizer;
import org.springframework.cloud.zookeeper.support.ServiceDiscoveryCustomizer;
import org.springframework.util.ClassUtils;

public class ZookeeperConfigServerBootstrapper implements Bootstrapper {

	@Override
	@SuppressWarnings("unchecked")
	public void intitialize(BootstrapRegistry registry) {
		if (!ClassUtils.isPresent("org.springframework.cloud.config.client.ConfigServerInstanceProvider", null)) {
			return;
		}
		// create curator
		CuratorFactory.registerCurator(registry, null, true);

		// create discovery
		registry.registerIfAbsent(ZookeeperDiscoveryProperties.class,
				context -> context.get(Binder.class)
						.bind(ZookeeperDiscoveryProperties.PREFIX, ZookeeperDiscoveryProperties.class)
						.orElseGet(() -> new ZookeeperDiscoveryProperties(new InetUtils(new InetUtilsProperties()))));
		registry.registerIfAbsent(InstanceSerializer.class,
				context -> new JsonInstanceSerializer<>(ZookeeperInstance.class));
		registry.registerIfAbsent(ServiceDiscoveryCustomizer.class, context -> {
			CuratorFramework curator = context.get(CuratorFramework.class);
			ZookeeperDiscoveryProperties properties = context.get(ZookeeperDiscoveryProperties.class);
			InstanceSerializer<ZookeeperInstance> serializer = context.get(InstanceSerializer.class);
			return new DefaultServiceDiscoveryCustomizer(curator, properties, serializer);
		});
		registry.registerIfAbsent(ServiceDiscovery.class, context -> {
			ServiceDiscoveryCustomizer customizer = context.get(ServiceDiscoveryCustomizer.class);
			return customizer.customize(ServiceDiscoveryBuilder.builder(ZookeeperInstance.class));
		});
		registry.registerIfAbsent(ZookeeperDiscoveryClient.class, context -> {
			Binder binder = context.get(Binder.class);
			if (!isEnabled(binder)) {
				return null;
			}
			ServiceDiscovery<ZookeeperInstance> serviceDiscovery = context.get(ServiceDiscovery.class);
			ZookeeperDependencies dependencies = binder.bind(ZookeeperDependencies.PREFIX, ZookeeperDependencies.class)
					.orElseGet(ZookeeperDependencies::new);
			ZookeeperDiscoveryProperties discoveryProperties = context.get(ZookeeperDiscoveryProperties.class);

			return new ZookeeperDiscoveryClient(serviceDiscovery, dependencies, discoveryProperties);
		});

		// create instance provider
		registry.registerIfAbsent(ConfigServerInstanceProvider.Function.class, context -> {
			if (!isEnabled(context.get(Binder.class))) {
				return null;
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

	private boolean isEnabled(Binder binder) {
		return binder.bind(ConfigClientProperties.CONFIG_DISCOVERY_ENABLED, Boolean.class).orElse(false);
	}

}
