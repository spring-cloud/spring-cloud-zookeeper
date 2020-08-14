/*
 * Copyright 2015-2019 the original author or authors.
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

package org.springframework.cloud.zookeeper.config;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.curator.framework.CuratorFramework;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.cloud.zookeeper.ConditionalOnZookeeperEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} that registers a Zookeeper configuration watcher.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnZookeeperEnabled
@ConditionalOnProperty(value = "spring.cloud.zookeeper.config.enabled", matchIfMissing = true)
public class ZookeeperConfigAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(RefreshEndpoint.class)
	protected static class ZkRefreshConfiguration {

		@Bean
		@ConditionalOnBean(ZookeeperPropertySourceLocator.class)
		@ConditionalOnProperty(name = "spring.cloud.zookeeper.config.watcher.enabled", matchIfMissing = true)
		public ConfigWatcher propertySourceLocatorConfigWatcher(ZookeeperPropertySourceLocator locator,
				CuratorFramework curator) {
			return new ConfigWatcher(locator.getContexts(), curator);
		}

		@Bean
		@ConditionalOnMissingBean(ZookeeperPropertySourceLocator.class)
		public ConfigWatcher configDataConfigWatcher(ConfigurableEnvironment env) {
			// move to separate class
			List<PropertySource<?>> sources = env.getPropertySources().stream()
					.filter(propertySource -> propertySource instanceof ZookeeperPropertySource)
					.collect(Collectors.toList());
			List<String> contexts = sources.stream()
					.map(propertySource -> ((ZookeeperPropertySource) propertySource).getContext())
					.collect(Collectors.toList());
			CuratorFramework source = (CuratorFramework) sources.get(0).getSource();
			return new ConfigWatcher(contexts, source);
		}

	}

}
