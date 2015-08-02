/*
 * Copyright 2012-2015 the original author or authors.
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
package org.springframework.cloud.zookeeper.discovery.dependency

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.zookeeper.common.CommonTestConfig
import org.springframework.cloud.zookeeper.common.TestRibbonClient
import org.springframework.cloud.zookeeper.discovery.PollingUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@ContextConfiguration(classes = Config, loader = SpringApplicationContextLoader)
@ActiveProfiles('dependencies')
@WebIntegrationTest(randomPort = true)
class ZookeeperDiscoveryWithDependenciesISpec extends Specification implements PollingUtils {

	@Autowired TestRibbonClient testRibbonClient
	@Autowired DiscoveryClient discoveryClient
	PollingConditions conditions

	def setup() {
		conditions = new PollingConditions()
	}

	def 'should find an instance via path when alias is not found'() {
		expect:
			conditions.eventually willPass {
				assert !discoveryClient.getInstances('nameWithoutAlias').empty
			}
	}

	def 'should find a collaborator via Ribbon by using its alias from dependencies'() {
		expect:
			conditions.eventually willPass {
				assert callingServiceAtBeansEndpointIsNotEmpty()
			}
	}

	def 'should find a collaborator via discovery client'() {
		given:
			List<ServiceInstance> instances = discoveryClient.getInstances('someAlias')
			ServiceInstance instance = instances.first()
		expect:
			conditions.eventually willPass {
				assert callingServiceViaUrlOnBeansEndpointIsNotEmpty(instance)
			}
	}

	private boolean callingServiceAtBeansEndpointIsNotEmpty() {
		return !testRibbonClient.callService('someAlias', 'beans').empty
	}

	private boolean callingServiceViaUrlOnBeansEndpointIsNotEmpty(ServiceInstance instance) {
		return !testRibbonClient.callOnUrl("${instance.host}:${instance.port}", 'beans').empty
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig)
	@EnableDiscoveryClient
	@Profile('dependencies')
	static class Config {

		@Bean
		TestRibbonClient testRibbonClient(@LoadBalanced RestTemplate restTemplate) {
			return new TestRibbonClient(restTemplate)
		}

	}

	@Controller
	@Profile('dependencies')
	class PingController {

		@RequestMapping('/ping') String ping() {
			return 'pong'
		}
	}
}