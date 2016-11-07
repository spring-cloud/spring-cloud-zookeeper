package org.springframework.cloud.zookeeper.discovery;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ZookeeperDiscoveryHealthIndicatorWithNestedStructureTests.Config.class)
@ActiveProfiles("nestedstructure")
@WebIntegrationTest(randomPort = true)
public class ZookeeperDiscoveryHealthIndicatorWithNestedStructureTests {

	@Autowired TestRibbonClient testRibbonClient;

	// Issue: #54 - ZookeeperDiscoveryHealthIndicator fails on nested structure
	@Test public void should_return_a_response_that_app_is_in_a_healthy_state_when_nested_folders_in_zookeeper_are_present() {
		// when:
		Response response = this.testRibbonClient.callService("me", "health", Response.class);
		// then:
		then(response.discoveryComposite.zookeeper.services).extracting("name").contains("me", "/a/b/c/d/anotherservice");
	}

	public static class Response {
		DiscoveryComposite discoveryComposite;

		public DiscoveryComposite getDiscoveryComposite() {
			return this.discoveryComposite;
		}

		public void setDiscoveryComposite(DiscoveryComposite discoveryComposite) {
			this.discoveryComposite = discoveryComposite;
		}
	}

	public static class DiscoveryComposite {
		Zoo zookeeper;

		public Zoo getZookeeper() {
			return this.zookeeper;
		}

		public void setZookeeper(Zoo zookeeper) {
			this.zookeeper = zookeeper;
		}
	}

	public static class Zoo {
		List<Service> services;

		public List<Service> getServices() {
			return this.services;
		}

		public void setServices(List<Service> services) {
			this.services = services;
		}
	}

	public static class Service {
		String name;

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
	
	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	@Import(CommonTestConfig.class)
	@Profile("nestedstructure")
	static class Config {

		@Autowired CuratorFramework curatorFramework;
		CustomZookeeperServiceDiscovery customZookeeperServiceDiscovery;

		@PostConstruct
		void registerNestedDependency() {
			this.customZookeeperServiceDiscovery = new CustomZookeeperServiceDiscovery("/a/b/c/d/anotherservice",
					"/services", this.curatorFramework);
			this.customZookeeperServiceDiscovery.build();
		}

		@PreDestroy
		void unregisterServiceDiscovery() {
			if (this.customZookeeperServiceDiscovery != null) {
				this.customZookeeperServiceDiscovery.close();
			}
		}

		@Bean TestRibbonClient testRibbonClient(@LoadBalanced RestTemplate restTemplate,
				@Value("${spring.application.name}") String springAppName) {
			return new TestRibbonClient(restTemplate, springAppName);
		}
	}
}
