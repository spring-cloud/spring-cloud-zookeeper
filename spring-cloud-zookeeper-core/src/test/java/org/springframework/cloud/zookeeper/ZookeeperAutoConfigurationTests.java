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

package org.springframework.cloud.zookeeper;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.drivers.TracerDriver;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.ensemble.fixed.FixedEnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Marcin Grzejszczak
 */

@RunWith(Enclosed.class)
public class ZookeeperAutoConfigurationTests {

	@RunWith(SpringRunner.class)
	@ContextConfiguration(loader = ZookeeperTestingServer.Loader.class,
			classes = { BaseTestConfig.class, ZookeeperAutoConfiguration.class })
	public static class BaseTests {

		@Autowired(required = false)
		CuratorFramework curator;

		@Test
		public void should_successfully_inject_Curator_as_a_Spring_bean() {
			assertThat(this.curator).isNotNull();
		}

	}

	static class BaseTestConfig {

		@Bean
		ZookeeperProperties zookeeperProperties(TestingServer testingServer) {
			ZookeeperProperties properties = new ZookeeperProperties();
			properties.setConnectString(testingServer.getConnectString());
			return properties;
		}

		@Bean(destroyMethod = "close")
		TestingServer testingServer() throws Exception {
			return new TestingServer();
		}

	}

	@RunWith(SpringRunner.class)
	@ContextConfiguration(loader = ZookeeperTestingServer.Loader.class,
			classes = { EnsembleTestConfig.class, ZookeeperAutoConfiguration.class })
	public static class EnsembleTests {

		@Autowired(required = false)
		CuratorFramework curator;

		@Autowired
		TestingServer testingServer;

		@Test
		public void should_successfully_inject_Curator_with_ensemble_connection_string() {
			assertThat(curator.getZookeeperClient().getCurrentConnectionString())
					.isEqualTo(testingServer.getConnectString());
			assertThat(curator.getZookeeperClient().getCurrentConnectionString())
					.isNotEqualTo(EnsembleTestConfig.DUMMY_CONNECTION_STRING);
		}

	}

	static class EnsembleTestConfig {

		static final String DUMMY_CONNECTION_STRING = "dummy-connection-string:2111";

		@Bean
		EnsembleProvider ensembleProvider(TestingServer testingServer) {
			return new FixedEnsembleProvider(testingServer.getConnectString());
		}

		@Bean
		ZookeeperProperties zookeeperProperties() {
			ZookeeperProperties properties = new ZookeeperProperties();
			properties.setConnectString(DUMMY_CONNECTION_STRING);
			return properties;
		}

		@Bean(destroyMethod = "close")
		TestingServer testingServer() throws Exception {
			return new TestingServer();
		}

	}

	@RunWith(SpringRunner.class)
	@ContextConfiguration(loader = ZookeeperTestingServer.Loader.class,
			classes = { BaseTestConfig.class,
			ZookeeperAutoConfiguration.class, CuratorFrameworkCustomizerConfig.class })
	@DirtiesContext
	public static class CuratorFrameworkCustomizerTest {

		@Autowired
		@Qualifier("customizerCallOrder")
		List<Integer> callOrder;

		@Test
		public void should_invoke_the_CuratorFrameworkCustomizer_in_order() {
			assertThat(callOrder).containsExactly(1, 2);
		}

	}

	static class CuratorFrameworkCustomizerConfig {

		@Bean("customizerCallOrder")
		public List<Integer> curatorFrameworkCustomizerCallOrder() {
			return new ArrayList<>(2);
		}

		@Bean
		@Order(1)
		public CuratorFrameworkCustomizer customizer1(
				@Qualifier("customizerCallOrder") List<Integer> callOrder) {
			return (builder) -> callOrder.add(1);
		}

		@Bean
		@Order(2)
		public CuratorFrameworkCustomizer customizer2(
				@Qualifier("customizerCallOrder") List<Integer> callOrder) {
			return (builder) -> callOrder.add(2);
		}

	}

	@RunWith(SpringRunner.class)
	@ContextConfiguration(loader = ZookeeperTestingServer.Loader.class,
			classes = { BaseTestConfig.class, TracerDriverTestConfig.class,
			ZookeeperAutoConfiguration.class })
	public static class TracerDriverTests {

		@Autowired(required = false)
		TracerDriver tracerDriver;

		@Autowired(required = false)
		CuratorFramework curator;

		@Autowired
		TestingServer testingServer;

		@Test
		public void should_successfully_inject_Curators_TracerDriver() {
			assertThat(curator.getZookeeperClient().getTracerDriver())
					.isEqualTo(tracerDriver);
		}

	}

	static class TracerDriverTestConfig {

		@Bean
		TracerDriver mockedTracerDriver() {
			return mock(TracerDriver.class);
		}

	}

}
