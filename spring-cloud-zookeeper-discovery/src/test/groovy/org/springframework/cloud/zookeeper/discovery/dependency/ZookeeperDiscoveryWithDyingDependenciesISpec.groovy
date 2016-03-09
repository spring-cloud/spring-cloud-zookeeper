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

import org.apache.curator.test.TestingServer
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.util.SocketUtils
import spock.lang.Issue
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

class ZookeeperDiscoveryWithDyingDependenciesISpec extends Specification {

	@Issue("#45")
	@RestoreSystemProperties
	def "should refresh a dependency in Ribbon when the dependency has de-registered and registered in Zookeeper"() {
		given:
			int zookeeperPort = SocketUtils.findAvailableTcpPort()
			TestingServer testingServer = new TestingServer(zookeeperPort)
			System.setProperty('spring.jmx.enabled', 'false')
			System.setProperty('spring.cloud.zookeeper.connectString', "127.0.0.1:$zookeeperPort")
		and:
			ConfigurableApplicationContext serverContext = contextWithProfile('server')
			ConfigurableApplicationContext clientContext = contextWithProfile('client')
		and:
			Integer portBeforeDying = callServiceAtPortEndpoint(clientContext)
		and:
			serverContext = restartContext(serverContext, 'server')
		expect:
			callServiceAtPortEndpoint(clientContext) != portBeforeDying
		cleanup:
			serverContext?.close()
			clientContext?.close()
			testingServer?.close()
	}

	private ConfigurableApplicationContext contextWithProfile(String profile) {
		return new SpringApplicationBuilder(Config).profiles(profile).build().run()
	}

	private ConfigurableApplicationContext restartContext(ConfigurableApplicationContext configurableApplicationContext, String profile) {
		configurableApplicationContext.close()
		return contextWithProfile(profile)
	}

	private Integer callServiceAtPortEndpoint(ApplicationContext applicationContext) {
		return applicationContext.getBean(TestRibbonClient).callService('testInstance', 'port', Integer)
	}

	@Configuration
	@EnableDiscoveryClient
	@EnableAutoConfiguration
	@Import(DependencyConfig)
	static class Config {
	}

}