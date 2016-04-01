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
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.SocketUtils;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Spencer Gibb
 */
public class ZookeeperPropertySourceLocatorTests {

	private ConfigurableEnvironment environment;
	public static final String PREFIX = "test__config__";
	public static final String ROOT = "/" + PREFIX + UUID.randomUUID();
	private ConfigurableApplicationContext context;
	public static final String KEY = ROOT + "/application/testProp";
	private TestingServer testingServer;

	@Configuration
	@EnableAutoConfiguration
	static class Config {
		@Bean
		public CountDownLatch countDownLatch() {
			return new CountDownLatch(1);
		}

		@Bean
		public RefreshEndpoint refreshEndpoint(ConfigurableApplicationContext context,
				RefreshScope scope) {
			return new TestRefreshEndpoint(context, scope, countDownLatch());
		}
	}

	static class TestRefreshEndpoint extends RefreshEndpoint {
		private CountDownLatch latch;

		public TestRefreshEndpoint( ConfigurableApplicationContext context, RefreshScope scope, CountDownLatch latch) {
			super(new ContextRefresher(context, scope));
			this.latch = latch;
		}

		@Override
		public synchronized String[] refresh() {
			String[] keys = super.refresh();
			this.latch.countDown();
			return keys;
		}
	}

	CuratorFramework curator;

	ZookeeperConfigProperties properties;

	@Before
	public void setup() throws Exception {
		int port = SocketUtils.findAvailableTcpPort();
		this.testingServer = new TestingServer(port);
		String connectString = "localhost:" + port;
		this.curator = CuratorFrameworkFactory.builder()
				.retryPolicy(new RetryOneTime(500))
				.connectString(connectString)
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
				.run("--spring.cloud.zookeeper.connectString=" + connectString,
						"--spring.application.name=testZkPropertySource",
						"--spring.cloud.zookeeper.config.root="+ROOT);

		this.curator = this.context.getBean(CuratorFramework.class);
		this.properties = this.context.getBean(ZookeeperConfigProperties.class);
		this.environment = this.context.getEnvironment();
	}

	public void delete(String path) throws Exception {
		try {
			this.curator.delete().deletingChildrenIfNeeded().forPath(path);
		} catch (KeeperException e) {
			if (e.code() != KeeperException.Code.NONODE) {
				throw e;
			}
		}
	}

	@After
	public void after() throws Exception {
		try {
			delete(this.properties.getRoot());
		} finally {
			this.context.close();
		}
	}

	@Test
	public void propertyLoadedAndUpdated() throws Exception {
		String testProp = this.environment.getProperty("testProp");
		assertThat("testProp was wrong", testProp, is(equalTo("testPropVal")));

		this.curator.setData().forPath(KEY, "testPropValUpdate".getBytes());

		CountDownLatch latch = this.context.getBean(CountDownLatch.class);
		boolean receivedEvent = latch.await(15, TimeUnit.SECONDS);
		assertThat("listener didn't receive event", receivedEvent, is(true));

		testProp = this.environment.getProperty("testProp");
		assertThat("testProp was wrong after update", testProp, is(equalTo("testPropValUpdate")));
	}
}
