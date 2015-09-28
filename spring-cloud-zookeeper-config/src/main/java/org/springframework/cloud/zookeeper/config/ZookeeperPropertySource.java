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

package org.springframework.cloud.zookeeper.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.core.env.EnumerablePropertySource;

/**
 * @author Spencer Gibb
 */
public class ZookeeperPropertySource extends EnumerablePropertySource<CuratorFramework>
		implements Lifecycle {
	private static final Logger LOG = LoggerFactory
			.getLogger(ZookeeperPropertySource.class);

	private String context;
	private ZookeeperConfigProperties properties;

	private TreeCache cache;
	private boolean running;

	public ZookeeperPropertySource(String context, CuratorFramework source,
			ZookeeperConfigProperties properties) {
		super(context, source);
		this.context = context;
		this.properties = properties;

		if (!this.context.startsWith("/")) {
			this.context = "/" + this.context;
		}
	}

	@Override
	public void start() {
		if (!this.properties.isCacheEnabled()) {
			return;
		}
		try {
			cache = TreeCache.newBuilder(source, context).build();
			cache.start();
			running = true;
			/*
			 * TODO: race condition since TreeCache.process(..) is invoked asynchronously.
			 * Methods getProperty and getPropertyNames could be invoked before that
			 * TreeCache.process(..) receives all the WatchedEvents. see
			 * https://github.com/spring-cloud/spring-cloud-zookeeper/issues/39
			 */
		}
		catch (NoNodeException e) {
			// no node, ignore
		}
		catch (Exception e) {
			LOG.error("Error initializing ZookeperPropertySource", e);
		}
	}

	@Override
	public Object getProperty(String name) {
		String fullPath = context + "/" + name.replace(".", "/");
		byte[] bytes = null;
		if (this.properties.isCacheEnabled()) {
			bytes = getPropertyCached(fullPath);
		}
		else {
			bytes = getPropertyNoCache(fullPath);
		}
		if (bytes == null)
			return null;
		return new String(bytes, Charset.forName("UTF-8"));
	}

	private byte[] getPropertyCached(String fullPath) {
		byte[] bytes = null;
		ChildData data = cache.getCurrentData(fullPath);
		if (data != null) {
			bytes = data.getData();
		}
		return bytes;
	}

	@SneakyThrows
	private byte[] getPropertyNoCache(String fullPath) {
		byte[] bytes = null;
		try {
			bytes = this.getSource().getData().forPath(fullPath);
		}
		catch (KeeperException e) {
			if (e.code() != KeeperException.Code.NONODE) { // not found
				throw e;
			}
		}
		return bytes;
	}

	@Override
	public String[] getPropertyNames() {
		List<String> keys = new ArrayList<>();
		findKeys(keys, context);
		return keys.toArray(new String[0]);
	}

	protected void findKeys(List<String> keys, String path) {
		if (this.properties.isCacheEnabled()) {
			findKeysCached(keys, path);
		}
		else {
			findKeysNoCache(keys, path);
		}
	}

	private void findKeysCached(List<String> keys, String path) {
		Map<String, ChildData> children = cache.getCurrentChildren(path);

		if (children == null)
			return;
		for (Map.Entry<String, ChildData> entry : children.entrySet()) {
			ChildData child = entry.getValue();
			if (child.getData() == null || child.getData().length == 0) {
				findKeysCached(keys, child.getPath());
			}
			else {
				keys.add(sanitizeKey(child.getPath()));
			}
		}
	}

	private String sanitizeKey(String path) {
		return path.replace(context + "/", "").replace('/', '.');
	}

	@SneakyThrows
	private void findKeysNoCache(List<String> keys, String path) {
		List<String> children = null;
		try {
			children = this.getSource().getChildren().forPath(path);
		}
		catch (KeeperException e) {
			if (e.code() != KeeperException.Code.NONODE) { // not found
				throw e;
			}
		}
		if (children == null || children.isEmpty()) {
			return;
		}

		for (String child : children) {
			String childPath = path + "/" + child;
			byte[] property = getPropertyNoCache(childPath);
			if (property == null || property.length == 0) {
				findKeysNoCache(keys, childPath);
			}
			else {
				keys.add(sanitizeKey(childPath));
			}
		}
	}

	@Override
	public void stop() {
		if (this.properties.isCacheEnabled() && cache != null) {
			cache.close();
			running = false;
		}
	}

	@Override
	public boolean isRunning() {
		return running;
	}

}
