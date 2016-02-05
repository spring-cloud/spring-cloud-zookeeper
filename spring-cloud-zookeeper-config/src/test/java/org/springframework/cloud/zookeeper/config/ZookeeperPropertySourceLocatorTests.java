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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;

import lombok.SneakyThrows;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Spencer Gibb
 */
public class ZookeeperPropertySourceLocatorTests {

	private ConfigurableEnvironment environment;
	public static final String PREFIX = "test__config__";
	public static final String ROOT = "/"+PREFIX + UUID.randomUUID();
	private ConfigurableApplicationContext context;
	public static final String KEY = ROOT + "/application/testProp";

	@Configuration
	@EnableAutoConfiguration
	static class Config {
		@Bean
		public TestEventListener testEventListener() {
			return new TestEventListener();
		}
	}

	static class TestEventListener {

		private CountDownLatch latch = new CountDownLatch(1);

		@EventListener
		public void handle(ZookeeperConfigRefreshEvent event) {
			if (event.getEvent().getData().getPath().equals(KEY)) {
				this.latch.countDown();
			}
		}

		public CountDownLatch getLatch() {
			return latch;
		}
	}

	CuratorFramework curator;

	ZookeeperConfigProperties properties;

	@Before
	@SneakyThrows
	public void setup() {
		this.curator = CuratorFrameworkFactory.builder()
				.retryPolicy(new RetryOneTime(500))
				.connectString(new ZookeeperProperties().getConnectString())
				.build();
		this.curator.start();

		List<String> children = this.curator.getChildren().forPath("/");
		for (String child : children) {
			if (child.startsWith(PREFIX) && child.length() > PREFIX.length()) {
				delete("/" + child);
			}
		}

		String create = this.curator.create().creatingParentsIfNeeded().forPath(KEY, "testPropVal".getBytes());
		this.curator.close();
		System.out.println(create);

		this.context = new SpringApplicationBuilder(Config.class)
				.web(false)
				.run("--spring.spring.application.name=testZkPropertySource",
						"--spring.cloud.zookeeper.config.root="+ROOT);

		this.curator = context.getBean(CuratorFramework.class);
		this.properties = context.getBean(ZookeeperConfigProperties.class);
		this.environment = context.getEnvironment();
	}

	@SneakyThrows
	public void delete(String path) {
		try {
			this.curator.delete().deletingChildrenIfNeeded().forPath(path);
		} catch (KeeperException e) {
			if (e.code() != KeeperException.Code.NONODE) {
				throw e;
			}
		}
	}

	@After
	@SneakyThrows
	public void after() {
		try {
			delete(properties.getRoot());
		} finally {
			context.close();
		}
	}

	@Test
	public void propertyLoadedAndUpdated() throws Exception {
		String testProp = this.environment.getProperty("testProp");
		assertThat("testProp was wrong", testProp, is(equalTo("testPropVal")));

		this.curator.setData().forPath(KEY, "testPropValUpdate".getBytes());

		TestEventListener listener = this.context.getBean(TestEventListener.class);
		boolean receivedEvent = listener.getLatch().await(1500, TimeUnit.MILLISECONDS);
		assertThat("listener didn't receive event", receivedEvent, is(true));
	}
}
