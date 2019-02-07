/*
 * Copyright 2015-2019 the original author or authors.
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

package org.springframework.cloud.zookeeper;

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
 * @author Marcin Grzejszczak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ZookeeperAutoConfigurationTests.TestConfig.class,
		ZookeeperAutoConfiguration.class })
public class ZookeeperAutoConfigurationTests {

	@Autowired(required = false)
	CuratorFramework curator;

	@Test
	public void should_successfully_inject_Curator_as_a_Spring_bean() {
		assertThat(this.curator).isNotNull();
	}

	static class TestConfig {

		@Bean
		ZookeeperProperties zookeeperProperties(TestingServer testingServer)
				throws Exception {
			ZookeeperProperties properties = new ZookeeperProperties();
			properties.setConnectString(testingServer.getConnectString());
			return properties;
		}

		@Bean(destroyMethod = "close")
		TestingServer testingServer() throws Exception {
			return new TestingServer();
		}

	}

}
