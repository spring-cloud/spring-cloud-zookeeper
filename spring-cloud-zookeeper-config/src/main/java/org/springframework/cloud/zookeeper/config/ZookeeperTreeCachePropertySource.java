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

import static org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type.*;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

/**
 * @author Spencer Gibb
 */
@CommonsLog
public class ZookeeperTreeCachePropertySource extends AbstractZookeeperPropertySource
		implements Lifecycle, TreeCacheListener {

	private TreeCache cache;
	private boolean running;
	private RefreshScope scope;
	private Map<String, Object> refreshableConfigurationProperties;

	public ZookeeperTreeCachePropertySource(String context, CuratorFramework source) {
		super(context, source);
	}

	@Override
	public void start() {
		try {
			cache = TreeCache.newBuilder(source, this.getContext()).build();
			cache.start();
			cache.getListenable().addListener(this);
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

	@Override
	public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
		if (refreshableConfigurationProperties == null) {
			// this will happen during bootstrap
			return;
		}
		Type eventType = event.getType();
		if (eventType != NODE_ADDED && eventType != NODE_REMOVED && eventType != NODE_UPDATED) {
			return;
		}
		String eventPath = event.getData().getPath();
		for (Map.Entry<String, Object> entry : refreshableConfigurationProperties.entrySet()) {
			String beanName = entry.getKey();
			Object bean = entry.getValue();
			Class<?> beanClass = AopProxyUtils.ultimateTargetClass(bean);
			ConfigurationProperties annotation = beanClass.getAnnotation(ConfigurationProperties.class);
			String beanPath = getConfigurationPath(annotation);
			String fullPath = getContext() + "/" + beanPath;
			if (!shouldRefresh(eventPath, fullPath)) {
				continue;
			}
			if (log.isDebugEnabled()) {
				log.debug(MessageFormat.format("Refreshing bean {0} of type {1} due to {2} event with path {3}",
						beanName, beanClass.getName(), eventType, eventPath));
			}
			scope.refresh(beanName);
		}
	}

	private static String getConfigurationPath(ConfigurationProperties annotation) {
		String beanPath = annotation.value();
		if (StringUtils.isEmpty(beanPath)) {
			beanPath = annotation.prefix();
		}
		return beanPath;
	}

	private static boolean shouldRefresh(String eventPath, String fullPath) {
		// don't refresh if then lengths are equal because that means we got an
		// event for the root config, not child data
		return eventPath.startsWith(fullPath) && eventPath.length() == fullPath.length();
	}

	public void setApplicationContext(ConfigurableApplicationContext context) {
		log.info("configuring " + getContext());
		scope = context.getBean(RefreshScope.class);
		refreshableConfigurationProperties = new HashMap<>();
		for (Map.Entry<String, Object> entry : context.getBeansWithAnnotation(ConfigurationProperties.class).entrySet()) {
			String beanName = entry.getKey();
			Object bean = entry.getValue();
			if (beanName.startsWith("scopedTarget.")) {
				// or should we *only* refresh beans that start w/
				// 'scopedTarget'?
				continue;
			}
			org.springframework.cloud.context.config.annotation.RefreshScope refreshAnnotation = AnnotationUtils
					.findAnnotation(bean.getClass(),
							org.springframework.cloud.context.config.annotation.RefreshScope.class);
			if (refreshAnnotation != null) {
				refreshableConfigurationProperties.put(beanName, bean);
			}
		}
	}

}
