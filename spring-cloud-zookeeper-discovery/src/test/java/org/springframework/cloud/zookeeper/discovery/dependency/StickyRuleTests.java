package org.springframework.cloud.zookeeper.discovery.dependency;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.cloud.zookeeper.discovery.TestServiceRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

import com.jayway.awaitility.Awaitility;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.NoOpPing;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = StickyRuleTests.Config.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("loadbalancerclient")
public class StickyRuleTests {

	@Autowired LoadBalancerClient loadBalancerClient;
	@Autowired DiscoveryClient discoveryClient;
	
	@Test
	public void should_use_sticky_load_balancing_strategy_taken_from_Zookeeper_dependencies() {
		//given:
			System.setProperty("spring.cloud.zookeeper.dependency.ribbon.loadbalancer.checkping", "false");	
		//expect:
			thereAreTwoRegisteredServices();
			URI uri = getUriForAlias();
			Awaitility.await().until(uriMatchesTwice(uri));
	}

	private Callable<Boolean> uriMatchesTwice(final URI uri) {
		return new Callable<Boolean>() {
			@Override public Boolean call() throws Exception {
				return uriMatches() && uriMatches();
			}

			private boolean uriMatches() {
				return uri != null && uri.equals(getUriForAlias());
			}
		};
	}

	private boolean thereAreTwoRegisteredServices() {
		List<ServiceInstance> instances = this.discoveryClient.getInstances("someAlias");
		return instances != null && instances.size() == 2;
	}

	private URI getUriForAlias() {
		ServiceInstance alias = this.loadBalancerClient.choose("someAlias");
		return alias != null ? alias.getUri() : null;
	}


	@Configuration
	@EnableAutoConfiguration
	@Profile("loadbalancerclient")
	static class Config {

		@Bean
		@LoadBalanced RestTemplate loadBalancedRestTemplate() {
			return new RestTemplate();
		}

		@Bean(destroyMethod = "close") TestingServer testingServer() throws Exception {
			return new TestingServer(SocketUtils.findAvailableTcpPort());
		}

		@Bean ZookeeperProperties zookeeperProperties() throws Exception {
			ZookeeperProperties zookeeperProperties = new ZookeeperProperties();
			zookeeperProperties.setConnectString("localhost:"+ testingServer().getPort());
			return zookeeperProperties;
		}

		@Bean(initMethod = "start", destroyMethod = "stop")
		TestServiceRegistrar serviceOne(CuratorFramework curatorFramework) {
			return new TestServiceRegistrar(SocketUtils.findAvailableTcpPort(), curatorFramework);
		}

		@Bean(initMethod = "start", destroyMethod = "stop") TestServiceRegistrar serviceTwo(CuratorFramework curatorFramework) {
			return new TestServiceRegistrar(SocketUtils.findAvailableTcpPort(), curatorFramework);
		}

		@Bean IPing noOpPing() {
			return new NoOpPing();
		}
	}
}
