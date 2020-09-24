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

package org.springframework.cloud.zookeeper.discovery.configclient;

import org.junit.After;
import org.junit.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dave Syer
 */
public class ZookeeperConfigServerAutoConfigurationTests {

	private ConfigurableApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void offByDefault() {
		this.context = new AnnotationConfigApplicationContext(
				ZookeeperConfigServerAutoConfiguration.class);
		assertThat(this.context
				.getBeanNamesForType(ZookeeperDiscoveryProperties.class).length)
				.isEqualTo(0);
	}

	@Test
	public void onWhenRequested() {
		setup("spring.cloud.config.server.prefix=/config");
		assertThat(this.context
				.getBeanNamesForType(ZookeeperDiscoveryProperties.class).length)
				.isEqualTo(1);
		ZookeeperDiscoveryProperties properties = this.context
				.getBean(ZookeeperDiscoveryProperties.class);
		assertThat(properties.getMetadata()).containsEntry("configPath", "/config");
	}

	private void setup(String... env) {
		this.context = new SpringApplicationBuilder(
				PropertyPlaceholderAutoConfiguration.class,
				ZookeeperConfigServerAutoConfiguration.class,
				ConfigServerProperties.class, ZookeeperDiscoveryProperties.class)
				.listeners(new ZookeeperTestingServer())
						.web(WebApplicationType.NONE).properties(env).run();
	}

}
