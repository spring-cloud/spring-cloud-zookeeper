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

package org.springframework.cloud.zookeeper.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Enrique Recarte Llorens
 */
public class ZookeeperPropertySourceLocatorFailFastTests {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		// This system property makes Curator fail faster, otherwise it takes 15 seconds to trigger a retry
		System.setProperty("curator-default-connection-timeout", "0");
	}

	@After
	public void tearDown() throws Exception {
		System.clearProperty("curator-default-connection-timeout");
	}

	@Test
	public void testFailFastFalseLoadsTheApplicationContext() throws Exception {
		new SpringApplicationBuilder()
			.sources(Config.class)
			.web(WebApplicationType.NONE)
			.run(
				"--spring.application.name=testZookeeperPropertySourceLocatorFailFast",
				"--spring.cloud.zookeeper.config.connectString=localhost:2188",
				"--spring.cloud.zookeeper.baseSleepTimeMs=0",
				"--spring.cloud.zookeeper.maxRetries=0",
				"--spring.cloud.zookeeper.maxSleepMs=0",
				"--spring.cloud.zookeeper.blockUntilConnectedWait=0",
				"--spring.cloud.zookeeper.config.failFast=false"
			);
	}

	@Test
	public void testFailFastTrueDoesNotLoadTheApplicationContext() throws Exception {
		expectedException.expect(Exception.class);

		new SpringApplicationBuilder()
			.sources(Config.class)
			.web(WebApplicationType.NONE)
			.run(
				"--spring.application.name=testZookeeperPropertySourceLocatorFailFast",
				"--spring.cloud.zookeeper.config.connectString=localhost:2188",
				"--spring.cloud.zookeeper.baseSleepTimeMs=0",
				"--spring.cloud.zookeeper.maxRetries=0",
				"--spring.cloud.zookeeper.maxSleepMs=0",
				"--spring.cloud.zookeeper.blockUntilConnectedWait=0",
				"--spring.cloud.zookeeper.config.failFast=true"
			);
	}

	@SpringBootApplication
	static class Config {
	}
}