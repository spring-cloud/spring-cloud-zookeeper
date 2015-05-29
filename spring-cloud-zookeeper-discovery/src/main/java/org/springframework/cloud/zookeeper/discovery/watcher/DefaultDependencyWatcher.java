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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.SneakyThrows;
import org.apache.curator.x.discovery.ServiceCache;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceDiscovery;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies.ZookeeperDependency;
import org.springframework.cloud.zookeeper.discovery.watcher.presence.DependencyPresenceOnStartupVerifier;
import org.springframework.context.ApplicationListener;

/**
 * @author Marcin Grzejszczak, 4financeIT
 * @author Michal Chmielarz, 4financeIT
 */
public class DefaultDependencyWatcher implements DependencyRegistrationHookProvider, ApplicationListener<InstanceRegisteredEvent> {

	private final ZookeeperServiceDiscovery serviceDiscovery;
	private final Map<String, ServiceCache> dependencyRegistry = new ConcurrentHashMap<>();
	private final List<DependencyWatcherListener> listeners;
	private final DependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier;
	private final ZookeeperDependencies zookeeperDependencies;

	public DefaultDependencyWatcher(ZookeeperServiceDiscovery serviceDiscovery,
									DependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier,
									List<DependencyWatcherListener> dependencyWatcherListeners,
									ZookeeperDependencies zookeeperDependencies) {
		this.serviceDiscovery = serviceDiscovery;
		this.dependencyPresenceOnStartupVerifier = dependencyPresenceOnStartupVerifier;
		this.listeners = dependencyWatcherListeners;
		this.zookeeperDependencies = zookeeperDependencies;
	}

	@Override
	@SneakyThrows
	public void onApplicationEvent(InstanceRegisteredEvent event) {
		registerDependencyRegistrationHooks();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void registerDependencyRegistrationHooks() throws Exception {
		for (ZookeeperDependency zookeeperDependency : zookeeperDependencies.getDependencyConfigurations()) {
			String dependencyPath = zookeeperDependency.getPath();
			ServiceCache serviceCache = serviceDiscovery.getServiceDiscovery()
					.serviceCacheBuilder().name(dependencyPath).build();
			serviceCache.start();
			dependencyPresenceOnStartupVerifier.verifyDependencyPresence(dependencyPath, serviceCache, zookeeperDependency.isRequired());
			dependencyRegistry.put(dependencyPath, serviceCache);
			serviceCache.addListener(new DependencyStateChangeListenerRegistry(listeners, dependencyPath, serviceCache));
		}
	}

	@Override
	public void clearDependencyRegistrationHooks() throws IOException {
		for (ServiceCache cache : dependencyRegistry.values()) {
			cache.close();
		}
	}

}
