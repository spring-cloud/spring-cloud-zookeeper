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

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;

import lombok.SneakyThrows;

/**
 * @author Spencer Gibb
 * @author Marcin Grzejszczak, 4financeIT
 */
public class ZookeeperDiscoveryClient implements DiscoveryClient {

	private ZookeeperServiceDiscovery serviceDiscovery;

	private ZookeeperDependencies zookeeperDependencies;

	public ZookeeperDiscoveryClient(ZookeeperServiceDiscovery serviceDiscovery, ZookeeperDependencies zookeeperDependencies) {
		this.serviceDiscovery = serviceDiscovery;
		this.zookeeperDependencies = zookeeperDependencies;
	}

	@Override
	public String description() {
		return "Spring Cloud Zookeeper Discovery Client";
	}

	@Override
	public org.springframework.cloud.client.ServiceInstance getLocalServiceInstance() {
		ServiceInstance<ZookeeperInstance> serviceInstance = this.serviceDiscovery.getServiceInstance();
		return createServiceInstance(serviceInstance.getId(), serviceInstance);
	}

	private static org.springframework.cloud.client.ServiceInstance createServiceInstance(String serviceId, ServiceInstance<ZookeeperInstance> serviceInstance) {
		boolean secure = serviceInstance.getSslPort() != null;
		Integer port = serviceInstance.getPort();

		if (secure) {
			port = serviceInstance.getSslPort();
		}

		return new DefaultServiceInstance(serviceId, serviceInstance.getAddress(), port, secure);
	}

	@Override
	@SneakyThrows
	public List<org.springframework.cloud.client.ServiceInstance> getInstances(
			final String serviceId) {
		String serviceIdToQuery = getServiceIdToQuery(serviceId);
		Collection<ServiceInstance<ZookeeperInstance>> zkInstances = this.serviceDiscovery
			.getServiceDiscovery().queryForInstances(serviceIdToQuery);

		ArrayList<org.springframework.cloud.client.ServiceInstance> instances = new ArrayList<>();

		for (ServiceInstance<ZookeeperInstance> instance : zkInstances) {
			instances.add(createServiceInstance(serviceIdToQuery, instance));
		}

		return instances;
	}

	private String getServiceIdToQuery(String serviceId) {
		if (this.zookeeperDependencies != null && this.zookeeperDependencies.hasDependencies()) {
			String pathForAlias = this.zookeeperDependencies.getPathForAlias(serviceId);
			return pathForAlias.isEmpty() ? serviceId : pathForAlias;
		}
		return serviceId;
	}

	@Override
	public List<String> getServices() {
		ArrayList<String> services = null;
		try {
			services = new ArrayList<>(this.serviceDiscovery.getServiceDiscovery().queryForNames());
		}
		catch (Exception e) {
			rethrowRuntimeException(e);
		}
		return services;
	}
}
