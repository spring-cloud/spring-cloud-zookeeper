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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;

/**
 * @author Spencer Gibb
 */
@CommonsLog
public class ZookeeperPropertySource extends AbstractZookeeperPropertySource {

	private Map<String, String> properties = new LinkedHashMap<>();

	public ZookeeperPropertySource(String context, CuratorFramework source) {
		super(context, source);

		findProperties(this.getContext());
	}

	@Override
	public Object getProperty(String name) {
		return this.properties.get(name);
	}

	@SneakyThrows
	private byte[] getPropertyBytes(String fullPath) {
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
		Set<String> strings = this.properties.keySet();
		return strings.toArray(new String[strings.size()]);
	}

	@SneakyThrows
	private void findProperties(String path) {
		log.trace("entering findProperties for path: " + path);
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
			byte[] bytes = getPropertyBytes(childPath);
			if (bytes == null || bytes.length == 0) {
				findProperties(childPath);
			}
			else {
				String key = sanitizeKey(childPath);
				this.properties.put(key, new String(bytes, Charset.forName("UTF-8")));
			}
		}
		log.trace("leaving findProperties for path: " + path);
	}

}
