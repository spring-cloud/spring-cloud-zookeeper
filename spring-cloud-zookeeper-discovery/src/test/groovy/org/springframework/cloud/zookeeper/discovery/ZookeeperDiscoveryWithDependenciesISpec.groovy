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
package org.springframework.cloud.zookeeper.discovery

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*

@ContextConfiguration(classes = Config, loader = SpringApplicationContextLoader)
@ActiveProfiles('watcher')
class ZookeeperDiscoveryWithDependenciesISpec extends Specification {

	@Autowired TestRibbonClient testRibbonClient
	@Autowired WireMockServer wiremockServer
	@Autowired DiscoveryClient discoveryClient
	WireMock wireMock

	def setup() {
		wireMock = new WireMock('localhost', wiremockServer.port())
		wireMock.register(get(urlEqualTo('/ping')).willReturn(aResponse().withBody('pong')))
	}

	def 'should find a collaborator via Ribbon by using its alias from dependencies'() {
		expect:
			'pong' == testRibbonClient.pingService('someAlias')
	}

	def 'should find a collaborator via discovery client'() {
		given:
			List<ServiceInstance> instances = discoveryClient.getInstances('someAlias')
			ServiceInstance instance = instances.first()
		expect:
			'pong' == testRibbonClient.pingOnUrl("${instance.host}:${instance.port}")
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig)
	@EnableDiscoveryClient
	static class Config {

		@Bean
		TestRibbonClient testRibbonClient(@LoadBalanced RestTemplate restTemplate) {
			return new TestRibbonClient(restTemplate)
		}

	}

	static class TestRibbonClient extends TestServiceRestClient {

		TestRibbonClient(RestTemplate restTemplate) {
			super(restTemplate)
		}
	}
}