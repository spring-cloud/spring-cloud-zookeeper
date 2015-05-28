/*
 * Copyright 2012-2015 the original author or authors.
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
package org.springframework.cloud.zookeeper.discovery.watcher;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.zookeeper.discovery.dependency.DependenciesPassedCondition;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependenciesAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.watcher.presence.DefaultDependencyPresenceOnStartupVerifier;
import org.springframework.cloud.zookeeper.discovery.watcher.presence.DependencyPresenceOnStartupVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides hooks for observing dependency lifecycle in Zookeeper.
 * Needs custom dependencies to be set in order to work.
 *
 * @see ZookeeperDependencies
 *
 * @author Marcin Grzejszczak, 4financeIT
 */
@Configuration
@EnableConfigurationProperties
@Conditional(DependenciesPassedCondition.class)
@ConditionalOnProperty(value = "zookeeper.dependencies.enabled", matchIfMissing = true)
@AutoConfigureAfter(ZookeeperDependenciesAutoConfiguration.class)
public class DependencyWatcherAutoConfiguration {

	@Autowired(required = false)
	private List<DependencyWatcherListener> dependencyWatcherListeners = new ArrayList<>();

	@Bean
	@ConditionalOnMissingBean
	public DependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier() {
		return new DefaultDependencyPresenceOnStartupVerifier();
	}

	@Bean(initMethod = "registerDependencyRegistrationHooks", destroyMethod = "clearDependencyRegistrationHooks")
	@ConditionalOnMissingBean
	public DependencyRegistrationHookProvider dependencyWatcher(ServiceDiscovery serviceDiscovery,
																DependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier,
																ZookeeperDependencies zookeeperDependencies) {
		return new DefaultDependencyWatcher(serviceDiscovery,
				dependencyPresenceOnStartupVerifier,
				dependencyWatcherListeners,
				zookeeperDependencies);
	}
}
