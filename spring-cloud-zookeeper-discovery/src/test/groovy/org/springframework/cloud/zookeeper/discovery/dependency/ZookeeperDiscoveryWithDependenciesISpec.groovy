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
package org.springframework.cloud.zookeeper.discovery.dependency
import org.springframework.beans.factory.annotation.Autowired
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
import org.springframework.cloud.zookeeper.discovery.PollingUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@ContextConfiguration(classes = Config, loader = SpringApplicationContextLoader)
@ActiveProfiles('dependencies')
@WebIntegrationTest(randomPort = true)
class ZookeeperDiscoveryWithDependenciesISpec extends Specification implements PollingUtils {

	@Autowired TestRibbonClient testRibbonClient
	@Autowired DiscoveryClient discoveryClient
	@Autowired AliasUsingFeignClient aliasUsingFeignClient
	@Autowired IdUsingFeignClient idUsingFeignClient
	PollingConditions conditions = new PollingConditions()

	def 'should find an instance via path when alias is not found'() {
		expect:
			conditions.eventually willPass {
				assert !discoveryClient.getInstances('nameWithoutAlias').empty
			}
	}

	def 'should find an instance using feign via serviceID when alias is not found'() {
		expect:
			conditions.eventually willPass {
				assert idUsingFeignClient.beans
			}
	}

	def 'should find a collaborator via load balanced rest template by using its alias from dependencies'() {
		expect:
			conditions.eventually willPass {
				assert callingServiceAtBeansEndpointIsNotEmpty()
			}
	}

	def 'should find a collaborator using feign by using its alias from dependencies'() {
		expect:
			conditions.eventually willPass {
				assert aliasUsingFeignClient.beans
			}
	}

	def 'should have headers from dependencies attached to the request via load balanced rest template'() {
		expect:
			conditions.eventually willPass {
				callingServiceToCheckIfHeadersArePassed()
			}
	}

	def 'should have headers from dependencies attached to the request via feign'() {
		expect:
			conditions.eventually willPass {
				assert aliasUsingFeignClient.checkHeaders()
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

	private void callingServiceToCheckIfHeadersArePassed() {
		testRibbonClient.callService('someAlias', 'checkHeaders')
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig)
	@EnableDiscoveryClient
	@EnableFeignClients
	@Profile('dependencies')
	static class Config {

		@Bean
		TestRibbonClient testRibbonClient(@LoadBalanced RestTemplate restTemplate) {
			return new TestRibbonClient(restTemplate)
		}

		@Bean
		PingController pingController() {
			return new PingController()
		}

	}

	@FeignClient("someAlias")
	public static interface AliasUsingFeignClient {
		@RequestMapping(method = RequestMethod.GET, value = "/beans")
		String getBeans();

		@RequestMapping(method = RequestMethod.GET, value = "/checkHeaders")
		String checkHeaders();
	}

	@FeignClient("nameWithoutAlias")
	public static interface IdUsingFeignClient {
		@RequestMapping(method = RequestMethod.GET, value = "/beans")
		String getBeans();
	}

	@RestController
	@Profile('dependencies')
	static class PingController {

		@RequestMapping('/ping') String ping() {
			return 'pong'
		}

		@RequestMapping('/checkHeaders') String checkHeaders(@RequestHeader('Content-Type') String contentType,
															 @RequestHeader('header1') Collection<String> header1,
															 @RequestHeader('header2') Collection<String> header2) {
			assert  contentType == 'application/vnd.newsletter.v1+json'
			assert  header1 == ['value1'] as Set
			assert  header2 == ['value2'] as Set
			return 'ok'
		}
	}

}