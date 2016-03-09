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
import com.netflix.loadbalancer.IPing
import com.netflix.loadbalancer.NoOpPing
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.test.TestingServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.cloud.zookeeper.ZookeeperProperties
import org.springframework.cloud.zookeeper.discovery.PollingUtils
import org.springframework.cloud.zookeeper.discovery.TestServiceRegistrar
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.SocketUtils
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import spock.util.environment.RestoreSystemProperties

@ContextConfiguration(classes = Config, loader = SpringApplicationContextLoader)
@ActiveProfiles('loadbalancerclient')
@WebIntegrationTest(randomPort = true)
class StickyRuleISpec extends Specification implements PollingUtils {

	@Autowired LoadBalancerClient loadBalancerClient
	@Autowired DiscoveryClient discoveryClient
	PollingConditions conditions

	def setup() {
		conditions = new PollingConditions()
	}

	@RestoreSystemProperties
	def 'should use sticky load balancing strategy taken from Zookeeper dependencies'() {
		given:
			System.setProperty('spring.cloud.zookeeper.dependencies.ribbon.loadbalancer.checkping', 'false')
		expect:
			thereAreTwoRegisteredServices()
			URI uri = getUriForAlias()
			conditions.eventually willPass {
				2.times {
					assert uri == getUriForAlias()
				}
			}
	}

	private boolean thereAreTwoRegisteredServices() {
		return discoveryClient.getInstances('someAlias')?.size() == 2
	}

	private URI getUriForAlias() {
		return loadBalancerClient.choose('someAlias')?.uri
	}


	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	@Profile('loadbalancerclient')
	static class Config {

		@Bean
		@LoadBalanced
		RestTemplate loadBalancedRestTemplate() {
			return new RestTemplate()
		}

		@Bean(destroyMethod = 'close')
		TestingServer testingServer() {
			return new TestingServer(SocketUtils.findAvailableTcpPort())
		}

		@Bean ZookeeperProperties zookeeperProperties() {
			return new ZookeeperProperties(connectString: "localhost:${testingServer().port}")
		}

		@Bean(initMethod = "start", destroyMethod = "stop") TestServiceRegistrar serviceOne(CuratorFramework curatorFramework) {
			return new TestServiceRegistrar(SocketUtils.findAvailableTcpPort(), curatorFramework)
		}

		@Bean(initMethod = "start", destroyMethod = "stop") TestServiceRegistrar serviceTwo(CuratorFramework curatorFramework) {
			return new TestServiceRegistrar(SocketUtils.findAvailableTcpPort(), curatorFramework)
		}

		@Bean IPing noOpPing() {
			return new NoOpPing()
		}
	}
}