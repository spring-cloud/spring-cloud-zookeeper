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
import java.util.Collections;
import java.util.List;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.util.StringUtils;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;

import static org.springframework.cloud.zookeeper.support.StatusConstants.INSTANCE_STATUS_KEY;
import static org.springframework.cloud.zookeeper.support.StatusConstants.STATUS_UP;
import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * Zookeeper version of {@link AbstractServerList} that returns the list of
 * servers on which instances are ran. The implementation is capable of resolving
 * the servers from {@link ZookeeperDependencies}.
 *
 * @author Spencer Gibb
 * @author Marcin Grzejszczak
 * @since 1.0.0
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
		this.serviceId = getServiceIdFromDepsOrClientName(clientConfig, zookeeperDependencies);
	}

	private String getServiceIdFromDepsOrClientName(IClientConfig clientConfig, ZookeeperDependencies zookeeperDependencies) {
		String serviceIdFromDeps = zookeeperDependencies.getPathForAlias(clientConfig.getClientName());
		return StringUtils.hasText(serviceIdFromDeps) ? serviceIdFromDeps : clientConfig.getClientName();
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
	protected List<ZookeeperServer> getServers() {
		try {
			if (this.serviceDiscovery == null) {
				return Collections.EMPTY_LIST;
			}
			Collection<ServiceInstance<ZookeeperInstance>> instances = this.serviceDiscovery
					.queryForInstances(this.serviceId);
			if (instances == null || instances.isEmpty()) {
				return Collections.EMPTY_LIST;
			}
			List<ZookeeperServer> servers = new ArrayList<>();
			for (ServiceInstance<ZookeeperInstance> instance : instances) {
				String instanceStatus = null;
				if (instance.getPayload() != null && instance.getPayload().getMetadata() != null) {
					instanceStatus = instance.getPayload().getMetadata().get(INSTANCE_STATUS_KEY);
				}
				if (!StringUtils.hasText(instanceStatus) // backwards compatibility
						|| instanceStatus.equalsIgnoreCase(STATUS_UP)) {
					servers.add(new ZookeeperServer(instance));
				}
			}
			return servers;
		}
		catch (Exception e) {
			rethrowRuntimeException(e);
		}
		return Collections.EMPTY_LIST;
	}
}
