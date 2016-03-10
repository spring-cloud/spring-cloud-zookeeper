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
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;

/**
 * @author Spencer Gibb
 */
public class ZookeeperDiscoveryHealthIndicator extends AbstractHealthIndicator {

	private static final Log log = org.apache.commons.logging.LogFactory
			.getLog(ZookeeperDiscoveryHealthIndicator.class);

	private ZookeeperServiceDiscovery serviceDiscovery;
	private ZookeeperDependencies zookeeperDependencies;
	private ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;

	public ZookeeperDiscoveryHealthIndicator(ZookeeperServiceDiscovery serviceDiscovery,
			ZookeeperDependencies zookeeperDependencies,
			ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
		this.serviceDiscovery = serviceDiscovery;
		this.zookeeperDependencies = zookeeperDependencies;
		this.zookeeperDiscoveryProperties = zookeeperDiscoveryProperties;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		try {
			Iterable<ServiceInstance<ZookeeperInstance>> allInstances = new ZookeeperServiceInstances(
					this.serviceDiscovery, this.zookeeperDependencies, this.zookeeperDiscoveryProperties);
			builder.up().withDetail("services", allInstances);
		}
		catch (Exception e) {
			log.error("Error", e);
			builder.down(e);
		}
	}
}
