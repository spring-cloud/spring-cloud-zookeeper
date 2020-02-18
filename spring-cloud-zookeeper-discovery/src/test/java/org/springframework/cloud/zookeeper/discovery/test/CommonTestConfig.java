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

package org.springframework.cloud.zookeeper.discovery.test;

import org.apache.curator.test.TestingServer;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

/**
 *
 */
@Configuration
public class CommonTestConfig {

	@Bean
	@LoadBalanced
	RestTemplate loadBalancedRestTemplate() {
		return new RestTemplate();
	}

	@Bean(destroyMethod = "close")
	TestingServer testingServer() throws Exception {
		return new TestingServer(SocketUtils.findAvailableTcpPort());
	}

	@Bean
	ZookeeperProperties zookeeperProperties(TestingServer testingServer) {
		ZookeeperProperties zookeeperProperties = new ZookeeperProperties();
		zookeeperProperties.setConnectString("localhost:" + testingServer.getPort());
		return zookeeperProperties;
	}

}
