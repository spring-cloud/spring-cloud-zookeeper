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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.client.discovery.health.DiscoveryHealthIndicator;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;

/**
 * {@link org.springframework.boot.actuate.health.HealthIndicator} that presents the
 * status of all instances registered in Zookeeper.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
public class ZookeeperDiscoveryHealthIndicator implements DiscoveryHealthIndicator {

	private static final Log log = LogFactory
			.getLog(ZookeeperDiscoveryHealthIndicator.class);

	private CuratorFramework curatorFramework;
	private ServiceDiscovery<ZookeeperInstance> serviceDiscovery;
	private final ZookeeperDependencies zookeeperDependencies;
	private final ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;

	public ZookeeperDiscoveryHealthIndicator(CuratorFramework curatorFramework,
			ServiceDiscovery<ZookeeperInstance> serviceDiscovery,
			ZookeeperDependencies zookeeperDependencies,
			ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
		this.curatorFramework = curatorFramework;
		this.serviceDiscovery = serviceDiscovery;
		this.zookeeperDependencies = zookeeperDependencies;
		this.zookeeperDiscoveryProperties = zookeeperDiscoveryProperties;
	}

	@Override
	public String getName() {
		return "zookeeper";
	}

	@Override
	public Health health() {
		Health.Builder builder = Health.unknown();
		try {
			Iterable<ServiceInstance<ZookeeperInstance>> allInstances =
					new ZookeeperServiceInstances(this.curatorFramework,
						this.serviceDiscovery, this.zookeeperDependencies,
						this.zookeeperDiscoveryProperties);
			builder.up().withDetail("services", allInstances);
		}
		catch (Exception e) {
			log.error("Error", e);
			builder.down(e);
		}

		return builder.build();
	}

}
