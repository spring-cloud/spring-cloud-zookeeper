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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author Eric Fenderbosch
 */
public class ZookeeperTreeCachePropertySourceTests {

	@Configuration
	@ComponentScan
	@EnableAutoConfiguration
	static class Config {
		//
	}

	private TestingServer zkServer;
	private ConfigurableApplicationContext context;

	@Before
	public void setup() throws Exception {
		zkServer = new TestingServer(2181, true);
		context = new SpringApplicationBuilder(Config.class).web(false). //
				run("--spring.application.name=testZkPropertySource");
	}

	@After
	public void teardown() throws Exception {
		context.close();
		zkServer.close();
	}

	@Test
	public void testRefresh() throws Exception {
		TestConfig config = context.getBean(TestConfig.class);
		assertThat(config.getOne(), nullValue());
		@SuppressWarnings("resource")
		CuratorFramework curator = context.getBean(CuratorFramework.class);
		setValue(curator, "/config/testZkPropertySource/config/one", "bar");
		Thread.sleep(1000);
		assertThat(config.getOne(), is("bar"));
	}

	private static void setValue(CuratorFramework zkClient, String name, String value) {
		String path = ZKPaths.makePath("/", name.replaceAll("\\.", "/"));
		byte[] bytes = value.getBytes();
		try {
			zkClient.setData().forPath(path, bytes);
		} catch (KeeperException.NoNodeException ignore) {
			try {
				zkClient.create().creatingParentsIfNeeded().forPath(path, bytes);
			} catch (Exception e) {
				// ignore
			}
		} catch (Exception e) {
			// ignore
		}
	}

	@Component
	@RefreshScope
	@ConfigurationProperties("config")
	public static class TestConfig {

		private String one;

		public String getOne() {
			return one;
		}

		public void setOne(String one) {
			this.one = one;
		}
	}

}
