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
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.test.TestingServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.zookeeper.ZookeeperProperties
import org.springframework.cloud.zookeeper.discovery.TestServiceRegistrar
import org.springframework.cloud.zookeeper.discovery.TestServiceRestClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.SocketUtils
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*

@ContextConfiguration(classes = Config, loader = SpringApplicationContextLoader)
@ActiveProfiles('loadbalancerclient')
@WebIntegrationTest(randomPort = true)
class StickyRuleISpec extends Specification {

	@Autowired TestRibbonClient testRibbonClient
	@Autowired WireMockServer wiremockServerOne
	@Autowired WireMockServer wiremockServerTwo

	def setup() {
		new WireMock('localhost', wiremockServerOne.port()).register(get(urlEqualTo('/ping')).willReturn(aResponse().withBody('pong')))
		new WireMock('localhost', wiremockServerTwo.port()).register(get(urlEqualTo('/ping')).willReturn(aResponse().withBody('pongFromSecondServer')))
	}

	def 'should use sticky load balancing strategy taken from Zookeeper dependencies'() {
		given:
			String initialResponse = testRibbonClient.pingService('someAlias')
		expect:
			5.times {
				assert testRibbonClient.pingService('someAlias') == initialResponse
			}
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	static class Config {

		@Bean(destroyMethod = 'close')
		TestingServer testingServer() {
			return new TestingServer(SocketUtils.findAvailableTcpPort())
		}

		@Bean(initMethod = "start", destroyMethod = "shutdown") WireMockServer wiremockServerOne() {
			return new WireMockServer(SocketUtils.findAvailableTcpPort())
		}

		@Bean(initMethod = "start", destroyMethod = "shutdown") WireMockServer wiremockServerTwo() {
			return new WireMockServer(SocketUtils.findAvailableTcpPort())
		}

		@Bean TestRibbonClient testRibbonClient(@LoadBalanced RestTemplate restTemplate) {
			return new TestRibbonClient(restTemplate)
		}

		@Bean ZookeeperProperties zookeeperProperties() {
			return new ZookeeperProperties(connectString: "localhost:${testingServer().port}")
		}

		@Bean(initMethod = "start", destroyMethod = "stop") TestServiceRegistrar serviceOne(CuratorFramework curatorFramework) {
			return new TestServiceRegistrar(wiremockServerOne().port(), curatorFramework)
		}

		@Bean(initMethod = "start", destroyMethod = "stop") TestServiceRegistrar serviceTwo(CuratorFramework curatorFramework) {
			return new TestServiceRegistrar(wiremockServerTwo().port(), curatorFramework)
		}

	}

	static class TestRibbonClient extends TestServiceRestClient {

		TestRibbonClient(RestTemplate restTemplate) {
			super(restTemplate)
		}
	}
}