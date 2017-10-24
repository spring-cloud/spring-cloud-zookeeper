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

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ConditionalOnZookeeperDiscoveryEnabled;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration.RegistrationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnMissingBean(type = "org.springframework.cloud.zookeeper.discovery.ZookeeperLifecycle")
@ConditionalOnZookeeperDiscoveryEnabled
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@AutoConfigureAfter( { ZookeeperServiceRegistryAutoConfiguration.class} )
@AutoConfigureBefore( {AutoServiceRegistrationAutoConfiguration.class,ZookeeperDiscoveryAutoConfiguration.class} )
public class ZookeeperAutoServiceRegistrationAutoConfiguration {

	@Bean
	public ZookeeperAutoServiceRegistration zookeeperAutoServiceRegistration(
			ZookeeperServiceRegistry registry, ZookeeperRegistration registration,
			ZookeeperDiscoveryProperties properties) {
		return new ZookeeperAutoServiceRegistration(registry, registration, properties);
	}

	@Bean
	@ConditionalOnMissingBean(ZookeeperRegistration.class)
	public ServiceInstanceRegistration serviceInstanceRegistration(
			ApplicationContext context, ZookeeperDiscoveryProperties properties) {
		String appName = context.getEnvironment().getProperty("spring.application.name",
				"application");
		String host = properties.getInstanceHost();
		if (!StringUtils.hasText(host)) {
			throw new IllegalStateException("instanceHost must not be empty");
		}

		ZookeeperInstance zookeeperInstance = new ZookeeperInstance(context.getId(),
				appName, properties.getMetadata());
		RegistrationBuilder builder = ServiceInstanceRegistration.builder().address(host)
				.name(appName).payload(zookeeperInstance)
				.uriSpec(properties.getUriSpec());

		if (properties.getInstanceSslPort() != null) {
			builder.sslPort(properties.getInstanceSslPort());
		}
		if (properties.getInstanceId() != null) {
			builder.id(properties.getInstanceId());
		}


		// TODO add customizer?

		return builder.build();
	}

}
