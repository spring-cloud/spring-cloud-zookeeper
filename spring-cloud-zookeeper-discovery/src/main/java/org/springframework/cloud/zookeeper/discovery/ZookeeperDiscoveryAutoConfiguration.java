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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 * @author Tim Ysewyn
 * @since 1.1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnZookeeperDiscoveryEnabled
@AutoConfigureBefore({CommonsClientAutoConfiguration.class})
public class ZookeeperDiscoveryAutoConfiguration {

	@Autowired
	private CuratorFramework curator;

	@Bean
	@ConditionalOnMissingBean
	public ZookeeperDiscoveryProperties zookeeperDiscoveryProperties(
			InetUtils inetUtils) {
		return new ZookeeperDiscoveryProperties(inetUtils);
	}

	@Bean
	public ZookeeperServiceWatch zookeeperServiceWatch(
			ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
		return new ZookeeperServiceWatch(curator, zookeeperDiscoveryProperties);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({ Endpoint.class, HealthIndicator.class })
	protected static class ZookeeperDiscoveryHealthConfig {

		@Autowired(required = false)
		private ZookeeperDependencies zookeeperDependencies;

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnEnabledHealthIndicator("zookeeper")
		public ZookeeperDiscoveryHealthIndicator zookeeperDiscoveryHealthIndicator(
				CuratorFramework curatorFramework,
				ServiceDiscovery<ZookeeperInstance> serviceDiscovery,
				ZookeeperDiscoveryProperties properties) {
			return new ZookeeperDiscoveryHealthIndicator(curatorFramework,
					serviceDiscovery, zookeeperDependencies, properties);
		}

	}

}
