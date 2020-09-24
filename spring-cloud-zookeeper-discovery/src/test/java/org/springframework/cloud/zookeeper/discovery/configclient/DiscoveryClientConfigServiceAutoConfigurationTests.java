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

import java.util.Arrays;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.DiscoveryClientConfigServiceBootstrapConfiguration;
import org.springframework.cloud.test.ClassPathExclusions;
import org.springframework.cloud.test.ModifiedClassPathRunner;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClientConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Dave Syer
 */
@RunWith(ModifiedClassPathRunner.class)
@ClassPathExclusions({ "spring-retry-*.jar", "spring-boot-starter-aop-*.jar" })
public class DiscoveryClientConfigServiceAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			if (this.context.getParent() != null) {
				((AnnotationConfigApplicationContext) this.context.getParent()).close();
			}
			this.context.close();
		}
	}

	@Test
	public void onWhenRequested() {
		setup("server.port=7000", "spring.cloud.config.discovery.enabled=true",
				"spring.cloud.zookeeper.discovery.instance-port:7001",
				"spring.cloud.zookeeper.discovery.instance-host:foo",
				"spring.cloud.config.discovery.service-id:configserver");
		assertThat(this.context.getBeanNamesForType(ZookeeperConfigServerAutoConfiguration.class).length).isEqualTo(1);
		ZookeeperDiscoveryClient client = this.context.getParent().getBean(ZookeeperDiscoveryClient.class);
		verify(client, atLeast(2)).getInstances("configserver");
		ConfigClientProperties locator = this.context.getBean(ConfigClientProperties.class);
		assertThat(locator.getUri()[0]).isEqualTo("http://foo:7001/");
	}

	private void setup(String... env) {
		ZookeeperTestingServer testingServer = new ZookeeperTestingServer();
		testingServer.start();
		AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
		TestPropertyValues.of(env).applyTo(parent);
		TestPropertyValues.of(ZookeeperProperties.PREFIX + ".connect-string=localhost:" + testingServer.getPort())
				.applyTo(parent);
		parent.register(UtilAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class,
				EnvironmentKnobbler.class, ZookeeperDiscoveryClientConfigServiceBootstrapConfiguration.class,
				DiscoveryClientConfigServiceBootstrapConfiguration.class, ConfigClientProperties.class);
		testingServer.appPrepared(parent);
		parent.refresh();
		this.context = new AnnotationConfigApplicationContext();
		this.context.setParent(parent);
		this.context.register(PropertyPlaceholderAutoConfiguration.class, ZookeeperConfigServerAutoConfiguration.class,
				ZookeeperAutoConfiguration.class, ZookeeperDiscoveryClientConfiguration.class);
		this.context.refresh();
	}

	@Configuration
	protected static class EnvironmentKnobbler {

		@Bean
		public ZookeeperDiscoveryClient zookeeperDiscoveryClient(ZookeeperDiscoveryProperties properties) {
			ZookeeperDiscoveryClient client = mock(ZookeeperDiscoveryClient.class);
			ServiceInstance instance = new DefaultServiceInstance("configserver1", "configserver",
					properties.getInstanceHost(), properties.getInstancePort(), false);
			given(client.getInstances("configserver")).willReturn(Arrays.asList(instance));
			return client;
		}

	}

}
