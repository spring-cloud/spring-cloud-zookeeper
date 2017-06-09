/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.zookeeper.support;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.zookeeper.discovery.ConditionalOnZookeeperDiscoveryEnabled;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnZookeeperDiscoveryEnabled
@AutoConfigureBefore({ ZookeeperDiscoveryAutoConfiguration.class,
		ZookeeperServiceRegistryAutoConfiguration.class })
public class CuratorServiceDiscoveryAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(ServiceDiscoveryCustomizer.class)
	public DefaultServiceDiscoveryCustomizer defaultServiceDiscoveryCustomizer(
			CuratorFramework curator, ZookeeperDiscoveryProperties properties,
			InstanceSerializer<ZookeeperInstance> serializer) {
		return new DefaultServiceDiscoveryCustomizer(curator, properties, serializer);
	}

	@Bean
	@ConditionalOnMissingBean
	public InstanceSerializer<ZookeeperInstance> deprecatedInstanceSerializer() {
		return new JsonInstanceSerializer<>(ZookeeperInstance.class);
	}

	@Bean
	@ConditionalOnMissingBean
	public ServiceDiscovery<ZookeeperInstance> curatorServiceDiscovery(
			ServiceDiscoveryCustomizer customizer) {
		return customizer.customize(ServiceDiscoveryBuilder.builder(ZookeeperInstance.class));
	}
}
