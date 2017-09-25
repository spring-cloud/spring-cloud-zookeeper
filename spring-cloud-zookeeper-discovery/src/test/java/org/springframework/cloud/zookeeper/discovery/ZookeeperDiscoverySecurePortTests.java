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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.ServerIntrospector;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZookeeperDiscoverySecurePortTests.Config.class,
		properties = {
			"feign.hystrix.enabled=false",
			"spring.cloud.zookeeper.discovery.uriSpec={scheme}://{address}:{port}/contextPath",
			"spring.cloud.zookeeper.discovery.instance-ssl-port=8443",
		},
		webEnvironment = RANDOM_PORT)
@ActiveProfiles("ribbon")
@DirtiesContext
public class ZookeeperDiscoverySecurePortTests {

	@Autowired
	private LoadBalancerClient loadBalancerClient;

	@Autowired
	private ZookeeperRegistration zookeeperRegistration;

	@Autowired
	private SpringClientFactory clientFactory;

	@Value("${spring.application.name}")
	private String springAppName;

	@Test
	public void zookeeperServerIntrospectorWorks() {
		ServerIntrospector serverIntrospector = this.clientFactory.getInstance(springAppName, ServerIntrospector.class);
		then(serverIntrospector).isInstanceOf(ZookeeperServerIntrospector.class);

		ZookeeperServer zookeeperServer = new ZookeeperServer(this.zookeeperRegistration.getServiceInstance());
		then(serverIntrospector.isSecure(zookeeperServer)).isTrue();
	}

	@Test
	public void isSecureIsTrue() {
		ServiceInstance instance = this.loadBalancerClient.choose(this.springAppName);
		then(instance.isSecure()).isTrue();
	}

	@Test
	public void shouldSetServiceInstanceSslPort() {
		then(this.zookeeperRegistration.getServiceInstance().getSslPort()).isEqualTo(8443);
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	@Profile("ribbon")
	static class Config {
	}

}
