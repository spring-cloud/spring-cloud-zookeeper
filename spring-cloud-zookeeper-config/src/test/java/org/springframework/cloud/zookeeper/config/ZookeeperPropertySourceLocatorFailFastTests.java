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

package org.springframework.cloud.zookeeper.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Enrique Recarte Llorens
 * @author Olga Maciaszek-Sharma
 */
public class ZookeeperPropertySourceLocatorFailFastTests {

	@Before
	public void setUp() {
		// This system property makes Curator fail faster, otherwise it takes 15 seconds
		// to trigger a retry
		System.setProperty("curator-default-connection-timeout", "0");
	}

	@After
	public void tearDown() {
		System.clearProperty("curator-default-connection-timeout");
	}

	@Test
	public void testFailFastFalseLoadsTheApplicationContext() {
		assertThatCode(() -> {
			new SpringApplicationBuilder().sources(Config.class)
					.web(WebApplicationType.NONE)
					.run("--spring.application.name=testZookeeperPropertySourceLocatorFailFast",
							"--spring.config.use-legacy-processing=true",
							"--spring.cloud.zookeeper.config.connectString=localhost:2188",
							"--spring.cloud.zookeeper.baseSleepTimeMs=0",
							"--spring.cloud.zookeeper.maxRetries=0",
							"--spring.cloud.zookeeper.maxSleepMs=0",
							"--spring.cloud.zookeeper.blockUntilConnectedWait=0",
							"--spring.cloud.zookeeper.config.failFast=false");
		}).doesNotThrowAnyException();
	}

	@Test
	public void testFailFastTrueDoesNotLoadTheApplicationContext() {
		assertThatThrownBy(() -> {
			new SpringApplicationBuilder().sources(Config.class)
					.web(WebApplicationType.NONE)
					.run("--spring.application.name=testZookeeperPropertySourceLocatorFailFast",
							"--spring.config.use-legacy-processing=true",
							"--spring.cloud.zookeeper.config.connectString=localhost:2188",
							"--spring.cloud.zookeeper.baseSleepTimeMs=0",
							"--spring.cloud.zookeeper.maxRetries=0",
							"--spring.cloud.zookeeper.maxSleepMs=0",
							"--spring.cloud.zookeeper.blockUntilConnectedWait=0",
							"--spring.cloud.zookeeper.config.failFast=true");
		})
				.isNotNull();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class Config {

	}

}
