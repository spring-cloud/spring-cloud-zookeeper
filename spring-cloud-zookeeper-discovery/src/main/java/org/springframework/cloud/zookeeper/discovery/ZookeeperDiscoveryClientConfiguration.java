/*
 * Copyright 2015-2019 the original author or authors.
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

package org.springframework.cloud.zookeeper.discovery;

import org.apache.curator.x.discovery.ServiceDiscovery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnBlockingDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link org.springframework.cloud.client.discovery.DiscoveryClient} configuration for
 * Zookeeper.
 *
 * @author Spencer Gibb
 * @author Tim Ysewyn
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnBlockingDiscoveryEnabled
@ConditionalOnZookeeperDiscoveryEnabled
@AutoConfigureBefore({ ZookeeperDiscoveryAutoConfiguration.class })
public class ZookeeperDiscoveryClientConfiguration {

	@Autowired(required = false)
	private ZookeeperDependencies zookeeperDependencies;

	@Bean
	@ConditionalOnMissingBean
	// currently means auto-registration is false. That will change when
	// ZookeeperServiceDiscovery is gone
	public ZookeeperDiscoveryClient zookeeperDiscoveryClient(
			ServiceDiscovery<ZookeeperInstance> serviceDiscovery,
			ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
		return new ZookeeperDiscoveryClient(serviceDiscovery, zookeeperDependencies,
				zookeeperDiscoveryProperties);
	}

	@Bean
	public Marker zookeeperDiscoveryClientMarker() {
		return new Marker();
	}

	class Marker {
	}

}
