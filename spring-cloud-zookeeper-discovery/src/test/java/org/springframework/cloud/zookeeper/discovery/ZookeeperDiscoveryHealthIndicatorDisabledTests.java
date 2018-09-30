/*
 * Copyright 2017-2018 the original author or authors.
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
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
		properties = "management.health.zookeeper.enabled=false")
public class ZookeeperDiscoveryHealthIndicatorDisabledTests {

	@Autowired(required = false)
	private ZookeeperDiscoveryHealthIndicator healthIndicator;

	// Issue: #101 - ZookeeperDiscoveryHealthIndicator should be able to be disabled with a property
	@Test
	public void healthIndicatorDisabled() {
		// when:
		// then:
		assertThat(this.healthIndicator).isNull();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	static class Config {
	}

}
