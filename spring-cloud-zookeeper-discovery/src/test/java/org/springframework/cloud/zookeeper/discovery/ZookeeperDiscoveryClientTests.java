package org.springframework.cloud.zookeeper.discovery;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;
import javax.annotation.Resource;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZookeeperDiscoveryClientTests.Config.class,
		properties = { "spring.application.name=zookeeperDiscoveryClientTests", "debug=true" },
		webEnvironment = RANDOM_PORT)
@DirtiesContext
public class ZookeeperDiscoveryClientTests {

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	@EnableDiscoveryClient(autoRegister = false)
	static class Config {

	}

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

	@Autowired
	private DiscoveryClient discoveryClient;

	@Test
	public void getInstancesshouldReturnEmptyWhenNoNodeException() {

		List<ServiceInstance> instances = discoveryClient.getInstances("myservice");
		// then:
		then(instances).isEmpty();
	}
}
