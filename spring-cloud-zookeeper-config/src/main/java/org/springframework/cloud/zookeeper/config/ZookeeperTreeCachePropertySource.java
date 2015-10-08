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

import lombok.extern.apachecommons.CommonsLog;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.springframework.context.Lifecycle;

/**
 * @author Spencer Gibb
 */
@CommonsLog
public class ZookeeperTreeCachePropertySource extends AbstractZookeeperPropertySource
		implements Lifecycle {

	private TreeCache cache;
	private boolean running;

	public ZookeeperTreeCachePropertySource(String context, CuratorFramework source) {
		super(context, source);
	}

	@Override
	public void start() {
		try {
			cache = TreeCache.newBuilder(source, this.getContext()).build();
			cache.start();
			running = true;
			// no race condition since ZookeeperAutoConfiguration.curatorFramework
			// calls curator.blockUntilConnected
		}
		catch (NoNodeException e) {
			// no node, ignore
		}
		catch (Exception e) {
			log.error("Error initializing ZookeperPropertySource", e);
		}
	}

	@Override
	public Object getProperty(String name) {
		String fullPath = this.getContext() + "/" + name.replace(".", "/");
		byte[] bytes = null;
		ChildData data = cache.getCurrentData(fullPath);
		if (data != null) {
			bytes = data.getData();
		}
		if (bytes == null)
			return null;
		return new String(bytes, Charset.forName("UTF-8"));
	}

	@Override
	public String[] getPropertyNames() {
		List<String> keys = new ArrayList<>();
		findKeys(keys, this.getContext());
		return keys.toArray(new String[0]);
	}

	protected void findKeys(List<String> keys, String path) {
		log.trace("enter findKeysCached for path: " + path);
		Map<String, ChildData> children = cache.getCurrentChildren(path);

		if (children == null)
			return;
		for (Map.Entry<String, ChildData> entry : children.entrySet()) {
			ChildData child = entry.getValue();
			if (child.getData() == null || child.getData().length == 0) {
				findKeys(keys, child.getPath());
			}
			else {
				keys.add(sanitizeKey(child.getPath()));
			}
		}
		log.trace("leaving findKeysCached for path: " + path);
	}


	@Override
	public void stop() {
		cache.close();
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

}
