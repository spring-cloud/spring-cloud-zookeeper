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

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.zookeeper.discovery.ConditionalOnZookeeperDiscoveryEnabled;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnZookeeperDiscoveryEnabled
@ConditionalOnProperty(value = "spring.cloud.service-registry.enabled", matchIfMissing = true)
@AutoConfigureBefore(ServiceRegistryAutoConfiguration.class)
public class ZookeeperServiceRegistryAutoConfiguration implements ApplicationContextAware {

	private ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Bean
	@SuppressWarnings("unchecked")
	public ZookeeperServiceRegistry zookeeperServiceRegistry() {
		return new ZookeeperServiceRegistry(this.context.getBean(ServiceDiscovery.class));
	}

	@Bean
	@ConditionalOnMissingBean
	public InstanceSerializer<ZookeeperInstance> instanceSerializer() {
		return new JsonInstanceSerializer<>(ZookeeperInstance.class);
	}

	@Bean
	@ConditionalOnMissingBean
	public ZookeeperDiscoveryProperties zookeeperDiscoveryProperties(InetUtils inetUtils) {
		return new ZookeeperDiscoveryProperties(inetUtils);
	}
}
