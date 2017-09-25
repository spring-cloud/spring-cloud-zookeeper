/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.zookeeper.serviceregistry;

import java.util.Collection;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.application.name=myTestService1-F" },
		webEnvironment = RANDOM_PORT)
public class ZookeeperAutoServiceRegistrationTests {

	@Autowired
	private ZookeeperRegistration registration;

	@Autowired
	private ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

	@Autowired
	private ZookeeperDiscoveryProperties properties;

	@Test
	public void contextLoads() throws Exception {
		Collection<ServiceInstance<ZookeeperInstance>> instances = serviceDiscovery.queryForInstances("myTestService1-F");
		assertThat(instances).hasSize(1);

		ServiceInstance<ZookeeperInstance> instance = instances.iterator().next();
		assertThat(instance).isNotNull();
		assertThat(instance.getName()).isEqualTo("myTestService1-F");
		/*Response<Map<String, Service>> response = consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get(registration.getServiceId());
		assertNotNull("service was null", service);
		assertNotEquals("service port is 0", 0, service.getPort().intValue());
		assertFalse("service id contained invalid character: " + service.getId(), service.getId().contains(":"));
		assertEquals("service id was wrong", registration.getServiceId(), service.getId());
		assertEquals("service name was wrong", "myTestService1-FF-something", service.getService());
		assertFalse("service address must not be empty", StringUtils.isEmpty(service.getAddress()));
		assertEquals("service address must equals hostname from discovery properties", discoveryProperties.getHostname(), service.getAddress());*/
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import({CommonTestConfig.class})
	/*@ImportAutoConfiguration({AutoServiceRegistrationAutoConfiguration.class, ZookeeperAutoServiceRegistration.class,
			ZookeeperServiceRegistryAutoConfiguration.class})*/
	protected static class TestConfig { }
}

