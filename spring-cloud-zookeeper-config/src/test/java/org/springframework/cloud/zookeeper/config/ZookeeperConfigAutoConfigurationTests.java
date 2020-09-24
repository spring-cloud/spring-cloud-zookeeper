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

import org.apache.zookeeper.KeeperException;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Cesar Aguilera
 */
public class ZookeeperConfigAutoConfigurationTests {

	@Rule
	public ExpectedException expectedException;

	@Before
	public void setUp() throws Exception {
		expectedException = ExpectedException.none();
		// makes Curator fail faster, otherwise it takes 15 seconds to trigger a retry
		System.setProperty("curator-default-connection-timeout", "0");
	}

	@After
	public void tearDown() throws Exception {
		System.clearProperty("curator-default-connection-timeout");
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void testConfigEnabledFalseDoesNotLoadZookeeperConfigAutoConfiguration() {
		ConfigurableApplicationContext context = new SpringApplicationBuilder()
				.listeners(new ZookeeperTestingServer())
				.sources(Config.class).web(WebApplicationType.NONE)
				.run("--spring.application.name=testZookeeperConfigEnabledSetToFalse",
						"--spring.config.use-legacy-processing=true",
						"--spring.jmx.default-domain=testZookeeperConfigEnabledSetToFalse",
						"--spring.cloud.zookeeper.config.connectString=localhost:2188",
						"--spring.cloud.zookeeper.baseSleepTimeMs=0",
						"--spring.cloud.zookeeper.maxRetries=0",
						"--spring.cloud.zookeeper.maxSleepMs=0",
						"--spring.cloud.zookeeper.blockUntilConnectedWait=0",
						"--spring.cloud.zookeeper.config.failFast=false",
						"--spring.cloud.zookeeper.config.enabled=false");

		context.getBean(ZookeeperConfigAutoConfiguration.class);
	}

	@Test
	public void testConfigEnabledTrueLoadsZookeeperConfigAutoConfiguration()
			throws Exception {
		expectedException.expectCause(Matchers.isA(KeeperException.class));

		new SpringApplicationBuilder().sources(Config.class).web(WebApplicationType.NONE)
				.listeners(new ZookeeperTestingServer())
				.run("--spring.application.name=testZookeeperConfigEnabledSetToTrue",
						"--spring.config.use-legacy-processing=true",
						"--spring.jmx.default-domain=testZookeeperConfigEnabledSetToTrue",
						"--spring.cloud.zookeeper.config.connectString=localhost:2188",
						"--spring.cloud.zookeeper.baseSleepTimeMs=0",
						"--spring.cloud.zookeeper.maxRetries=0",
						"--spring.cloud.zookeeper.maxSleepMs=0",
						"--spring.cloud.zookeeper.blockUntilConnectedWait=0",
						"--spring.cloud.zookeeper.config.failFast=false",
						"--spring.cloud.zookeeper.config.enabled=true");
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class Config {

	}

}
