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

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import static org.assertj.core.api.BDDAssertions.then;

@RunWith(Parameterized.class)
public class ZookeeperDiscoveryPropertiesTests {

	private String root;

	public ZookeeperDiscoveryPropertiesTests(String root) {
		this.root = root;
	}

	@Parameterized.Parameters(name = "With root {0}")
	public static Iterable<String> rootVariations() {
		return Arrays.asList("es", "es/", "/es");
	}

	@Test
	public void should_escape_root() {
		// given:
		ZookeeperDiscoveryProperties zookeeperDiscoveryProperties = new ZookeeperDiscoveryProperties(
				new InetUtils(new InetUtilsProperties()));
		// when:
		zookeeperDiscoveryProperties.setRoot(root);
		// then:
		then(zookeeperDiscoveryProperties.getRoot()).isEqualTo("/es");
	}

}
