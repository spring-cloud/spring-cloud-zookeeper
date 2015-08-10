/*
 * Copyright 2013-2015 the original author or authors.
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

package org.springframework.cloud.zookeeper.discovery;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnProperty(value = "spring.cloud.zookeeper.discovery.enabled", matchIfMissing = true)
@EnableConfigurationProperties
public class ZookeeperDiscoveryClientConfiguration {

	@Autowired(required = false)
	private ZookeeperDependencies zookeeperDependencies;

	@Autowired
	private CuratorFramework curator;

	@Bean
	public ZookeeperDiscoveryProperties zookeeperDiscoveryProperties() {
		return new ZookeeperDiscoveryProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public ZookeeperServiceDiscovery zookeeperServiceDiscovery() {
		return new ZookeeperServiceDiscovery(curator, zookeeperDiscoveryProperties(),
				instanceSerializer());
	}

	@Bean
	public ZookeeperLifecycle zookeeperLifecycle() {
		return new ZookeeperLifecycle(zookeeperDiscoveryProperties(), zookeeperServiceDiscovery());
	}

	@Bean
	public ZookeeperDiscoveryClient zookeeperDiscoveryClient() {
		return new ZookeeperDiscoveryClient(zookeeperServiceDiscovery(), zookeeperDependencies);
	}

	@Bean
	public InstanceSerializer<ZookeeperInstance> instanceSerializer() {
		return new JsonInstanceSerializer<>(ZookeeperInstance.class);
	}

	@Configuration
	@ConditionalOnClass(Endpoint.class)
	protected static class ZookeeperDiscoveryHealthConfig {
		@Autowired
		private ZookeeperServiceDiscovery serviceDiscovery;

		@Bean
		public ZookeeperDiscoveryHealthIndicator zookeeperDiscoveryHealthIndicator() {
			return new ZookeeperDiscoveryHealthIndicator(serviceDiscovery);
		}
	}

	@Bean
	public ZookeeperServiceWatch zookeeperServiceWatch() {
		return new ZookeeperServiceWatch(curator, zookeeperDiscoveryProperties());
	}

}
