/*
 * Copyright 2015-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.zookeeper.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.curator.framework.CuratorFramework;

import org.springframework.core.style.ToStringCreator;

public class ZookeeperPropertySources {
	private final ZookeeperConfigProperties properties;
	private final Log log;

	public ZookeeperPropertySources(ZookeeperConfigProperties properties, Log log) {
		this.properties = properties;
		this.log = log;
	}

	public List<String> getAutomaticContexts(List<String> profiles) {
		return getAutomaticContexts(profiles, true);
	}

	public List<String> getAutomaticContexts(List<String> profiles, boolean reverse) {
		return generateAutomaticContexts(profiles, reverse).stream().map(Context::getPath).collect(Collectors.toList());
	}

	public List<Context> generateAutomaticContexts(List<String> profiles, boolean reverse) {
		List<Context> contexts = new ArrayList<>();
		String root = properties.getRoot();

		String defaultContext = root + "/" + properties.getDefaultContext();
		contexts.add(new Context(defaultContext));
		addProfiles(contexts, defaultContext, profiles);

		StringBuilder baseContext = new StringBuilder(root);
		if (!properties.getName().startsWith("/")) {
			baseContext.append("/");
		}
		// getName() defaults to ${spring.application.name} or application
		baseContext.append(properties.getName());
		contexts.add(new Context(baseContext.toString()));
		addProfiles(contexts, baseContext.toString(), profiles);

		if (reverse) {
			Collections.reverse(contexts);
		}
		return contexts;
	}

	private void addProfiles(List<Context> contexts, String baseContext, List<String> profiles) {
		for (String profile : profiles) {
			String path = baseContext + properties.getProfileSeparator() + profile;
			contexts.add(new Context(path, profile));
		}
	}

	public ZookeeperPropertySource createPropertySource(String context, boolean optional, CuratorFramework curator) {
		try {
			return new ZookeeperPropertySource(context, curator);
			// TODO: howto call close when /refresh
		}
		catch (Exception e) {
			if (this.properties.isFailFast() || !optional) {
				throw new ZookeeperPropertySourceNotFoundException(e);
			}
			else {
				log.warn("Unable to load zookeeper config from " + context, e);
			}
		}
		return null;
	}
	public static class Context {

		private final String path;

		private final String profile;

		public Context(String path) {
			this.path = path;
			this.profile = null;
		}

		public Context(String path, String profile) {
			this.path = path;
			this.profile = profile;
		}

		public String getPath() {
			return this.path;
		}

		public String getProfile() {
			return this.profile;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("path", path).append("profile", profile).toString();

		}

	}


	static class ZookeeperPropertySourceNotFoundException extends RuntimeException {

		ZookeeperPropertySourceNotFoundException(Exception source) {
			super(source);
		}
	}
}
