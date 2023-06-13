/*
 * Copyright 2015-2022 the original author or authors.
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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * @author Ryan Baxter
 */
@Testcontainers
class ZookeeperConfigServerBootstrapperTestsIT {
	private static final int ZOOKEEPER_PORT = 2181;

	public static final DockerImageName MOCKSERVER_IMAGE = DockerImageName.parse("mockserver/mockserver")
			.withTag("mockserver-" + MockServerClient.class.getPackage().getImplementationVersion());

	@Container
	private static final GenericContainer<?> zookeeper = new GenericContainer<>("zookeeper:3.8.0")
			.withExposedPorts(ZOOKEEPER_PORT);

	@Container
	static MockServerContainer mockServer = new MockServerContainer(MOCKSERVER_IMAGE);

	private ConfigurableApplicationContext context;

	private ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

	private CuratorFramework curatorFramework;

	@BeforeEach
	void before() {
		curatorFramework = CuratorFrameworkFactory.builder().connectString(zookeeper.getHost() + ":" + zookeeper.getMappedPort(ZOOKEEPER_PORT))
				.retryPolicy(new RetryOneTime(100)).build();

		try {
			curatorFramework.start();
			serviceDiscovery = ServiceDiscoveryBuilder.builder(ZookeeperInstance.class).client(curatorFramework).basePath("/services")
					.build();
			serviceDiscovery.start();
			serviceDiscovery.registerService(ServiceInstanceRegistration.builder().id("zookeeper-configserver").name("zookeeper-configserver")
					.address(mockServer.getHost()).port(mockServer.getServerPort()).uriSpec(ZookeeperDiscoveryProperties.DEFAULT_URI_SPEC).build().getServiceInstance());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@AfterEach
	void after() throws IOException {
		this.context.close();
		this.serviceDiscovery.close();
		this.curatorFramework.close();
	}

	@Test
	public void contextLoads() throws JsonProcessingException {
		Environment environment = new Environment("test", "default");
		Map<String, Object> properties = new HashMap<>();
		properties.put("hello", "world");
		PropertySource p = new PropertySource("p1", properties);
		environment.add(p);
		ObjectMapper objectMapper = new ObjectMapper();
		try (MockServerClient mockServerClient = new MockServerClient(mockServer.getHost(),
				mockServer.getMappedPort(MockServerContainer.PORT))) {
			mockServerClient.when(request().withPath("/application/default"))
					.respond(response().withBody(objectMapper.writeValueAsString(environment))
							.withHeader("content-type", "application/json"));
			this.context = setup().run();
			assertThat(this.context.getEnvironment().getProperty("hello")).isEqualTo("world");
		}

	}

	SpringApplicationBuilder setup(String... env) {
		return new SpringApplicationBuilder(TestConfig.class)
				.properties(addDefaultEnv(env));
	}

	private String[] addDefaultEnv(String[] env) {
		Set<String> set = new LinkedHashSet<>();
		if (env != null && env.length > 0) {
			set.addAll(Arrays.asList(env));
		}
		set.add("spring.config.import=classpath:bootstrapper.yaml");
		set.add("spring.cloud.config.enabled=true");
		set.add("spring.cloud.service-registry.auto-registration.enabled=false");
		set.add(ZookeeperProperties.PREFIX + ".connectString=" + zookeeper.getHost() + ":" + zookeeper.getMappedPort(ZOOKEEPER_PORT));
		return set.toArray(new String[0]);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestConfig {

	}
}
