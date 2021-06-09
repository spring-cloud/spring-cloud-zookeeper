/*
 * Copyright 2015-2021 the original author or authors.
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

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;
import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RestController;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Jan Thewes
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZookeeperDiscoveryHealthPublishingTests.Config.class, webEnvironment = RANDOM_PORT)
@ContextConfiguration(loader = ZookeeperTestingServer.Loader.class)
@ActiveProfiles("instancestatuspublishing")
@DirtiesContext
public class ZookeeperDiscoveryHealthPublishingTests {

	@Autowired
	ZookeeperRegistration zookeeperRegistration;
	@Autowired
	ZookeeperServiceRegistry zookeeperServiceRegistry;
	@Value("${spring.application.name}")
	String springAppName;
	@Autowired
	ModifyableHealthContributor modifyableHealthContributor;

	@Test
	public void check_instance_status_is_up_after_start() {
		then(zookeeperServiceRegistry.getStatus(zookeeperRegistration)).isEqualTo("UP");
	}

	@Test
	public void set_instance_unhealthy_and_check_service_registry() {
		modifyableHealthContributor.setHealthy(false);
		await().until(() -> zookeeperServiceRegistry.getStatus(zookeeperRegistration).equals("DOWN"));
	}

	@Test
	public void set_instance_healthy_unhealthy_healthy_and_check_service_registry_after_each_change() {
		modifyableHealthContributor.setHealthy(true);
		await().until(() -> zookeeperServiceRegistry.getStatus(zookeeperRegistration).equals("UP"));
		modifyableHealthContributor.setHealthy(false);
		await().until(() -> zookeeperServiceRegistry.getStatus(zookeeperRegistration).equals("DOWN"));
		modifyableHealthContributor.setHealthy(true);
		await().until(() -> zookeeperServiceRegistry.getStatus(zookeeperRegistration).equals("UP"));
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	@Profile("instancestatuspublishing")
	@RestController
	@EnableScheduling
	static class Config {

		@Bean
		public ModifyableHealthContributor simpleHealthContributor() {
			return new ModifyableHealthContributor();
		}

	}

	static class ModifyableHealthContributor extends AbstractHealthIndicator {

		private boolean isHealthy = true;

		@Override
		protected void doHealthCheck(Builder builder) throws Exception {
			if (isHealthy) {
				builder.up().build();
			}
			else {
				builder.down().build();
			}
		}

		public boolean isHealthy() {
			return isHealthy;
		}

		public void setHealthy(boolean healthy) {
			isHealthy = healthy;
		}
	}

}
