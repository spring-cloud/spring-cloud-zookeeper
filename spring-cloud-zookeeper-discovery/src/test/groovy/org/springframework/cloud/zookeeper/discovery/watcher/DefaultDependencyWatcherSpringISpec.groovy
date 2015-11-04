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
package org.springframework.cloud.zookeeper.discovery.watcher

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.test.TestingServer
import org.apache.curator.x.discovery.ServiceCache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.cloud.zookeeper.discovery.CustomZookeeperServiceDiscovery
import org.springframework.cloud.zookeeper.discovery.PollingUtils
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceDiscovery
import org.springframework.cloud.zookeeper.discovery.watcher.presence.DependencyPresenceOnStartupVerifier
import org.springframework.cloud.zookeeper.discovery.watcher.presence.LogMissingDependencyChecker
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.SocketUtils
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@ContextConfiguration(classes = Config, loader = SpringApplicationContextLoader)
@ActiveProfiles('watcher')
class DefaultDependencyWatcherSpringISpec extends Specification implements PollingUtils {

	@Autowired AssertableDependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier
	@Autowired AssertableDependencyWatcherListener dependencyWatcherListener
	@Autowired ZookeeperServiceDiscovery serviceDiscovery
	PollingConditions conditions

	def setup() {
		conditions = new PollingConditions()
	}

	def 'should verify that presence of a dependency has been checked'() {
		expect:
			dependencyPresenceOnStartupVerifier.startupPresenceVerified
	}

	def 'should verify that dependency watcher listener is successfully registered and operational'() {
		when:
			serviceDiscovery.serviceDiscovery.unregisterService(serviceDiscovery.serviceInstance)

		then:
			conditions.eventually willPass {
				assert dependencyWatcherListener.dependencyState == DependencyState.DISCONNECTED
			}
	}

	@Configuration
	@EnableAutoConfiguration
	@Profile('watcher')
	static class Config {

		@Bean
		static PropertySourcesPlaceholderConfigurer propertiesConfigurer() {
			return new PropertySourcesPlaceholderConfigurer()
		}

		@Bean(destroyMethod = 'close')
		TestingServer testingServer() {
			return new TestingServer(SocketUtils.findAvailableTcpPort())
		}

		@Bean
		ZookeeperServiceDiscovery zookeeperServiceDiscovery() {
			return new MyZookeeperServiceDiscovery(curatorFramework())
		}

		@Bean(initMethod = 'start', destroyMethod = 'close')
		CuratorFramework curatorFramework() {
			return CuratorFrameworkFactory.newClient(testingServer().connectString, new ExponentialBackoffRetry(20, 20, 500))
		}

		@Bean
		DependencyWatcherListener dependencyWatcherListener() {
			return new AssertableDependencyWatcherListener()
		}

		@Bean
		DependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier() {
			return new AssertableDependencyPresenceOnStartupVerifier()
		}

	}

	static class MyZookeeperServiceDiscovery extends CustomZookeeperServiceDiscovery {
		MyZookeeperServiceDiscovery(CuratorFramework curator) {
			super('testInstance', curator)
		}
	}

	static class AssertableDependencyWatcherListener implements DependencyWatcherListener {

		DependencyState dependencyState = DependencyState.CONNECTED

		@Override
		void stateChanged(String dependencyName, DependencyState newState) {
			dependencyState = newState
		}
	}

	static class AssertableDependencyPresenceOnStartupVerifier extends DependencyPresenceOnStartupVerifier {

		boolean startupPresenceVerified = false

		AssertableDependencyPresenceOnStartupVerifier() {
			super(new LogMissingDependencyChecker())
		}

		@Override
		void verifyDependencyPresence(String dependencyName, ServiceCache serviceCache, boolean required) {
			startupPresenceVerified = true
		}
	}
}
