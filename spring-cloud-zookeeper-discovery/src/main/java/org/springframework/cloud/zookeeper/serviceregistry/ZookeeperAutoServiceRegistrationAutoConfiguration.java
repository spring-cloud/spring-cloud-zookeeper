/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.zookeeper.serviceregistry;

/**
 * @author Spencer Gibb
 */

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ConditionalOnZookeeperDiscoveryEnabled;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceDiscovery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(type = "org.springframework.cloud.zookeeper.discovery.ZookeeperLifecycle")
@ConditionalOnZookeeperDiscoveryEnabled
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@AutoConfigureAfter( { ZookeeperServiceRegistryAutoConfiguration.class} )
@AutoConfigureBefore( {AutoServiceRegistrationAutoConfiguration.class,ZookeeperDiscoveryAutoConfiguration.class} )
public class ZookeeperAutoServiceRegistrationAutoConfiguration {

	@Bean
	public ZookeeperAutoServiceRegistration zookeeperAutoServiceRegistration(ZookeeperServiceRegistry registry, ZookeeperRegistration registration,
			ZookeeperDiscoveryProperties properties) {
		return new ZookeeperAutoServiceRegistration(registry, registration, properties);
	}


	@Bean
	@ConditionalOnMissingBean
	public ZookeeperServiceDiscovery zookeeperServiceDiscovery(CuratorFramework curator, ZookeeperDiscoveryProperties zookeeperDiscoveryProperties, InstanceSerializer<ZookeeperInstance> instanceSerializer) {
		return new ZookeeperServiceDiscovery(curator, zookeeperDiscoveryProperties,
				instanceSerializer);
	}
}
