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

package org.springframework.cloud.zookeeper.discovery;


import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wmz7year
 */
@SpringBootTest(properties = { "spring.application.name=testZookeeperDiscovery",
		"spring.cloud.service-registry.auto-registration.enabled=false",
		"spring.cloud.zookeeper.discovery.instance-id=zkpropstestid-123",
		"spring.cloud.zookeeper.discovery.preferIpAddress=true",
		"spring.cloud.zookeeper.discovery.instanceIpAddress=1.1.1.1" },
		classes = ZookeeperDiscoveryPropertiesIntegrationTests.Config.class,
		webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(loader = ZookeeperTestingServer.Loader.class)
public class ZookeeperDiscoveryPropertiesIntegrationTests {

	@Autowired
	private ZookeeperDiscoveryProperties discoveryProperties;

	@Test
	public void testPreferIpAddress() {
		assertThat(this.discoveryProperties.getInstanceId())
				.isEqualTo("zkpropstestid-123");
		assertThat(this.discoveryProperties.getInstanceHost()).isEqualTo("1.1.1.1");
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	static class Config {

	}

}
