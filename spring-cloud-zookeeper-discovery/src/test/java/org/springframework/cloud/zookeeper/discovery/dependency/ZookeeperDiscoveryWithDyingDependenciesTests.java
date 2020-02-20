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

package org.springframework.cloud.zookeeper.discovery.dependency;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.test.TestingServer;
import org.assertj.core.api.BDDAssertions;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.zookeeper.discovery.test.TestLoadBalancedClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.SocketUtils;

import static com.jayway.awaitility.Awaitility.await;

/**
 * @author Marcin Grzejszczak
 */
@Ignore
public class ZookeeperDiscoveryWithDyingDependenciesTests {

	private static final Log log = LogFactory
			.getLog(ZookeeperDiscoveryWithDyingDependenciesTests.class);

	// Issue: #45
	@Test
	public void should_refresh_a_dependency_in_LoadBalancer_when_the_dependency_has_deregistered_and_registered_in_Zookeeper()
			throws Exception {
		ConfigurableApplicationContext serverContext = null;
		ConfigurableApplicationContext clientContext = null;
		TestingServer testingServer = null;
		try {
			// given:
			int zookeeperPort = SocketUtils.findAvailableTcpPort();
			testingServer = new TestingServer(zookeeperPort);
			System.setProperty("spring.jmx.enabled", "false");
			System.setProperty("spring.cloud.zookeeper.connectString",
					"127.0.0.1:" + zookeeperPort);
			// and:
			serverContext = contextWithProfile("server");
			clientContext = contextWithProfile("client");
			// and:
			Integer serverPortBeforeDying = callServiceAtPortEndpoint(clientContext);
			// and:
			serverContext = restartContext(serverContext, "server");
			// expect:
			await().atMost(5, TimeUnit.SECONDS).until(applicationHasStartedOnANewPort(
					clientContext, serverPortBeforeDying));
		}
		finally {
			// cleanup:
			close(serverContext);
			close(clientContext);
			close(testingServer);
		}
	}

	private Callable<Boolean> applicationHasStartedOnANewPort(
			final ConfigurableApplicationContext clientContext,
			final Integer serverPortBeforeDying) {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try {
					BDDAssertions.then(callServiceAtPortEndpoint(clientContext))
							.isNotEqualTo(serverPortBeforeDying);
				}
				catch (Exception e) {
					log.error("Exception occurred while trying to call the server", e);
					return false;
				}
				return true;
			}
		};
	}

	private void close(Closeable closeable) throws IOException {
		if (closeable != null) {
			closeable.close();
		}
	}

	private ConfigurableApplicationContext contextWithProfile(String profile) {
		return new SpringApplicationBuilder(Config.class).profiles(profile).build().run();
	}

	private ConfigurableApplicationContext restartContext(
			ConfigurableApplicationContext configurableApplicationContext, String profile)
			throws IOException {
		close(configurableApplicationContext);
		return contextWithProfile(profile);
	}

	private Integer callServiceAtPortEndpoint(ApplicationContext applicationContext) {
		return applicationContext.getBean(TestLoadBalancedClient.class)
				.callService("testInstance", "port", Integer.class);
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(DependencyConfig.class)
	static class Config {

	}

}
