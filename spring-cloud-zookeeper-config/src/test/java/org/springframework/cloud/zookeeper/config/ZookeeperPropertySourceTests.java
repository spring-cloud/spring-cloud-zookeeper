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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.junit.Before;
import org.junit.Test;

import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link org.springframework.core.env.PropertySource} that stores properties
 * from Zookeeper inside a map. Properties are loaded upon class initialization.
 *
 * @author lemonJ
 * @since 1.0.0
 */
public class ZookeeperPropertySourceTests {

	private static final Log log = LogFactory.getLog(ZookeeperPropertySourceTests.class);

	public static final String PREFIX = "test__config__";

	public static final String ROOT = "/" + PREFIX + UUID.randomUUID();

	private ZookeeperTestingServer testingServer;

	private CuratorFramework curator;

	@Before
	public void setup() throws Exception {
		this.testingServer = new ZookeeperTestingServer();
		testingServer.start();
		String connectString = "localhost:" + testingServer.getPort();
		this.curator = CuratorFrameworkFactory.builder().retryPolicy(new RetryOneTime(500)).connectString(connectString)
				.build();
		this.curator.start();
	}

	@Test
	public void loadConfigInParallel() throws Exception {
		for (int i = 0; i < 1000; i++) {
			String path = ROOT + "/" + i;
			this.curator.create().creatingParentsIfNeeded().forPath(path);
			this.curator.setData().forPath(path, "testPropValUpdate".getBytes());
		}

		ZookeeperConfigProperties properties = new ZookeeperConfigProperties();
		properties.setParallel(true);
		ZookeeperPropertySource parallelPropertySource = new ZookeeperPropertySource(ROOT, curator, properties);

		properties.setParallel(false);
		ZookeeperPropertySource serialPropertySource = new ZookeeperPropertySource(ROOT, curator, properties);

		Field propertiesField = ZookeeperPropertySource.class.getDeclaredField("properties");
		propertiesField.setAccessible(true);
		Map<String, String> serialPropertyMap = (Map<String, String>) ReflectionUtils.getField(propertiesField,
				serialPropertySource);
		Map<String, String> parallelPropertyMap = (Map<String, String>) ReflectionUtils.getField(propertiesField,
				parallelPropertySource);
		assertThat(parallelPropertyMap).as("parallelPropertyMap").containsAllEntriesOf(serialPropertyMap);
	}
}
