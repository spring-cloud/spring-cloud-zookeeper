/*
 * Copyright 2015-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.zookeeper.discovery.watcher;

import java.util.concurrent.Callable;

import com.jayway.awaitility.Awaitility;
import org.apache.curator.x.discovery.ServiceCache;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.zookeeper.discovery.watcher.presence.DependencyPresenceOnStartupVerifier;
import org.springframework.cloud.zookeeper.discovery.watcher.presence.LogMissingDependencyChecker;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;
import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DefaultDependencyWatcherSpringTests.Config.class, webEnvironment = RANDOM_PORT)
@ContextConfiguration(loader = ZookeeperTestingServer.Loader.class)
@ActiveProfiles("watcher")
public class DefaultDependencyWatcherSpringTests {

	@Autowired
	AssertableDependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier;

	@Autowired
	AssertableDependencyWatcherListener dependencyWatcherListener;

	@Autowired
	ZookeeperRegistration zookeeperRegistration;

	@Autowired
	ZookeeperServiceRegistry registry;

	@Test
	public void should_verify_that_presence_of_a_dependency_has_been_checked() {
		then(this.dependencyPresenceOnStartupVerifier.startupPresenceVerified).isTrue();
	}

	@Ignore // FIXME 2.0.0
	@Test
	public void should_verify_that_dependency_watcher_listener_is_successfully_registered_and_operational()
			throws Exception {
		// when:
		this.registry.deregister(this.zookeeperRegistration);

		// then:
		Awaitility.await().until(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				then(DefaultDependencyWatcherSpringTests.this.dependencyWatcherListener.dependencyState)
						.isEqualTo(DependencyState.DISCONNECTED);
				return true;
			}
		});
	}

	@Configuration
	@EnableAutoConfiguration
	@Profile("watcher")
	static class Config {

		@Bean
		@LoadBalanced
		RestTemplate loadBalancedRestTemplate() {
			return new RestTemplate();
		}

		@Bean
		static PropertySourcesPlaceholderConfigurer propertiesConfigurer() {
			return new PropertySourcesPlaceholderConfigurer();
		}

		@Bean
		DependencyWatcherListener dependencyWatcherListener() {
			return new AssertableDependencyWatcherListener();
		}

		@Bean
		DependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier() {
			return new AssertableDependencyPresenceOnStartupVerifier();
		}

	}

	static class AssertableDependencyWatcherListener
			implements DependencyWatcherListener {

		DependencyState dependencyState = DependencyState.CONNECTED;

		@Override
		public void stateChanged(String dependencyName, DependencyState newState) {
			dependencyState = newState;
		}

	}

	static class AssertableDependencyPresenceOnStartupVerifier
			extends DependencyPresenceOnStartupVerifier {

		boolean startupPresenceVerified = false;

		AssertableDependencyPresenceOnStartupVerifier() {
			super(new LogMissingDependencyChecker());
		}

		@Override
		public void verifyDependencyPresence(String dependencyName,
				ServiceCache serviceCache, boolean required) {
			startupPresenceVerified = true;
		}

	}

}
