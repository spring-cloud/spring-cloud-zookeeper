package org.springframework.cloud.zookeeper.discovery.issues.issue91;

import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.EndpointMBeanExportAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SocketUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.jayway.awaitility.Awaitility;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
public class Issue91Tests {

	TestingServer server;
	String connectionString;

	@Before
	public void setup() throws Exception {
		this.server = new TestingServer(SocketUtils.findAvailableTcpPort());
		this.connectionString = "--spring.cloud.zookeeper.connectString=" + this.server.getConnectString();
	}

	@After
	public void cleanup() throws Exception {
		this.server.close();
	}

	@Test
	public void should_work_when_using_web_client_without_the_web_environment()
			throws Exception {
		SpringApplication producerApp = new SpringApplication(HelloProducer.class);
		producerApp.setWebEnvironment(true);
		SpringApplication clientApplication = new SpringApplication(HelloClient.class);
		clientApplication.setWebEnvironment(false);

		try (ConfigurableApplicationContext producerContext = producerApp.run(this.connectionString, "--server.port=0",
				"--spring.application.name=hello-world")) {
			try (final ConfigurableApplicationContext context = clientApplication.run(this.connectionString,
					"--spring.cloud.zookeeper.discovery.register=false")) {
				Awaitility.await().until(new Runnable() {
					@Override public void run() {
						try {
							HelloClient bean = context.getBean(HelloClient.class);
							then(bean.discoveryClient.getServices()).isNotEmpty();
							then(bean.discoveryClient.getInstances("hello-world")).isNotEmpty();
							String string = bean.restTemplate.getForObject("http://hello-world/", String.class);
							then(string).isEqualTo("foo");
						} catch (IllegalStateException e) {
							throw new AssertionError(e);
						}
					}
				});
			}
		}
	}
}

@EnableAutoConfiguration(exclude = {EndpointMBeanExportAutoConfiguration.class,
		JmxAutoConfiguration.class})
@EnableDiscoveryClient
@Configuration
class HelloClient {
	@LoadBalanced @Bean RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired DiscoveryClient discoveryClient;

	@Autowired RestTemplate restTemplate;
}

@EnableAutoConfiguration(exclude = {EndpointMBeanExportAutoConfiguration.class,
		JmxAutoConfiguration.class})
@EnableDiscoveryClient
@RestController
class HelloProducer {

	@RequestMapping("/")
	public String foo() {
		return "foo";
	}

}