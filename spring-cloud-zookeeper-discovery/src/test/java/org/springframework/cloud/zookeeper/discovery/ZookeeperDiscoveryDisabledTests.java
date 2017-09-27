/*
 * Copyright 2013-2017 the original author or authors.
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

import org.apache.curator.framework.CuratorFramework;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZookeeperDiscoveryDisabledTests.SomeApp.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {"spring.cloud.zookeeper.discovery.enabled=false", "debug=true"})
public class ZookeeperDiscoveryDisabledTests {

	@Test
	@Ignore //FIXME 2.0.0 error creating zookeeperHealthIndicator, CuratorFramework not found, but report says it is
	public void should_start_the_context_with_discovery_disabled() throws Exception {
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	static class SomeApp {
		@Bean
		CuratorFramework curator() {
			return null;
		}
	}
}
