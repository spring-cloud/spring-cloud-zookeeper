/*
 * Copyright 2015-2020 the original author or authors.
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

package org.springframework.cloud.zookeeper.discovery.configclient;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.test.ClassPathExclusions;
import org.springframework.cloud.test.ModifiedClassPathRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.SocketUtils;

@RunWith(ModifiedClassPathRunner.class)
@ClassPathExclusions({ "spring-cloud-config-client-*.jar", "spring-cloud-config-server-*.jar" })
public class ZookeeperConfigServerBootstrapperNoConfigClientTests {

	@Test
	public void contextLoads() throws Exception {
		TestingServer testingServer = null;
		ConfigurableApplicationContext context = null;
		try {
			TomcatURLStreamHandlerFactory.disable();
			int port = SocketUtils.findAvailableTcpPort();
			testingServer = new TestingServer(port);
			context = new SpringApplicationBuilder(TestConfig.class).properties("--server.port=0",
					"spring.cloud.config.discovery.enabled=true", "spring.cloud.zookeeper.connect-string=localhost:" + port,
							"spring.cloud.service-registry.auto-registration.enabled=false")
					.run();
		}
		finally {
			if (context != null) {
				context.close();
			}
			if (testingServer != null) {
				testingServer.close();
			}
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestConfig {

	}

}
