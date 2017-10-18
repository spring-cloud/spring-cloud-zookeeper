/*
 * Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.zookeeper.discovery;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZookeeperDiscoveryAutoRegistrationFalseTests.Config.class,
		properties = { "spring.application.name=testzkautoregfalse", "debug=true" },
		webEnvironment = RANDOM_PORT)
@DirtiesContext
public class ZookeeperDiscoveryAutoRegistrationFalseTests {

	@Autowired DiscoveryClient discoveryClient;
	@Value("${spring.application.name}") String springAppName;

	@Test public void discovery_client_is_zookeeper() {
		//given: this.discoveryClient
		//expect:
		then(discoveryClient).isInstanceOf(CompositeDiscoveryClient.class);
		CompositeDiscoveryClient composite = (CompositeDiscoveryClient) discoveryClient;
		List<DiscoveryClient> discoveryClients = composite.getDiscoveryClients();
		DiscoveryClient first = discoveryClients.get(0);
		then(first).isInstanceOf(ZookeeperDiscoveryClient.class);
	}

	@Test public void application_should_not_have_been_registered() {
		//given:
		List<ServiceInstance> instances = this.discoveryClient.getInstances(springAppName);
		//expect:
		then(instances).isEmpty();
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	@EnableDiscoveryClient(autoRegister = false)
	static class Config {

	}

	@Controller
	@Profile("ribbon")
	class PingController {

		@RequestMapping("/ping") String ping() {
			return "pong";
		}
	}
}
