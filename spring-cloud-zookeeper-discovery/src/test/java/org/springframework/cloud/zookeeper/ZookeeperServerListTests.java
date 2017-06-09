/*
 * Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.zookeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServer;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServerList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.zookeeper.support.StatusConstants.INSTANCE_STATUS_KEY;
import static org.springframework.cloud.zookeeper.support.StatusConstants.STATUS_OUT_OF_SERVICE;
import static org.springframework.cloud.zookeeper.support.StatusConstants.STATUS_UP;

/**
 * @author Spencer Gibb
 */
public class ZookeeperServerListTests {

	@Test
	public void testNullServiceDiscoveryReturnsEmptyList() {
		ZookeeperServerList serverList = new ZookeeperServerList(null);
		List<ZookeeperServer> servers = serverList.getInitialListOfServers();
		assertThat(servers).isEmpty();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testEmptyInstancesReturnsEmptyList() throws Exception {
		ServiceDiscovery<ZookeeperInstance> serviceDiscovery = mock(ServiceDiscovery.class);
		when(serviceDiscovery.queryForInstances(anyString())).thenReturn(null);

		ZookeeperServerList serverList = new ZookeeperServerList(serviceDiscovery);
		List<ZookeeperServer> servers = serverList.getInitialListOfServers();
		assertThat(servers).isEmpty();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetServers() throws Exception {
		ArrayList<ServiceInstance<ZookeeperInstance>> instances = new ArrayList<>();
		instances.add(serviceInstance(1, null));

		ServiceDiscovery<ZookeeperInstance> serviceDiscovery = mock(ServiceDiscovery.class);
		when(serviceDiscovery.queryForInstances(nullable(String.class))).thenReturn(instances);

		ZookeeperServerList serverList = new ZookeeperServerList(serviceDiscovery);
		List<ZookeeperServer> servers = serverList.getInitialListOfServers();
		assertThat(servers).hasSize(1);
	}

	private ServiceInstance<ZookeeperInstance> serviceInstance(int instanceNum, String instanceStatus) {
		String id = "instance" + instanceNum + "id";
		String name = "instance" + instanceNum + "name";

		ZookeeperInstance payload = null;

		if (instanceStatus != null) {
			payload = new ZookeeperInstance(id, name, Collections.singletonMap(INSTANCE_STATUS_KEY, instanceStatus));
		}
		String address = "instance" + instanceNum + "addr";
		int port = 8080 + instanceNum;
		return new ServiceInstance<>(name, id, address, port, null, payload,
				0, null, null);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetServersWithInstanceStatus() throws Exception {
		ArrayList<ServiceInstance<ZookeeperInstance>> instances = new ArrayList<>();
		instances.add(serviceInstance(1, STATUS_UP));
		instances.add(serviceInstance(2, STATUS_OUT_OF_SERVICE));

		ServiceDiscovery<ZookeeperInstance> serviceDiscovery = mock(ServiceDiscovery.class);
		when(serviceDiscovery.queryForInstances(nullable(String.class))).thenReturn(instances);

		ZookeeperServerList serverList = new ZookeeperServerList(serviceDiscovery);
		List<ZookeeperServer> servers = serverList.getInitialListOfServers();
		assertThat(servers).hasSize(1);

		assertThat(servers.get(0).getInstance().getPayload().getMetadata())
				.contains(MapEntry.entry(INSTANCE_STATUS_KEY, STATUS_UP));
	}
}
