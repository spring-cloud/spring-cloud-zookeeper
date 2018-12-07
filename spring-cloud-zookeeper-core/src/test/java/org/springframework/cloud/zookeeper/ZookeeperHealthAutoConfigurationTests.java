/*
 * Copyright 2013-2018 the original author or authors.
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
package org.springframework.cloud.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

/**
 * Tests for {@link ZookeeperHealthAutoConfiguration}.
 * 
 * @author tgianos
 * @since 2.0.1
 */
public class ZookeeperHealthAutoConfigurationTests {
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(ZookeeperAutoConfiguration.class,
					ZookeeperHealthAutoConfiguration.class))
			.withUserConfiguration(ZookeeperAutoConfigurationTests.TestConfig.class);

	@Test
	public void testDefaultPropertiesCreateZookeeperHealthIndicator() {
		this.contextRunner.run((context) -> Assertions.assertThat(context)
				.hasSingleBean(ZookeeperHealthIndicator.class));
	}

	@Test
	public void testZookeeperHealthIndicatorDisabled() {
		this.contextRunner.withPropertyValues("management.health.zookeeper.enabled=false")
				.run((context) -> Assertions.assertThat(context)
						.doesNotHaveBean(ZookeeperHealthIndicator.class));
	}

	@Test
	public void testZookeeperHealthIndicatorAlreadyAdded() {
		this.contextRunner.withUserConfiguration(HealthIndicatorCustomConfig.class)
				.run((context) -> {
					Assertions.assertThat(context)
							.hasSingleBean(ZookeeperHealthIndicator.class);
					Assertions.assertThat(context)
							.doesNotHaveBean("zookeeperHealthIndicator");
					Assertions.assertThat(context)
							.hasBean("customZookeeperHealthIndicator");
				});
	}

	static class HealthIndicatorCustomConfig {
		@Bean
		ZookeeperHealthIndicator customZookeeperHealthIndicator(
				CuratorFramework curatorFramework) {
			return new ZookeeperHealthIndicator(curatorFramework);
		}
	}
}
