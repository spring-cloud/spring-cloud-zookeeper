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

import static org.springframework.util.ReflectionUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.util.ReflectionUtils;

/**
 * Zookeeper version of {@link DiscoveryClient}. Capable of resolving aliases from
 * {@link ZookeeperDependencies} to service names in Zookeeper.
 *
 * @author Spencer Gibb
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class ZookeeperDiscoveryClient implements DiscoveryClient {

	private static final Log log = LogFactory.getLog(ZookeeperDiscoveryClient.class);

	private ZookeeperServiceDiscovery zookeeperServiceDiscovery;

	private ZookeeperDependencies zookeeperDependencies;
	private ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

	@Deprecated
	public ZookeeperDiscoveryClient(ZookeeperServiceDiscovery zookeeperServiceDiscovery, ZookeeperDependencies zookeeperDependencies) {
		this.zookeeperServiceDiscovery = zookeeperServiceDiscovery;
		this.zookeeperDependencies = zookeeperDependencies;
	}

	public ZookeeperDiscoveryClient(ServiceDiscovery<ZookeeperInstance> serviceDiscovery, ZookeeperDependencies zookeeperDependencies) {
		this.serviceDiscovery = serviceDiscovery;
		this.zookeeperDependencies = zookeeperDependencies;
	}

	@Override
	public String description() {
		return "Spring Cloud Zookeeper Discovery Client";
	}

	@Override
	public org.springframework.cloud.client.ServiceInstance getLocalServiceInstance() {
		if (this.zookeeperServiceDiscovery == null) {
			return null;
		}
		ServiceInstance<ZookeeperInstance> serviceInstance = this.zookeeperServiceDiscovery.getServiceInstanceRef().get();
		return serviceInstance == null ? null : createServiceInstance(serviceInstance.getName(), serviceInstance);
	}

	private static org.springframework.cloud.client.ServiceInstance createServiceInstance(String serviceId, ServiceInstance<ZookeeperInstance> serviceInstance) {
		return new ZookeeperServiceInstance(serviceId, serviceInstance);
	}

	@Override
	public List<org.springframework.cloud.client.ServiceInstance> getInstances(
			final String serviceId) {
		try {
			if (getServiceDiscovery() == null) {
				return Collections.EMPTY_LIST;
			}
			String serviceIdToQuery = getServiceIdToQuery(serviceId);
			Collection<ServiceInstance<ZookeeperInstance>> zkInstances = getServiceDiscovery().queryForInstances(serviceIdToQuery);
			List<org.springframework.cloud.client.ServiceInstance> instances = new ArrayList<>();
			for (ServiceInstance<ZookeeperInstance> instance : zkInstances) {
				instances.add(createServiceInstance(serviceIdToQuery, instance));
			}
			return instances;
		} catch (Exception exception) {
			ReflectionUtils.rethrowRuntimeException(exception);
		}
		return new ArrayList<>();
	}

	private ServiceDiscovery<ZookeeperInstance> getServiceDiscovery() {
		if (this.serviceDiscovery != null) {
			return this.serviceDiscovery;
		}
		if (this.zookeeperServiceDiscovery.getServiceDiscoveryRef() == null) {
			return null;
		}
		return this.zookeeperServiceDiscovery.getServiceDiscoveryRef().get();
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
		List<String> services = null;
		if (getServiceDiscovery() == null) {
			log.warn("Service Discovery is not yet ready - returning empty list of services");
			return Collections.emptyList();
		}
		try {
			services = new ArrayList<>(getServiceDiscovery().queryForNames());
		}
		catch (Exception e) {
			rethrowRuntimeException(e);
		}
		return services;
	}
}
