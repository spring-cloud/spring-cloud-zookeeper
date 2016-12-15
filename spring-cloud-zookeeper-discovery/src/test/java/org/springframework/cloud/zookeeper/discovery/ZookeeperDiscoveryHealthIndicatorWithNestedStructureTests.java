package org.springframework.cloud.zookeeper.discovery;

import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.UriSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperBuilderRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZookeeperDiscoveryHealthIndicatorWithNestedStructureTests.Config.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("nestedstructure")
public class ZookeeperDiscoveryHealthIndicatorWithNestedStructureTests {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	@Autowired TestRibbonClient testRibbonClient;
	@Autowired CuratorFramework curatorFramework;

	// Issue: #54 - ZookeeperDiscoveryHealthIndicator fails on nested structure
	@Test public void should_return_a_response_that_app_is_in_a_healthy_state_when_nested_folders_in_zookeeper_are_present()
			throws Exception {
		// when:
		String response = this.testRibbonClient.callService("me", "health");
		// then:
		log.info("Received response [" + response + "]");
		then(this.curatorFramework.getChildren().forPath("/services/me")).isNotEmpty();
		then(this.curatorFramework.getChildren().forPath("/services/a/b/c/d/anotherservice")).isNotEmpty();
	}
	
	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	@Import(CommonTestConfig.class)
	@Profile("nestedstructure")
	static class Config {

		@Autowired
		ZookeeperServiceRegistry serviceRegistry;
		private ZookeeperRegistration registration;

		@PostConstruct
		void registerNestedDependency() {
			try {
				ServiceInstanceBuilder<ZookeeperInstance> builder = ServiceInstance.<ZookeeperInstance>builder()
						.uriSpec(new UriSpec("{scheme}://{address}:{port}/"))
						.address("anyUrl")
						.port(10)
						.name("/a/b/c/d/anotherservice");
				this.registration = new ZookeeperBuilderRegistration(builder);
				this.serviceRegistry.register(registration);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			//this.customZookeeperServiceDiscovery = new CustomZookeeperServiceDiscovery(,
			//		"/services", this.curatorFramework);
		}

		@PreDestroy
		void unregisterServiceDiscovery() {
			this.serviceRegistry.deregister(this.registration);
		}

		@Bean TestRibbonClient testRibbonClient(@LoadBalanced RestTemplate restTemplate,
				@Value("${spring.application.name}") String springAppName) {
			return new TestRibbonClient(restTemplate, springAppName);
		}
	}
}
