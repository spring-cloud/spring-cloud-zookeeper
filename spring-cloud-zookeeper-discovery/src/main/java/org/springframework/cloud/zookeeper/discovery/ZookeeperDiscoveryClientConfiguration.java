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
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link org.springframework.cloud.client.discovery.DiscoveryClient} configuration
 * for Zookeeper.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(value = "spring.cloud.zookeeper.discovery.enabled", matchIfMissing = true)
public class ZookeeperDiscoveryClientConfiguration {

	@Autowired(required = false)
	private ZookeeperDependencies zookeeperDependencies;

	@Autowired
	private CuratorFramework curator;

	@Bean
	public ZookeeperDiscoveryProperties zookeeperDiscoveryProperties(InetUtils inetUtils) {
		return new ZookeeperDiscoveryProperties(inetUtils);
	}

	@Bean
	@ConditionalOnMissingBean
	public ZookeeperServiceDiscovery zookeeperServiceDiscovery(ZookeeperDiscoveryProperties zookeeperDiscoveryProperties, InstanceSerializer<ZookeeperInstance> instanceSerializer) {
		return new ZookeeperServiceDiscovery(this.curator, zookeeperDiscoveryProperties,
				instanceSerializer);
	}

	@Bean
	public ZookeeperLifecycle zookeeperLifecycle(ZookeeperServiceDiscovery zookeeperServiceDiscovery, ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
		return new ZookeeperLifecycle(zookeeperDiscoveryProperties, zookeeperServiceDiscovery);
	}

	@Bean
	public ZookeeperDiscoveryClient zookeeperDiscoveryClient(ZookeeperServiceDiscovery zookeeperServiceDiscovery) {
		return new ZookeeperDiscoveryClient(zookeeperServiceDiscovery, this.zookeeperDependencies);
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
		@Autowired
		private ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;
		@Autowired(required = false)
		private ZookeeperDependencies zookeeperDependencies;

		@Bean
		@ConditionalOnMissingBean
		public ZookeeperDiscoveryHealthIndicator zookeeperDiscoveryHealthIndicator() {
			return new ZookeeperDiscoveryHealthIndicator(this.serviceDiscovery,
					this.zookeeperDependencies, this.zookeeperDiscoveryProperties);
		}
	}

	@Bean
	public ZookeeperServiceWatch zookeeperServiceWatch(ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
		return new ZookeeperServiceWatch(this.curator, zookeeperDiscoveryProperties);
	}

}
