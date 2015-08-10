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

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * @author Spencer Gibb
 * @author Marcin Grzejszczak, 4financeIT
 */
public class ZookeeperServerList extends AbstractServerList<ZookeeperServer> {

	private String serviceId;
	private final ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

	public ZookeeperServerList(ServiceDiscovery<ZookeeperInstance> serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig clientConfig) {
		this.serviceId = clientConfig.getClientName();
	}

	public void initFromDependencies(IClientConfig clientConfig, ZookeeperDependencies zookeeperDependencies) {
		this.serviceId = zookeeperDependencies.getPathForAlias(clientConfig.getClientName());
	}

	@Override
	public List<ZookeeperServer> getInitialListOfServers() {
		return getServers();
	}

	@Override
	public List<ZookeeperServer> getUpdatedListOfServers() {
		return getServers();
	}

	@SuppressWarnings("unchecked")
	private List<ZookeeperServer> getServers() {
		try {
			Collection<ServiceInstance<ZookeeperInstance>> instances = serviceDiscovery
					.queryForInstances(serviceId);
			if (instances == null || instances.isEmpty()) {
				return Collections.EMPTY_LIST;
			}
			List<ZookeeperServer> servers = new ArrayList<>();
			for (ServiceInstance<ZookeeperInstance> instance : instances) {
				servers.add(new ZookeeperServer(instance));
			}

			return servers;
		}
		catch (Exception e) {
			rethrowRuntimeException(e);
		}
		return Collections.EMPTY_LIST;
	}
}
