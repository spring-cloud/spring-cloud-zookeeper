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

/**
 * @author Spencer Gibb
 */

import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.UriSpec;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.zookeeper.discovery.ConditionalOnZookeeperDiscoveryEnabled;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnBean(AutoServiceRegistrationProperties.class)
@ConditionalOnZookeeperDiscoveryEnabled
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@AutoConfigureAfter(ZookeeperServiceRegistryAutoConfiguration.class)
@AutoConfigureBefore( {AutoServiceRegistrationAutoConfiguration.class, ZookeeperDiscoveryAutoConfiguration.class} )
public class ZookeeperAutoServiceRegistrationAutoConfiguration {

	@Bean
	public ZookeeperAutoServiceRegistration zookeeperAutoServiceRegistration(ZookeeperServiceRegistry registry, ZookeeperRegistration registration,
			ZookeeperDiscoveryProperties properties) {
		return new ZookeeperAutoServiceRegistration(registry, registration, properties);
	}

	@Bean
	@ConditionalOnMissingBean
	public ZookeeperRegistration zookeeperRegistration(ApplicationContext context, ZookeeperDiscoveryProperties properties) {
		String host = properties.getInstanceHost();
		if (!StringUtils.hasText(host)) {
			throw new IllegalStateException("instanceHost must not be empty");
		}

		UriSpec uriSpec = new UriSpec(properties.getUriSpec());

		RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(context.getEnvironment());
		String appName = resolver.getProperty("spring.application.name", "application");

		try {
			ServiceInstanceBuilder<ZookeeperInstance> builder = ServiceInstance.<ZookeeperInstance>builder()
					.name(appName)
					.payload(new ZookeeperInstance(context.getId(), appName, properties.getMetadata()))
					.address(host)
					.uriSpec(uriSpec);
			return new ZookeeperBuilderRegistration(builder);
		} catch (Exception e) {
			throw new RuntimeException("Error building ZookeeperRegistration", e);
		}
	}
}
