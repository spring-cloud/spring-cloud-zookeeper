/*
 * Copyright 2015-2025 the original author or authors.
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

package org.springframework.cloud.zookeeper.discovery;

import com.jayway.awaitility.Awaitility;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.test.TestSocketUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * Test for gh-91, using s-c-zookeeper in a non-web app.
 *
 * @author Marcin Grzejszczak
 */
public class ZookeeprDiscoveryNonWebAppTests {

	TestingServer server;

	String connectionString;

	@Before
	public void setup() throws Exception {
		this.server = new TestingServer(TestSocketUtils.findAvailableTcpPort());
		this.connectionString = "--spring.cloud.zookeeper.connectString="
				+ this.server.getConnectString();
	}

	@After
	public void cleanup() throws Exception {
		this.server.close();
	}

	@Test
	public void should_work_when_using_web_client_without_the_web_environment() {
		SpringApplication producerApp = new SpringApplicationBuilder(HelloProducer.class)
				.web(WebApplicationType.SERVLET).build();
		SpringApplication clientApplication = new SpringApplicationBuilder(
				HelloClient.class).web(WebApplicationType.NONE).build();

		try (ConfigurableApplicationContext producerContext = producerApp.run(
				this.connectionString, "--server.port=0",
				"--spring.application.name=hello-world", "--debug")) {
			try (ConfigurableApplicationContext context = clientApplication.run(
					this.connectionString,
					"--spring.cloud.zookeeper.discovery.register=false")) {
				Awaitility.await().until(new Runnable() {
					@Override
					public void run() {
						try {
							HelloClient bean = context.getBean(HelloClient.class);
							then(bean.discoveryClient.getServices()).isNotEmpty();
							then(bean.discoveryClient.getInstances("hello-world"))
									.isNotEmpty();
							String string = bean.restTemplate
									.getForObject("http://hello-world/", String.class);
							then(string).isEqualTo("foo");
						}
						catch (IllegalStateException e) {
							throw new AssertionError(e);
						}
					}
				});
			}
		}
	}

	@EnableAutoConfiguration(exclude = { JmxAutoConfiguration.class })
	@Configuration
	static class HelloClient {

		@LoadBalanced
		@Bean
		RestTemplate restTemplate() {
			this.restTemplate = new RestTemplateBuilder().build();
			return this.restTemplate;
		}

		@Autowired
		DiscoveryClient discoveryClient;

		RestTemplate restTemplate;

	}

	@EnableAutoConfiguration(exclude = { JmxAutoConfiguration.class })
	@RestController
	static class HelloProducer {

		@RequestMapping("/")
		public String foo() {
			return "foo";
		}

	}

}
