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

package org.springframework.cloud.zookeeper.serviceregistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * The {@link ZookeeperInstanceHealthPublisher} is responsible for automatically updating
 * the instance status using the {@link HealthEndpoint}. Therefore it uses the
 * {@linkplain TaskScheduler} to schedule the instance status updates.*
 * @author Jan Thewes
 */
public class ZookeeperInstanceHealthPublisher {

	private static final Logger logger = LoggerFactory
			.getLogger(ZookeeperInstanceHealthPublisher.class);
	private final HealthEndpoint healthEndpoint;
	private final ZookeeperServiceRegistry zookeeperServiceRegistry;
	private final ZookeeperRegistration zookeeperRegistration;

	public ZookeeperInstanceHealthPublisher(HealthEndpoint healthEndpoint,
			ZookeeperServiceRegistry zookeeperServiceRegistry,
			ZookeeperRegistration zookeeperRegistration) {
		if (logger.isTraceEnabled()) {
			logger.trace("Instance health publisher is activated");
		}
		this.healthEndpoint = healthEndpoint;
		this.zookeeperServiceRegistry = zookeeperServiceRegistry;
		this.zookeeperRegistration = zookeeperRegistration;
	}

	@Scheduled(initialDelayString = "${spring.cloud.zookeeper.discovery"
			+ ".instanceHealthStatusPublishInterval}", fixedDelayString = "${spring.cloud.zookeeper.discovery"
					+ ".instanceHealthStatusPublishInterval}")
	public void executeHealthStatusPublish() {
		try {
			logger.trace("Checking instance health status");
			String currentRegisteredInstanceStatus = zookeeperServiceRegistry
					.getStatus(zookeeperRegistration).toString();
			String currentInstanceStatus = healthEndpoint.health().getStatus().getCode();
			if (currentRegisteredInstanceStatus == null
					|| !currentRegisteredInstanceStatus.equals(currentInstanceStatus)) {
				if (logger.isTraceEnabled()) {
					logger.trace(
							"Setting instance status to '{}'. Current status in service registry is '{}'",
							currentInstanceStatus, currentRegisteredInstanceStatus);
				}
				zookeeperServiceRegistry.setStatus(zookeeperRegistration,
						currentInstanceStatus);
			}
		}
		catch (Exception e) {
			logger.warn(
					"There was an error publishing the current health status to the service registry",
					e);
		}
	}

}
