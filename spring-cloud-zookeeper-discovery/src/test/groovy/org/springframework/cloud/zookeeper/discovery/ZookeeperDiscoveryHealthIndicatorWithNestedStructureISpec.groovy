package org.springframework.cloud.zookeeper.discovery

import groovy.json.JsonSlurper
import org.apache.curator.framework.CuratorFramework
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import spock.lang.Issue
import spock.lang.Specification

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@ContextConfiguration(classes = Config, loader = SpringApplicationContextLoader)
@ActiveProfiles('nestedstructure')
@WebIntegrationTest(randomPort = true)
class ZookeeperDiscoveryHealthIndicatorWithNestedStructureISpec extends Specification implements PollingUtils {

	@Autowired TestRibbonClient testRibbonClient

	@Issue("#54 - ZookeeperDiscoveryHealthIndicator fails on nested structure")
	def 'should return a response that app is in a healthy state when nested folders in zookeeper are present'() {
		when:
			String response = testRibbonClient.callService('me', 'health')
		then:
			twoServicesArePresentedInHealthEndpoint(response)
	}

	private boolean twoServicesArePresentedInHealthEndpoint(String response) {
		def services = new JsonSlurper().parseText(response).zookeeperDiscovery.services
		assert ['me', '/a/b/c/d/anotherservice'].every { expectedServiceName ->
			services.any { service ->
				expectedServiceName == service.name
			}
		}
		return true
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	@Import(CommonTestConfig)
	@Profile('nestedstructure')
	static class Config {

		@Autowired CuratorFramework curatorFramework
		CustomZookeeperServiceDiscovery customZookeeperServiceDiscovery

		@PostConstruct
		void registerNestedDependency() {
			customZookeeperServiceDiscovery = new CustomZookeeperServiceDiscovery("/a/b/c/d/anotherservice",
					'/services', curatorFramework).build()
		}

		@PreDestroy
		void unregisterServiceDiscovery() {
			customZookeeperServiceDiscovery?.close()
		}

		@Bean
		TestRibbonClient testRibbonClient(@LoadBalanced RestTemplate restTemplate,
										  @Value('${spring.application.name}') String springAppName) {
			return new TestRibbonClient(restTemplate, springAppName)
		}
	}
}
