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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ConfigDataContextRefresher;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Spencer Gibb
 */
public class ZookeeperConfigDataIntegrationTests {

	private static final Log log = LogFactory
			.getLog(ZookeeperConfigDataIntegrationTests.class);

	public static final String APPLICATION_NAME = "testZkConfigDataIntegration";

	public static final String PREFIX = "test__configdata__";

	public static final String ROOT = "/" + PREFIX + UUID.randomUUID();

	public static final String CONTEXT = ROOT + "/application/";

	public static final String KEY_BASIC = "testProp";

	public static final String KEY_BASIC_PATH = CONTEXT + KEY_BASIC;

	public static final String KEY_APP_PATH = ROOT + "/" + APPLICATION_NAME + "/" + KEY_BASIC;

	public static final String VAL_BASIC_DEFAULT = "testPropValDefault";

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

	private CuratorFramework curator;
	private ZookeeperTestingServer testingServer;

	@Before
	public void setup() throws Exception {
		testingServer = new ZookeeperTestingServer();
		testingServer.start();
		String connectString = "localhost:" + testingServer.getPort();
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
				.forPath(KEY_BASIC_PATH, VAL_BASIC_DEFAULT.getBytes())).append('\n');
		create.append(this.curator.create().creatingParentsIfNeeded()
				.forPath(KEY_APP_PATH, VAL_BASIC.getBytes())).append('\n');
		create.append(this.curator.create().creatingParentsIfNeeded()
				.forPath(KEY_WITH_DOT_PATH, VAL_WITH_DOT.getBytes())).append('\n');
		create.append(this.curator.create().creatingParentsIfNeeded()
				.forPath(KEY_NESTED_PATH, VAL_NESTED.getBytes())).append('\n');
		create.append(this.curator.create().creatingParentsIfNeeded()
				.forPath(KEY_WITHOUT_VALUE_PATH, null)).append('\n');
		this.curator.close();
		System.out.println(create);

		this.context = new SpringApplicationBuilder(Config.class)
				.web(WebApplicationType.NONE)
				.run("--spring.config.import=zookeeper:" + connectString,
						"--spring.application.name=" + APPLICATION_NAME,
						"--logging.level.org.springframework.cloud.zookeeper=DEBUG",
						"--spring.cloud.zookeeper.config.root=" + ROOT);

		this.curator = this.context.getBean(CuratorFramework.class);
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
			delete(ROOT);
		}
		finally {
			this.context.close();
			this.testingServer.close();
		}
	}

	@Test
	public void checkKeyValues() {
		String propValue = this.environment.getProperty(KEY_BASIC);
		assertThat(propValue).as(KEY_BASIC + " was wrong").isEqualTo(VAL_BASIC);

		propValue = this.environment.getProperty(KEY_NESTED);
		assertThat(propValue).as(VAL_NESTED + " was wrong").isEqualTo(VAL_NESTED);

		propValue = this.environment.getProperty(KEY_WITH_DOT);
		assertThat(propValue).as(VAL_WITH_DOT + " was wrong").isEqualTo(VAL_WITH_DOT);

		propValue = this.environment.getProperty(KEY_WITHOUT_VALUE);
		assertThat(propValue).as(KEY_WITHOUT_VALUE + " was wrong").isEmpty();
	}

	@Test
	public void propertyLoadedAndUpdated() throws Exception {
		String testProp = this.environment.getProperty(KEY_BASIC);
		assertThat(testProp).as("testProp was wrong").isEqualTo(VAL_BASIC);

		this.curator.setData().forPath(KEY_APP_PATH, "testPropValUpdate".getBytes());

		CountDownLatch latch = this.context.getBean(CountDownLatch.class);
		boolean receivedEvent = latch.await(15, TimeUnit.SECONDS);
		assertThat(receivedEvent).as("listener didn't receive event").isTrue();

		testProp = this.environment.getProperty(KEY_BASIC);
		assertThat(testProp).as("testProp was wrong after update")
				.isEqualTo("testPropValUpdate");
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config implements ApplicationListener<EnvironmentChangeEvent> {

		@Bean
		public CountDownLatch countDownLatch() {
			return new CountDownLatch(1);
		}

		@Bean
		public ContextRefresher contextRefresher(ConfigurableApplicationContext context,
				RefreshScope scope) {
			return new ConfigDataContextRefresher(context, scope);
		}

		@Override
		public void onApplicationEvent(EnvironmentChangeEvent event) {
			log.debug("Event keys: " + event.getKeys());
			if (event.getKeys().contains(KEY_BASIC)) {
				countDownLatch().countDown();
			}
		}

	}

}
