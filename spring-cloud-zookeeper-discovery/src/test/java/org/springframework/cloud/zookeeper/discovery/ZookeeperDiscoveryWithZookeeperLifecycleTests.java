/*
 * Copyright 2013-2017 the original author or authors.
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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.toomuchcoding.jsonassert.JsonPath;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Test for backwards compatibility
 * @author Marcin Grzejszczak
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZookeeperDiscoveryWithZookeeperLifecycleTests.Config.class,
		properties = "spring.application.name=testzkwithzookeeperlifecycle",
		webEnvironment = RANDOM_PORT)
@ActiveProfiles("ribbon")
public class ZookeeperDiscoveryWithZookeeperLifecycleTests {

	@Autowired TestRibbonClient testRibbonClient;
	@Autowired DiscoveryClient discoveryClient;
	@Autowired ZookeeperServiceDiscovery serviceDiscovery;
	@Value("${spring.application.name}") String springAppName;

	@Test public void should_find_the_app_by_its_name_via_Ribbon() {
		//expect:
		then(registeredServiceStatusViaServiceName()).isEqualTo("UP");
	}

	@Test public void should_find_a_collaborator_via_discovery_client() {
		//given:
		List<ServiceInstance> instances = this.discoveryClient.getInstances(this.springAppName);
		ServiceInstance instance = instances.get(0);
		//expect:
		then(registeredServiceStatus(instance)).isEqualTo("UP");
		then(instance.getMetadata().get("testMetadataKey")).isEqualTo("testMetadataValue");
	}

	@Test public void should_present_application_name_as_id_of_the_service_instance() {
		//given:
		ServiceInstance instance = this.discoveryClient.getLocalServiceInstance();
		//expect:
		then(this.springAppName).isEqualTo(instance.getServiceId());
	}

	private String registeredServiceStatusViaServiceName() {
		return JsonPath.builder(this.testRibbonClient.thisHealthCheck()).field("status").read(String.class);
	}

	private String registeredServiceStatus(ServiceInstance instance) {
		return JsonPath.builder(this.testRibbonClient.callOnUrl(instance.getHost()+":"+instance.getPort(), "health")).field("status").read(String.class);
	}

	@Test public void should_properly_find_local_instance() {
		//expect:
		then(this.serviceDiscovery.getServiceInstanceRef().get().getAddress()).isEqualTo(this.discoveryClient.getLocalServiceInstance().getHost());
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	@Profile("ribbon")
	static class Config {
		@Bean
		public ZookeeperServiceDiscovery zookeeperServiceDiscovery(ZookeeperDiscoveryProperties properties, CuratorFramework curator,
													 InstanceSerializer<ZookeeperInstance> instanceSerializer) {
			return new ZookeeperServiceDiscovery(curator, properties, instanceSerializer);
		}

		@Bean
		public ZookeeperLifecycle zookeeperLifecycle(ZookeeperDiscoveryProperties properties,
													 ZookeeperServiceDiscovery serviceDiscovery) {
			return new ZookeeperLifecycle(properties, serviceDiscovery);
		}

		@Bean TestRibbonClient testRibbonClient(@LoadBalanced RestTemplate restTemplate,
				@Value("${spring.application.name}") String springAppName) {
			return new TestRibbonClient(restTemplate, springAppName);
		}
	}

	@Controller
	@Profile("ribbon")
	class PingController {

		@RequestMapping("/ping") String ping() {
			return "pong";
		}
	}
}
