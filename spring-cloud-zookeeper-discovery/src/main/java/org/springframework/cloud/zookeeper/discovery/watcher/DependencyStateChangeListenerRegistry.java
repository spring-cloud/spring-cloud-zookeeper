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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.details.ServiceCacheListener;

/**
 * Informs all the DependencyWatcherListeners that a dependency's state has changed
 *
 * @author Marcin Grzejszczak
 * @author Tomasz Nurkiewicz, 4financeIT
 * @since 1.0.0
 */
public class DependencyStateChangeListenerRegistry implements ServiceCacheListener {

	private static final Log log = LogFactory.getLog(DependencyStateChangeListenerRegistry.class);

	private final List<DependencyWatcherListener> listeners;
	private final String dependencyName;
	private final ServiceCache<?> serviceCache;

	public DependencyStateChangeListenerRegistry(List<DependencyWatcherListener> listeners, String dependencyName, ServiceCache<?> serviceCache) {
		this.listeners = listeners;
		this.dependencyName = dependencyName;
		this.serviceCache = serviceCache;
	}

	@Override
	public void cacheChanged() {
		DependencyState state = this.serviceCache.getInstances().isEmpty() ? DependencyState.DISCONNECTED : DependencyState.CONNECTED;
		logCurrentState(state);
		informListeners(state);
	}

	private void logCurrentState(DependencyState dependencyState) {
		log.info("Service cache state change for '"+this.dependencyName+"' instances, current service state: " + dependencyState);
	}

	private void informListeners(DependencyState state) {
		for (DependencyWatcherListener listener : this.listeners) {
			listener.stateChanged(this.dependencyName, state);
		}
	}

	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {
		// TODO do something or ignore for what is worth
	}
}
