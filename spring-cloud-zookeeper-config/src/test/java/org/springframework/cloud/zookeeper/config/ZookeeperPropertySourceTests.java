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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.UUID;

import lombok.SneakyThrows;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.KeeperException;
import org.junit.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author Spencer Gibb
 */
public class ZookeeperPropertySourceTests {

	private ConfigurableEnvironment environment;
	public static final String PREFIX = "test__config__";
	public static final String ROOT = "/"+PREFIX + UUID.randomUUID();
	private ConfigurableApplicationContext context;

	static class Config {
	}

	CuratorFramework curator;

	ZookeeperConfigProperties properties;

	@SneakyThrows
	public void setup(boolean cacheEnabled) {
		curator = CuratorFrameworkFactory.builder()
				.retryPolicy(new RetryOneTime(500))
				.connectString(new ZookeeperProperties().getConnectString())
				.build();
		curator.start();

		List<String> children = curator.getChildren().forPath("/");
		for (String child : children) {
			if (child.startsWith(PREFIX) && child.length() > PREFIX.length()) {
				delete("/" + child);
			}
		}

		String key = ROOT + "/application/testProp";
		String create = curator.create().creatingParentsIfNeeded().forPath(key, "testPropVal".getBytes());
		curator.close();
		System.out.println(create);

		context = new SpringApplicationBuilder(Config.class)
				.web(false)
				.run("--spring.spring.application.name=testZkPropertySource",
						"--spring.cloud.zookeeper.config.cacheEnabled="+ cacheEnabled,
						"--spring.cloud.zookeeper.config.root="+ROOT);

		curator = context.getBean(CuratorFramework.class);
		properties = context.getBean(ZookeeperConfigProperties.class);
		environment = context.getEnvironment();

	}

	@SneakyThrows
	public void delete(String path) {
		try {
			curator.delete().deletingChildrenIfNeeded().forPath(path);
		} catch (KeeperException e) {
			if (e.code() != KeeperException.Code.NONODE) {
				throw e;
			}
		}
	}

	@SneakyThrows
	public void after() {
		try {
			delete(properties.getRoot());
		} finally {
			context.close();
		}
	}

	@Test
	public void propertyLoadedCached() {
		setup(true);
		String testProp = this.environment.getProperty("testProp");
		assertThat("testProp was wrong", testProp, is(equalTo("testPropVal")));
		after();
	}

	@Test
	public void propertyLoadedNoCache() {
		setup(false);
		String testProp = this.environment.getProperty("testProp");
		assertThat("testProp was wrong", testProp, is(equalTo("testPropVal")));
		after();
	}
}
