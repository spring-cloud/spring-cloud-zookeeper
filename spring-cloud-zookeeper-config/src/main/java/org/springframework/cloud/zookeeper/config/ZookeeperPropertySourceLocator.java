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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * @author Spencer Gibb
 */
public class ZookeeperPropertySourceLocator implements PropertySourceLocator {

	private ZookeeperConfigProperties properties;

	private CuratorFramework curator;

	private ConcurrentHashMap<String, ZookeeperTreeCachePropertySource> lifecycleSources = new ConcurrentHashMap<>();

	public ZookeeperPropertySourceLocator(CuratorFramework curator, ZookeeperConfigProperties properties) {
		this.curator = curator;
		this.properties = properties;
	}

	@Override
	public PropertySource<?> locate(Environment environment) {
		if (environment instanceof ConfigurableEnvironment) {
			ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
			String appName = env.getProperty("spring.application.name");
			List<String> profiles = Arrays.asList(env.getActiveProfiles());

			String root = properties.getRoot();
			List<String> contexts = new ArrayList<>();

			
			String[] parentContexts=properties.getDefaultContext().split(",");
			for (String parentContext : parentContexts) {
				String defaultContext = root + "/" + parentContext.trim();
				contexts.add(defaultContext);
				addProfiles(contexts, defaultContext, profiles);
			}

			String baseContext = root + "/" + appName;
			contexts.add(baseContext);
			addProfiles(contexts, baseContext, profiles);

			CompositePropertySource composite = new CompositePropertySource("zookeeper");

			Collections.reverse(contexts);

			for (String propertySourceContext : contexts) {
				PropertySource propertySource = create(propertySourceContext);
				composite.addPropertySource(propertySource);
				// TODO: howto call close when /refresh
			}

			return composite;
		}
		return null;
	}

	@PreDestroy
	public void destroy() {
		for (ZookeeperTreeCachePropertySource source : this.lifecycleSources.values()) {
			source.stop();
		}
	}

	private PropertySource<CuratorFramework> create(String context) {
		if (this.properties.isCacheEnabled()) {
			ZookeeperTreeCachePropertySource propertySource = new ZookeeperTreeCachePropertySource(context, curator);
			propertySource.start();
			lifecycleSources.put(propertySource.getName(), propertySource);
			return propertySource;
		}
		return new ZookeeperPropertySource(context, curator);
	}

	private void addProfiles(List<String> contexts, String baseContext,
			List<String> profiles) {
		for (String profile : profiles) {
			contexts.add(baseContext + properties.getProfileSeparator() + profile);
		}
	}
}
