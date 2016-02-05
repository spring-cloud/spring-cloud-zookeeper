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

package org.springframework.cloud.zookeeper.config;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.event.EventListener;

import lombok.extern.apachecommons.CommonsLog;

/**
 * @author Spencer Gibb
 */
@CommonsLog
public class ZookeeperConfigRefreshListener {
	private String contextName;
	private RefreshEndpoint refresh;
	private AtomicBoolean ready = new AtomicBoolean(false);

	public ZookeeperConfigRefreshListener(String contextName, RefreshEndpoint refresh) {
		this.contextName = contextName;
		this.refresh = refresh;
	}

	@EventListener
	public void handle(ApplicationReadyEvent event) {
		this.ready.compareAndSet(false, true);
	}

	@EventListener
	public void handle(ZookeeperConfigRefreshEvent event) {
		if (this.ready.get()) { // don't handle events before app is ready
			log.debug(contextName + " " + event.getEventDesc());
			if (this.refresh != null) {
				String[] keys = this.refresh.refresh();
				log.info("Refresh keys changed: " + Arrays.asList(keys));
			}
		}
	}
}
