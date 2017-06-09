/*
 * Copyright 2013-2015 the original author or authors.
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

import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependency;
import org.springframework.cloud.zookeeper.discovery.watcher.presence.DependencyPresenceOnStartupVerifier;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ReflectionUtils;

/**
 * This Dependency Watcher will verify the presence of dependencies upon startup and registers listeners
 * to changing of state of dependencies during the application's lifecycle.
 *
 * @author Marcin Grzejszczak
 * @author Michal Chmielarz, 4financeIT
 * @since 1.0.0
 *
 * @see DependencyPresenceOnStartupVerifier
 * @see DependencyWatcherListener
 */
public class DefaultDependencyWatcher implements DependencyRegistrationHookProvider, ApplicationListener<InstanceRegisteredEvent<?>> {

	private final Map<String, ServiceCache<?>> dependencyRegistry = new ConcurrentHashMap<>();
	private final List<DependencyWatcherListener> listeners;
	private ServiceDiscovery<ZookeeperInstance> serviceDiscovery;
	private final DependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier;
	private final ZookeeperDependencies zookeeperDependencies;

	public DefaultDependencyWatcher(ServiceDiscovery<ZookeeperInstance> serviceDiscovery,
									DependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier,
									List<DependencyWatcherListener> dependencyWatcherListeners,
									ZookeeperDependencies zookeeperDependencies) {
		this.serviceDiscovery = serviceDiscovery;
		this.dependencyPresenceOnStartupVerifier = dependencyPresenceOnStartupVerifier;
		this.listeners = dependencyWatcherListeners;
		this.zookeeperDependencies = zookeeperDependencies;
	}

	@Override
	public void onApplicationEvent(InstanceRegisteredEvent<?> event) {
		registerDependencyRegistrationHooks();
	}

	@Override
	public void registerDependencyRegistrationHooks() {
		for (ZookeeperDependency zookeeperDependency : this.zookeeperDependencies.getDependencyConfigurations()) {
			String dependencyPath = zookeeperDependency.getPath();
			ServiceCache<?> serviceCache = getServiceDiscovery()
					.serviceCacheBuilder().name(dependencyPath).build();
			try {
				serviceCache.start();
			}
			catch (Exception e) {
				ReflectionUtils.rethrowRuntimeException(e);
			}
			this.dependencyPresenceOnStartupVerifier.verifyDependencyPresence(dependencyPath, serviceCache, zookeeperDependency.isRequired());
			this.dependencyRegistry.put(dependencyPath, serviceCache);
			serviceCache.addListener(new DependencyStateChangeListenerRegistry(this.listeners, dependencyPath, serviceCache));
		}
	}

	private ServiceDiscovery<ZookeeperInstance> getServiceDiscovery() {
		return this.serviceDiscovery;
	}

	@Override
	public void clearDependencyRegistrationHooks() throws IOException {
		for (ServiceCache<?> cache : this.dependencyRegistry.values()) {
			cache.close();
		}
	}

}
