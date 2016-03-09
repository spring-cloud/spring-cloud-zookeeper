/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.zookeeper.discovery.dependency
import org.apache.curator.framework.CuratorFramework
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.zookeeper.discovery.PollingUtils
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
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
	@Autowired ZookeeperDependencies zookeeperDependencies
	@Autowired Config dependencyConfig
	@Autowired CuratorFramework curatorFramework
	PollingConditions conditions = new PollingConditions(timeout: 2)

	def 'should find an instance via path when alias is not found'() {
		expect:
			conditions.eventually willPass {
				assert !discoveryClient.getInstances('nameWithoutAlias').empty
			}
	}

	def 'should fill out properly the stub section of a dependency'() {
		given:
			StubsConfiguration stubsConfiguration = zookeeperDependencies.dependencies.get('someAlias').stubsConfiguration
		expect:
			stubsConfiguration.stubsGroupId == 'org.springframework'
			stubsConfiguration.stubsArtifactId == 'foo'
			stubsConfiguration.stubsClassifier == 'stubs'
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

	def 'should have path equal to alias'() {
		given:
			def dependency = zookeeperDependencies.getDependencyForAlias('aliasIsPath')
		expect:
			dependency.path == 'aliasIsPath'
	}

	def 'should have alias equal to path'() {
		given:
			def dependency = zookeeperDependencies.getDependencyForPath('aliasIsPath')
		expect:
			dependency.path == 'aliasIsPath'
	}

	def 'should have path set via string constructor'() {
		given:
			def dependency = zookeeperDependencies.getDependencyForAlias('anotherAlias')
		expect:
			dependency.path == 'myPath'
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
	@Import(DependencyConfig)
	@Profile('dependencies')
	static class Config {
	}

}