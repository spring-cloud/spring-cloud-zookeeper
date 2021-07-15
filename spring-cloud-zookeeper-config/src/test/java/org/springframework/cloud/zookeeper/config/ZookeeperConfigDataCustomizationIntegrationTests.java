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

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.cloud.zookeeper.CuratorFactory;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Spencer Gibb
 */
public class ZookeeperConfigDataCustomizationIntegrationTests {

	private static final Log log = LogFactory
			.getLog(ZookeeperConfigDataCustomizationIntegrationTests.class);

	public static final String PREFIX = "test__configdata__";

	public static final String ROOT = "/" + PREFIX + UUID.randomUUID();

	private ConfigurableApplicationContext context;
	private BindHandlerBootstrapper bindHandlerBootstrapper;

	@Before
	public void setup() {
		bindHandlerBootstrapper = new BindHandlerBootstrapper();
		this.context = new SpringApplicationBuilder(Config.class)
				.listeners(new ZookeeperTestingServer())
				.web(WebApplicationType.NONE)
				.addBootstrapRegistryInitializer(bindHandlerBootstrapper)
				.addBootstrapRegistryInitializer(ZookeeperBootstrapper.fromBootstrapContext(this::curatorFramework))
				.run("--spring.config.import=zookeeper:",
						"--spring.application.name=testZkConfigDataIntegration",
						"--logging.level.org.springframework.cloud.zookeeper=DEBUG",
						"--spring.cloud.zookeeper.config.root=" + ROOT);
	}

	@After
	public void after() {
		if (context != null) {
			this.context.close();
		}
	}

	CuratorFramework curatorFramework(BootstrapContext context) {
		ZookeeperProperties properties = context.get(ZookeeperProperties.class);
		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
				.retryPolicy(CuratorFactory.retryPolicy(properties))
				.connectString(properties.getConnectString());
		TestCuratorFramework curator = new TestCuratorFramework(builder);
		curator.start();
		try {
			curator.blockUntilConnected(properties.getBlockUntilConnectedWait(),
					properties.getBlockUntilConnectedUnit());
		}
		catch (InterruptedException e) {
			ReflectionUtils.rethrowRuntimeException(e);
		}
		return curator;
	}

	@Test
	public void curatorFrameworkIsCustom() {
		CuratorFramework curator = context.getBean(CuratorFramework.class);
		assertThat(curator).isNotNull().isInstanceOf(TestCuratorFramework.class);
		assertThat(bindHandlerBootstrapper.onSuccessCount).isGreaterThan(0);
	}

	static class TestCuratorFramework extends CuratorFrameworkImpl {
		TestCuratorFramework(CuratorFrameworkFactory.Builder builder) {
			super(builder);
		}
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config {

	}

	static class BindHandlerBootstrapper implements BootstrapRegistryInitializer {

		private int onSuccessCount = 0;

		@Override
		public void initialize(BootstrapRegistry registry) {
			registry.register(BindHandler.class, context -> new BindHandler() {
				@Override
				public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context,
						Object result) {
					onSuccessCount++;
					return result;
				}
			});
		}

	}

}
