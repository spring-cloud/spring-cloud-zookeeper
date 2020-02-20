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

import java.util.ArrayList;
import java.util.List;

import reactor.core.publisher.Flux;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.zookeeper.support.StatusConstants.INSTANCE_STATUS_KEY;
import static org.springframework.cloud.zookeeper.support.StatusConstants.STATUS_UP;

/**
 * A {@link ServiceInstanceListSupplier} implementation that filters available instances based on status retrieved from Zookeeper.
 *
 * @author Olga Maciaszek-Sharma
 * @since 3.0.0
 */
public class ZookeeperServiceInstanceListSupplier implements ServiceInstanceListSupplier {

	private final ServiceInstanceListSupplier delegate;
	private final String serviceId;

	public ZookeeperServiceInstanceListSupplier(ServiceInstanceListSupplier delegate,
			ZookeeperDependencies zookeeperDependencies) {
		this.delegate = delegate;
		this.serviceId = getServiceIdFromDepsOrClientName(delegate
				.getServiceId(), zookeeperDependencies);
	}

	private String getServiceIdFromDepsOrClientName(String delegateServiceId,
			ZookeeperDependencies zookeeperDependencies) {
		String serviceIdFromDeps = zookeeperDependencies
				.getPathForAlias(delegateServiceId);
		return StringUtils.hasText(serviceIdFromDeps) ? serviceIdFromDeps
				: delegateServiceId;
	}

	@Override
	public String getServiceId() {
		return serviceId;
	}

	@Override
	public Flux<List<ServiceInstance>> get() {
		return delegate.get().map(this::filteredByZookeeperStatusUp);
	}

	private List<ServiceInstance> filteredByZookeeperStatusUp(List<ServiceInstance> serviceInstances) {
		ArrayList<ServiceInstance> filteredInstances = new ArrayList<>();
		for (ServiceInstance serviceInstance : serviceInstances) {
			if (serviceInstance instanceof ZookeeperServiceInstance) {
				org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> zookeeperServiceInstance = ((ZookeeperServiceInstance) serviceInstance)
						.getServiceInstance();
				String instanceStatus = null;
				if (zookeeperServiceInstance.getPayload() != null
						&& zookeeperServiceInstance.getPayload().getMetadata() != null) {
					instanceStatus = zookeeperServiceInstance.getPayload().getMetadata()
							.get(INSTANCE_STATUS_KEY);
				}
				if (!StringUtils.hasText(instanceStatus) // backwards compatibility
						|| instanceStatus.equalsIgnoreCase(STATUS_UP)) {
					filteredInstances.add(serviceInstance);
				}
			}
		}
		return filteredInstances;
	}
}
