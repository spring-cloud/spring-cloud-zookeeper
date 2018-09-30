/*
 * Copyright 2016-2018 the original author or authors.
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

package org.springframework.cloud.zookeeper.discovery;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wmz7year
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = {"pring.application.name=testZookeeperDiscovery",
		"spring.cloud.zookeeper.discovery.instance-id=zkpropstestid-123",
		"spring.cloud.zookeeper.discovery.preferIpAddress=true",
		"spring.cloud.zookeeper.discovery.instanceIpAddress=1.1.1.1"},
		classes = ZookeeperDiscoveryPropertiesTests.Config.class,
		webEnvironment = WebEnvironment.RANDOM_PORT)
public class ZookeeperDiscoveryPropertiesTests {

	@Autowired
	private ZookeeperDiscoveryProperties discoveryProperties;

	@Test
	public void testPreferIpAddress() {
		assertThat(this.discoveryProperties.getInstanceId()).isEqualTo("zkpropstestid-123");
		assertThat(this.discoveryProperties.getInstanceHost()).isEqualTo("1.1.1.1");
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	static class Config {
	}
}
