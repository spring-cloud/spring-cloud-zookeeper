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

package org.springframework.cloud.zookeeper.discovery.configclient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClientConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.support.CuratorServiceDiscoveryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

/**
 * Helper for config client that wants to lookup the config server via discovery.
 *
 * @author Spencer Gibb
 */
@ConditionalOnClass(ConfigServicePropertySourceLocator.class)
@ConditionalOnProperty(value = "spring.cloud.config.discovery.enabled", matchIfMissing = false)
@Configuration
@Import({ ZookeeperAutoConfiguration.class, ZookeeperDiscoveryClientConfiguration.class,
		CuratorServiceDiscoveryAutoConfiguration.class, ZookeeperDiscoveryAutoConfiguration.class})
@Order(0)
public class ZookeeperDiscoveryClientConfigServiceBootstrapConfiguration {

	@Bean
	public ZookeeperDiscoveryProperties zookeeperDiscoveryProperties(InetUtils inetUtils) {
		ZookeeperDiscoveryProperties properties = new ZookeeperDiscoveryProperties(inetUtils);
		// for bootstrap, registration is not needed, just discovery client
		properties.setRegister(false);
		return properties;
	}
}
