package org.springframework.cloud.zookeeper.discovery;

import java.util.List;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;

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
}
