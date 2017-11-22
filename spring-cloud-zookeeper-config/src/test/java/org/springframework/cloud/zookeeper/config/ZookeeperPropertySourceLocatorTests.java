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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.SocketUtils;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

/**
 * @author Spencer Gibb
 */
public class ZookeeperPropertySourceLocatorTests {

	private static final Log log = LogFactory.getLog(ZookeeperPropertySourceLocatorTests.class);

	public static final String PREFIX = "test__config__";
	public static final String ROOT = "/" + PREFIX + UUID.randomUUID();
	public static final String CONTEXT = ROOT + "/application/";

	public static final String KEY_BASIC = "testProp";
	public static final String KEY_BASIC_PATH = CONTEXT + KEY_BASIC;
	public static final String VAL_BASIC = "testPropVal";

	public static final String KEY_WITH_DOT = "testProp.dot";
	public static final String KEY_WITH_DOT_PATH = CONTEXT + KEY_WITH_DOT;
	public static final String VAL_WITH_DOT = "withDotVal";

	public static final String KEY_NESTED = "testProp.nested";
	public static final String KEY_NESTED_PATH = CONTEXT + KEY_NESTED.replace('.', '/');
	public static final String VAL_NESTED = "nestedVal";

	public static final String KEY_WITHOUT_VALUE = "testProp.novalue";
	public static final String KEY_WITHOUT_VALUE_PATH = CONTEXT + KEY_WITHOUT_VALUE;

	private ConfigurableEnvironment environment;
	private ConfigurableApplicationContext context;
	private TestingServer testingServer;
	private CuratorFramework curator;
	private ZookeeperConfigProperties properties;

	@Configuration
	@EnableAutoConfiguration
	static class Config {
		private AtomicBoolean ready = new AtomicBoolean(false);

		@Bean
		public CountDownLatch countDownLatch() {
			return new CountDownLatch(1);
		}

		@Bean
		public ContextRefresher contextRefresher(ConfigurableApplicationContext context,
				RefreshScope scope) {
			return new ContextRefresher(context, scope);
		}

		@EventListener
		public void handle(EnvironmentChangeEvent event) {
			log.debug("Event keys: " + event.getKeys());
			if (event.getKeys().contains(KEY_BASIC)) {
				countDownLatch().countDown();
			}
		}
	}

	@Before
	public void setup() throws Exception {
		int port = SocketUtils.findAvailableTcpPort();
		this.testingServer = new TestingServer(port);
		String connectString = "localhost:" + port;
		this.curator = CuratorFrameworkFactory.builder()
				.retryPolicy(new RetryOneTime(500)).connectString(connectString).build();
		this.curator.start();

		List<String> children = this.curator.getChildren().forPath("/");
		for (String child : children) {
			if (child.startsWith(PREFIX) && child.length() > PREFIX.length()) {
				delete("/" + child);
			}
		}

		StringBuilder create = new StringBuilder(1024);
		create.append(this.curator.create().creatingParentsIfNeeded()
				.forPath(KEY_BASIC_PATH, VAL_BASIC.getBytes())).append('\n');
		create.append(this.curator.create().creatingParentsIfNeeded()
				.forPath(KEY_WITH_DOT_PATH, VAL_WITH_DOT.getBytes())).append('\n');
		create.append(this.curator.create().creatingParentsIfNeeded()
				.forPath(KEY_NESTED_PATH, VAL_NESTED.getBytes())).append('\n');
		create.append(this.curator.create().creatingParentsIfNeeded()
				.forPath(KEY_WITHOUT_VALUE_PATH, null)).append('\n');
		this.curator.close();
		System.out.println(create);

		this.context = new SpringApplicationBuilder(Config.class).web(false).run(
				"--spring.cloud.zookeeper.connectString=" + connectString,
				"--spring.application.name=testZkPropertySource", "--logging.level.org.springframework.cloud.zookeeper=DEBUG",
				"--spring.cloud.zookeeper.config.root=" + ROOT);

		this.curator = this.context.getBean(CuratorFramework.class);
		this.properties = this.context.getBean(ZookeeperConfigProperties.class);
		this.environment = this.context.getEnvironment();
	}

	public void delete(String path) throws Exception {
		try {
			this.curator.delete().deletingChildrenIfNeeded().forPath(path);
		}
		catch (KeeperException e) {
			if (e.code() != KeeperException.Code.NONODE) {
				throw e;
			}
		}
	}

	@After
	public void after() throws Exception {
		try {
			delete(this.properties.getRoot());
		}
		finally {
			this.context.close();
			this.testingServer.close();
		}
	}

	@Test
	public void checkKeyValues() throws Exception {
		String propValue = this.environment.getProperty(KEY_BASIC);
		assertThat(KEY_BASIC + " was wrong", propValue, is(equalTo(VAL_BASIC)));

		propValue = this.environment.getProperty(KEY_NESTED);
		assertThat(VAL_NESTED + " was wrong", propValue, is(equalTo(VAL_NESTED)));

		propValue = this.environment.getProperty(KEY_WITH_DOT);
		assertThat(VAL_WITH_DOT + " was wrong", propValue, is(equalTo(VAL_WITH_DOT)));

		propValue = this.environment.getProperty(KEY_WITHOUT_VALUE);
		assertThat(KEY_WITHOUT_VALUE + " was wrong", propValue, is(isEmptyString()));
	}

	@Test
	public void propertyLoadedAndUpdated() throws Exception {
		String testProp = this.environment.getProperty(KEY_BASIC);
		assertThat("testProp was wrong", testProp, is(equalTo(VAL_BASIC)));

		this.curator.setData().forPath(KEY_BASIC_PATH, "testPropValUpdate".getBytes());

		CountDownLatch latch = this.context.getBean(CountDownLatch.class);
		boolean receivedEvent = latch.await(15, TimeUnit.SECONDS);
		assertThat("listener didn't receive event", receivedEvent, is(true));

		testProp = this.environment.getProperty(KEY_BASIC);
		assertThat("testProp was wrong after update", testProp,
				is(equalTo("testPropValUpdate")));
	}
}
