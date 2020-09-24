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

package org.springframework.cloud.zookeeper.discovery;

import java.util.List;

import com.jayway.awaitility.Awaitility;
import com.toomuchcoding.jsonassert.JsonPath;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.cloud.zookeeper.discovery.test.TestLoadBalancedClient;
import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration;
import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.zookeeper.discovery.test.TestLoadBalancedClient.BASE_PATH;

/**
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZookeeperDiscoveryTests.Config.class, properties = {
		"spring.cloud.zookeeper.discovery.uri-spec={scheme}://{address}:{port}/contextPath",
		"management.endpoints.web.exposure.include=*" }, webEnvironment = RANDOM_PORT)
@ActiveProfiles("loadbalancer")
@DirtiesContext
@ContextConfiguration(loader = ZookeeperTestingServer.Loader.class)
public class ZookeeperDiscoveryTests {

	@Autowired
	TestLoadBalancedClient testLoadBalancedClient;

	@Autowired
	DiscoveryClient discoveryClient;

	@Autowired
	ServiceInstanceRegistration serviceDiscovery;

	@Value("${spring.application.name}")
	String springAppName;

	@Autowired
	IdUsingFeignClient idUsingFeignClient;

	@Autowired
	Registration registration;

	@Test
	public void should_find_the_app_by_its_name_via_LoadBalancer() {
		// expect:
		then(registeredServiceStatusViaServiceName()).isEqualTo("UP");
	}

	@Test
	public void should_find_a_collaborator_via_discovery_client() {
		// given:
		List<ServiceInstance> instances = this.discoveryClient
				.getInstances(this.springAppName);
		ServiceInstance instance = instances.get(0);
		// expect:
		then(registeredServiceStatus(instance)).isEqualTo("UP");
		then(instance.getInstanceId()).isEqualTo("loadbalancer-instance-id-123");
		then(instance.getMetadata().get("testMetadataKey"))
				.isEqualTo("testMetadataValue");
		then(instance).isInstanceOf(ZookeeperServiceInstance.class);
		ZookeeperServiceInstance zkInstance = (ZookeeperServiceInstance) instance;
		then(zkInstance.getServiceInstance().getId()).isEqualTo("loadbalancer-instance-id-123");
	}

	@Test
	public void should_present_application_name_as_id_of_the_service_instance() {
		// given:
		// expect:
		then(this.springAppName).isEqualTo(this.registration.getServiceId());
	}

	@Test
	public void should_service_instance_uri_match_uriSpec() {
		// given:
		// expect:
		then(this.registration.getUri()).hasPath("/contextPath");
	}

	@Test
	public void should_find_an_instance_using_feign_via_service_id() {
		final IdUsingFeignClient idUsingFeignClient = this.idUsingFeignClient;
		// expect:
		Awaitility.await().until(() -> {
			then(idUsingFeignClient.hi()).isNotEmpty();
			return true;
		});
	}

	private String registeredServiceStatusViaServiceName() {
		return JsonPath.builder(this.testLoadBalancedClient.thisHealthCheck()).field("status")
				.read(String.class);
	}

	private String registeredServiceStatus(ServiceInstance instance) {
		return JsonPath.builder(this.testLoadBalancedClient.callOnUrl(
				instance.getHost() + ":" + instance.getPort(), BASE_PATH + "/health"))
				.field("status").read(String.class);
	}

	@Test
	public void should_properly_find_local_instance() {
		// expect:
		then(this.serviceDiscovery.getServiceInstance().getAddress())
				.isEqualTo(this.registration.getHost());
	}

	@FeignClient("loadBalancerApp")
	public interface IdUsingFeignClient {

		@RequestMapping(method = RequestMethod.GET, value = "/hi")
		String hi();

	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	@EnableFeignClients(clients = { IdUsingFeignClient.class })
	@Profile("loadbalancer")
	@RestController
	static class Config {

		@Bean
		TestLoadBalancedClient testLoadBalancedClient(@LoadBalanced RestTemplate restTemplate,
				@Value("${spring.application.name}") String springAppName) {
			return new TestLoadBalancedClient(restTemplate, springAppName);
		}

		@RequestMapping("/hi")
		public String hi() {
			return "hi";
		}

	}

	@Controller
	@Profile("loadbalancer")
	class PingController {

		@RequestMapping("/ping")
		String ping() {
			return "pong";
		}

	}

}
