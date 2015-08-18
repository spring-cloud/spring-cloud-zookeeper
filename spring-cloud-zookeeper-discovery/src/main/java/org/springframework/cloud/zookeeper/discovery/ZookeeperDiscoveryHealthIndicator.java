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

import java.util.ArrayList;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;

/**
 * @author Spencer Gibb
 */
@Slf4j
public class ZookeeperDiscoveryHealthIndicator extends AbstractHealthIndicator {

	private ZookeeperServiceDiscovery serviceDiscovery;
	private ZookeeperDependencies zookeeperDependencies;

	public ZookeeperDiscoveryHealthIndicator(ZookeeperServiceDiscovery serviceDiscovery, ZookeeperDependencies zookeeperDependencies) {
		this.serviceDiscovery = serviceDiscovery;
		this.zookeeperDependencies = zookeeperDependencies;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		try {
			Collection<String> names = getNamesToQuery();
			ArrayList<ServiceInstance<ZookeeperInstance>> allInstances = new ArrayList<>();
			for (String name : names) {
				Collection<ServiceInstance<ZookeeperInstance>> instances = serviceDiscovery
						.getServiceDiscovery().queryForInstances(name);
				for (ServiceInstance<ZookeeperInstance> instance : instances) {
					allInstances.add(instance);
				}
			}
			builder.up().withDetail("services", allInstances);
		}
		catch (Exception e) {
			log.error("Error", e);
			builder.down(e);
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<String> getNamesToQuery() throws Exception {
		if (zookeeperDependencies == null) {
			return serviceDiscovery.getServiceDiscovery().queryForNames();
		}
		return zookeeperDependencies.getDependencyNames();
	}
}
