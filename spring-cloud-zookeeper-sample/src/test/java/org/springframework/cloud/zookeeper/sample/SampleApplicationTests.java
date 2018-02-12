/*
 * Copyright 2013-2015 the original author or authors.
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

package org.springframework.cloud.zookeeper.sample;

import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.SocketUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class SampleApplicationTests {

	@Test public void contextLoads() throws Exception {
		int zkPort = SocketUtils.findAvailableTcpPort();
		TestingServer server = new TestingServer(zkPort);

		int port = SocketUtils.findAvailableTcpPort(zkPort+1);

		ConfigurableApplicationContext context = new SpringApplicationBuilder(SampleZookeeperApplication.class).run(
				"--server.port="+port,
				"--management.endpoints.web.exposure.include=*",
				"--spring.cloud.zookeeper.connect-string=localhost:" + zkPort);

		ResponseEntity<String> response = new TestRestTemplate().getForEntity("http://localhost:"+port+"/hi", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		context.close();
		server.close();
	}
}
