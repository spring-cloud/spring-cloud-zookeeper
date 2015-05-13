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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.details.ServiceCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Marcin Grzejszczak, 4financeIT
 * @author Tomasz Nurkiewicz, 4financeIT
 */
public class DependencyStateChangeListenerRegistry implements ServiceCacheListener {

	private static final Logger log = LoggerFactory.getLogger(DependencyStateChangeListenerRegistry.class);

	private final List<DependencyWatcherListener> listeners;
	private final String dependencyName;
	private final ServiceCache serviceCache;

	public DependencyStateChangeListenerRegistry(List<DependencyWatcherListener> listeners, String dependencyName, ServiceCache serviceCache) {
		this.listeners = listeners;
		this.dependencyName = dependencyName;
		this.serviceCache = serviceCache;
	}

	@Override
	public void cacheChanged() {
		DependencyState state = serviceCache.getInstances().isEmpty() ? DependencyState.DISCONNECTED : DependencyState.CONNECTED;
		logCurrentState(state);
		informListeners(state);
	}

	private void logCurrentState(DependencyState dependencyState) {
		log.info("Service cache state change for '{}' instances, current service state: {}", dependencyName, dependencyState);
	}

	private void informListeners(DependencyState state) {
		for (DependencyWatcherListener listener : listeners) {
			listener.stateChanged(dependencyName, state);
		}
	}

	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {
		// todo do something or ignore for what is worth
	}
}
