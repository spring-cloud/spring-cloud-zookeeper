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

	@Test public void should_return_an_empty_list_of_services_if_service_discovery_is_null() {
		// given:
		ServiceDiscovery<ZookeeperInstance> serviceDiscovery = mock(ServiceDiscovery.class);
		ZookeeperDiscoveryClient zookeeperDiscoveryClient = new ZookeeperDiscoveryClient(serviceDiscovery, null);
		// when:
		List<String> services = zookeeperDiscoveryClient.getServices();
		// then:
		then(services).isEmpty();
	}

	@Test
	public void getServicesShouldReturnEmptyWhenNoNodeException() throws Exception {
		// given:
		ServiceDiscovery<ZookeeperInstance> serviceDiscovery = mock(ServiceDiscovery.class);
		when(serviceDiscovery.queryForNames()).thenThrow(new NoNodeException());
		ZookeeperDiscoveryClient discoveryClient = new ZookeeperDiscoveryClient(serviceDiscovery, null);
		// when:
		List<String> services = discoveryClient.getServices();
		// then:
		then(services).isEmpty();
	}

	@Test
	public void getInstancesshouldReturnEmptyWhenNoNodeException() throws Exception {
		// given:
		ServiceDiscovery<ZookeeperInstance> serviceDiscovery = mock(ServiceDiscovery.class);
		when(serviceDiscovery.queryForInstances("myservice")).thenThrow(new NoNodeException());
		ZookeeperDiscoveryClient discoveryClient = new ZookeeperDiscoveryClient(serviceDiscovery, null);
		// when:
		List<ServiceInstance> instances = discoveryClient.getInstances("myservice");
		// then:
		then(instances).isEmpty();
	}
}
