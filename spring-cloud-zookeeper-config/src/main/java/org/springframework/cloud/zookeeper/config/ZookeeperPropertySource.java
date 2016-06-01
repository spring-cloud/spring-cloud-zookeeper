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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.springframework.util.ReflectionUtils;

/**
 * {@link org.springframework.core.env.PropertySource} that stores properties
 * from Zookeeper inside a map. Properties are loaded upon class initialization.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
public class ZookeeperPropertySource extends AbstractZookeeperPropertySource {

	private static final Log log = LogFactory.getLog(ZookeeperPropertySource.class);

	private Map<String, String> properties = new LinkedHashMap<>();

	public ZookeeperPropertySource(String context, CuratorFramework source) {
		super(context, source);
		findProperties(this.getContext(), null);
	}

	@Override
	public Object getProperty(String name) {
		return this.properties.get(name);
	}

	private byte[] getPropertyBytes(String fullPath) {
		try {
			byte[] bytes = null;
			try {
				bytes = this.getSource().getData().forPath(fullPath);
			} catch (KeeperException e) {
				if (e.code() != KeeperException.Code.NONODE) { // not found
					throw e;
				}
			}
			return bytes;
		} catch (Exception exception) {
			ReflectionUtils.rethrowRuntimeException(exception);
		}
		return null;
	}

	@Override
	public String[] getPropertyNames() {
		Set<String> strings = this.properties.keySet();
		return strings.toArray(new String[strings.size()]);
	}

	private void findProperties(String path, List<String> children) {
		try {
			log.trace("entering findProperties for path: " + path);
			if (children == null) {
				children = getChildren(path);
			}
			if (children == null || children.isEmpty()) {
				return;
			}
			for (String child : children) {
				String childPath = path + "/" + child;
				List<String> childPathChildren = getChildren(childPath);

				byte[] bytes = getPropertyBytes(childPath);
				if (bytes == null || bytes.length == 0) {
					if (childPathChildren == null || childPathChildren.isEmpty()) {
						registerKeyValue(childPath, "");
					}
				} else {
					registerKeyValue(childPath, new String(bytes, Charset.forName("UTF-8")));
				}

				// Check children even if we have found a value for the current znode
				findProperties(childPath, childPathChildren);
			}
			log.trace("leaving findProperties for path: " + path);
		} catch (Exception exception) {
			ReflectionUtils.rethrowRuntimeException(exception);
		}
	}

	private void registerKeyValue(String path, String value) {
		String key = sanitizeKey(path);
		this.properties.put(key, value);
	}

	private List<String> getChildren(String path) throws Exception {
		List<String> children = null;
		try {
			children = this.getSource().getChildren().forPath(path);
		} catch (KeeperException e) {
			if (e.code() != KeeperException.Code.NONODE) { // not found
				throw e;
			}
		}
		return children;
	}

}
