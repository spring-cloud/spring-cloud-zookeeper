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

import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.ensemble.fixed.FixedEnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Konrad Kamil Dobrzyński
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		ZookeeperAutoConfigurationEnsembleTests.TestConfig.class,
		ZookeeperAutoConfiguration.class })
public class ZookeeperAutoConfigurationEnsembleTests {

	@Autowired(required = false)
	CuratorFramework curator;

	@Autowired
	TestingServer testingServer;

	@Test
	public void should_successfully_inject_Curator_with_ensemble_connection_string() {
		assertThat(curator.getZookeeperClient().getCurrentConnectionString())
				.isEqualTo(testingServer.getConnectString());
		assertThat(curator.getZookeeperClient().getCurrentConnectionString())
				.isNotEqualTo(TestConfig.DUMMY_CONNECTION_STRING);
	}

	static class TestConfig {

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

}
