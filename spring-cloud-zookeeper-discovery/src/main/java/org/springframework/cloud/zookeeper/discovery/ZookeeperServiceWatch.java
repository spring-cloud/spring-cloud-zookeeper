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

package org.springframework.cloud.zookeeper.discovery;

import javax.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link TreeCacheListener} that sends {@link HeartbeatEvent} when an
 * entry inside Zookeeper has changed.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
public class ZookeeperServiceWatch implements
		ApplicationListener<InstanceRegisteredEvent<?>>, TreeCacheListener,
		ApplicationEventPublisherAware {

	private final CuratorFramework curator;
	private final ZookeeperDiscoveryProperties properties;
	private final AtomicLong cacheChange = new AtomicLong(0);
	private ApplicationEventPublisher publisher;
	private TreeCache cache;

	public ZookeeperServiceWatch(CuratorFramework curator,
			ZookeeperDiscoveryProperties properties) {
		this.curator = curator;
		this.properties = properties;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	public TreeCache getCache() {
		return this.cache;
	}

	@Override
	public void onApplicationEvent(InstanceRegisteredEvent<?> event) {
		this.cache = TreeCache.newBuilder(this.curator, this.properties.getRoot()).build();
		this.cache.getListenable().addListener(this);
		try {
			this.cache.start();
		}
		catch (Exception e) {
			ReflectionUtils.rethrowRuntimeException(e);
		}
	}

	@PreDestroy
	public void stop() throws Exception {
		if (this.cache != null) {
			this.cache.close();
		}
	}

	@Override
	public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
		if (event.getType().equals(TreeCacheEvent.Type.NODE_ADDED)
				|| event.getType().equals(TreeCacheEvent.Type.NODE_REMOVED)
				|| event.getType().equals(TreeCacheEvent.Type.NODE_UPDATED)) {
			long newCacheChange = this.cacheChange.incrementAndGet();
			this.publisher.publishEvent(new HeartbeatEvent(this, newCacheChange));
		}
	}
}
