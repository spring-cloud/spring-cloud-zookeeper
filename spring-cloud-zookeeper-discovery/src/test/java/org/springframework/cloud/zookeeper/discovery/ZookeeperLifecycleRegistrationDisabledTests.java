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

package org.springframework.cloud.zookeeper.discovery;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZookeeperLifecycleRegistrationDisabledTests.TestPropsConfig.class,
	properties = { "spring.application.name=myTestNotRegisteredService",
		"spring.cloud.zookeeper.discovery.register=false", "spring.cloud.zookeeper.dependency.enabled=false"},
		webEnvironment = RANDOM_PORT)
public class ZookeeperLifecycleRegistrationDisabledTests {


	@Autowired
	private ZookeeperDiscoveryClient client;

	@Test
	public void contextLoads() {
		List<ServiceInstance> instances = this.client.getInstances("myTestNotRegisteredService");
		assertTrue("service was registered", instances.isEmpty());
	}

	@Configuration
	@EnableAutoConfiguration
	@Import({ CommonTestConfig.class })
	static class TestPropsConfig { }
}
