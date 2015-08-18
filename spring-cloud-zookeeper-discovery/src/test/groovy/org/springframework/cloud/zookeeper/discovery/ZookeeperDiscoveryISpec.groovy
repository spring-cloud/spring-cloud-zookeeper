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
package org.springframework.cloud.zookeeper.discovery

import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.netflix.feign.EnableFeignClients
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@ContextConfiguration(classes = Config, loader = SpringApplicationContextLoader)
@ActiveProfiles('ribbon')
@WebIntegrationTest(randomPort = true)
class ZookeeperDiscoveryISpec extends Specification implements PollingUtils {

	@Autowired TestRibbonClient testRibbonClient
	@Autowired DiscoveryClient discoveryClient
    @Autowired ZookeeperServiceDiscovery serviceDiscovery
	@Value('${spring.application.name}') String springAppName
	@Autowired IdUsingFeignClient idUsingFeignClient
	PollingConditions conditions = new PollingConditions()


	def 'should find the app by its name via Ribbon'() {
		expect:
			'UP' == registeredServiceStatusViaServiceName()
	}

	def 'should find a collaborator via discovery client'() {
		given:
			List<ServiceInstance> instances = discoveryClient.getInstances(springAppName)
			ServiceInstance instance = instances.first()
		expect:
			'UP' == registeredServiceStatus(instance)
	}

	def 'should find an instance using feign via service id'() {
		expect:
			conditions.eventually willPass {
				assert idUsingFeignClient.beans
			}
	}

	private String registeredServiceStatusViaServiceName() {
		return new JsonSlurper().parseText(testRibbonClient.thisHealthCheck()).status
	}

	private String registeredServiceStatus(ServiceInstance instance) {
		return new JsonSlurper().parseText(testRibbonClient.callOnUrl("${instance.host}:${instance.port}", 'health')).status
	}

	def 'should properly find local instance'() {
		expect:
			serviceDiscovery.serviceInstance.address == discoveryClient.localServiceInstance.host
	}

	@FeignClient("ribbonApp")
	public static interface IdUsingFeignClient {
		@RequestMapping(method = RequestMethod.GET, value = "/beans")
		String getBeans();
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig)
	@EnableDiscoveryClient
	@EnableFeignClients
	@Profile('ribbon')
	static class Config {

		@Bean
		TestRibbonClient testRibbonClient(@LoadBalanced RestTemplate restTemplate,
										  @Value('${spring.application.name}') String springAppName) {
			return new TestRibbonClient(restTemplate, springAppName)
		}
	}

	@Controller
	@Profile('ribbon')
	class PingController {

		@RequestMapping('/ping') String ping() {
			return 'pong'
		}
	}
}
