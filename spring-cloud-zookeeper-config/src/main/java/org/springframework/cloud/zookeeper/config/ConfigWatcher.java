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

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.KeeperException;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import static org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type.NODE_ADDED;
import static org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type.NODE_REMOVED;
import static org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type.NODE_UPDATED;

/**
 * Class that registers a {@link TreeCache} for each context.
 * It publishes events upon element change in Zookeeper.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
public class ConfigWatcher implements Closeable, TreeCacheListener, ApplicationEventPublisherAware{

	private static final Log log = LogFactory.getLog(ConfigWatcher.class);

	private AtomicBoolean running = new AtomicBoolean(false);
	private List<String> contexts;
	private CuratorFramework source;
	private ApplicationEventPublisher publisher;
	private HashMap<String, TreeCache> caches;

	public ConfigWatcher(List<String> contexts, CuratorFramework source) {
		this.contexts = contexts;
		this.source = source;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@PostConstruct
	public void start() {
		if (this.running.compareAndSet(false, true)) {
			this.caches = new HashMap<>();
			for (String context : this.contexts) {
				if (!context.startsWith("/")) {
					context = "/" + context;
				}
				try {
					TreeCache cache = TreeCache.newBuilder(this.source, context).build();
					cache.getListenable().addListener(this);
					cache.start();
					this.caches.put(context, cache);
					// no race condition since ZookeeperAutoConfiguration.curatorFramework
					// calls curator.blockUntilConnected
				} catch (KeeperException.NoNodeException e) {
					// no node, ignore
				} catch (Exception e) {
					log.error("Error initializing listener for context " + context, e);
				}
			}
		}
	}

	@Override
	public void close() {
		if (this.running.compareAndSet(true, false)) {
			for (TreeCache cache : this.caches.values()) {
				cache.close();
			}
			this.caches = null;
		}
	}

	@Override
	public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
		TreeCacheEvent.Type eventType = event.getType();
		if (eventType == NODE_ADDED || eventType == NODE_REMOVED || eventType == NODE_UPDATED) {
			this.publisher.publishEvent(new RefreshEvent(this, event, getEventDesc(event)));
		}
	}

	public String getEventDesc(TreeCacheEvent event) {
		StringBuilder out = new StringBuilder();
		out.append("type=").append(event.getType());
		out.append(", path=").append(event.getData().getPath());
		byte[] data = event.getData().getData();
		if (data != null && data.length > 0) {
			out.append(", data=").append(new String(data, Charset.forName("UTF-8")));
		}
		return out.toString();
	}
}
