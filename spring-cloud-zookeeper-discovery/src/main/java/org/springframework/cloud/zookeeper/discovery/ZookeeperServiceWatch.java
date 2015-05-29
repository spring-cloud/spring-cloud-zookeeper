package org.springframework.cloud.zookeeper.discovery;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;

import lombok.SneakyThrows;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;

/**
 * @author Spencer Gibb
 */
public class ZookeeperServiceWatch implements
		ApplicationListener<InstanceRegisteredEvent>, TreeCacheListener,
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

	@Override
	@SneakyThrows
	public void onApplicationEvent(InstanceRegisteredEvent event) {
		cache = TreeCache.newBuilder(curator, properties.getRoot()).build();
		cache.getListenable().addListener(this);
		cache.start();
	}

	@PreDestroy
	public void stop() throws Exception {
		if (cache != null) {
			cache.close();
		}
	}

	@Override
	public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
		if (event.getType().equals(TreeCacheEvent.Type.NODE_ADDED)
				|| event.getType().equals(TreeCacheEvent.Type.NODE_REMOVED)
				|| event.getType().equals(TreeCacheEvent.Type.NODE_UPDATED)) {
			long newCacheChange = cacheChange.incrementAndGet();
			publisher.publishEvent(new HeartbeatEvent(this, newCacheChange));
		}
	}
}
