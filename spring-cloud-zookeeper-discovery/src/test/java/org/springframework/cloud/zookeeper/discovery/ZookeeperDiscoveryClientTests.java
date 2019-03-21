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

import java.util.List;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.junit.Test;

import org.springframework.cloud.client.ServiceInstance;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Marcin Grzejszczak
 */
public class ZookeeperDiscoveryClientTests {

	@Test
	public void should_return_an_empty_list_of_services_if_service_discovery_is_null() {
		// given:
		ServiceDiscovery<ZookeeperInstance> serviceDiscovery = mock(
				ServiceDiscovery.class);
		ZookeeperDiscoveryClient zookeeperDiscoveryClient = new ZookeeperDiscoveryClient(
				serviceDiscovery, null, new ZookeeperDiscoveryProperties());
		// when:
		List<String> services = zookeeperDiscoveryClient.getServices();
		// then:
		then(services).isEmpty();
	}

	@Test
	public void getServicesShouldReturnEmptyWhenNoNodeException() throws Exception {
		// given:
		ServiceDiscovery<ZookeeperInstance> serviceDiscovery = mock(
				ServiceDiscovery.class);
		when(serviceDiscovery.queryForNames()).thenThrow(new NoNodeException());
		ZookeeperDiscoveryClient discoveryClient = new ZookeeperDiscoveryClient(
				serviceDiscovery, null, new ZookeeperDiscoveryProperties());
		// when:
		List<String> services = discoveryClient.getServices();
		// then:
		then(services).isEmpty();
	}

	@Test
	public void getInstancesshouldReturnEmptyWhenNoNodeException() throws Exception {
		// given:
		ServiceDiscovery<ZookeeperInstance> serviceDiscovery = mock(
				ServiceDiscovery.class);
		when(serviceDiscovery.queryForInstances("myservice"))
				.thenThrow(new NoNodeException());
		ZookeeperDiscoveryClient discoveryClient = new ZookeeperDiscoveryClient(
				serviceDiscovery, null, new ZookeeperDiscoveryProperties());
		// when:
		List<ServiceInstance> instances = discoveryClient.getInstances("myservice");
		// then:
		then(instances).isEmpty();
	}

}
