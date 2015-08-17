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
package org.springframework.cloud.zookeeper.discovery.dependency;

import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Representation of this service's dependencies in Zookeeper
 *
 * @author Marcin Grzejszczak, 4financeIT
 */
@Data
@ConfigurationProperties("spring.cloud.zookeeper")
public class ZookeeperDependencies {

	/**
	 * Common prefix that will be applied to all Zookeeper dependencies' paths
	 */
	private String prefix = "";

	/**
	 * Mapping of alias to ZookeeperDependency. From Ribbon perspective the alias
	 * is actually serviceID since Ribbon can't accept nested structures in serviceID
	 */
	private Map<String, ZookeeperDependency> dependencies = new LinkedHashMap<>();

	@PostConstruct
	public void init() {
		for (Map.Entry<String, ZookeeperDependency> entry : this.dependencies.entrySet()) {
			ZookeeperDependency value = entry.getValue();
			if (StringUtils.hasText(prefix)) {
				value.path = prefix + value.path;
			}
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ZookeeperDependency {

		private static final String VERSION_PLACEHOLDER_REGEX = "\\$version";
		private static final String CONTENT_TYPE_HEADER = "Content-Type";

		/**
		 * Path under which the dependency is registered in Zookeeper. The common prefix
		 * {@link ZookeeperDependencies#prefix} will be applied to this path
		 */
		private String path;

		/**
		 * Type of load balancer that should be used for this particular dependency
		 */
		private LoadBalancerType loadBalancerType = LoadBalancerType.ROUND_ROBIN;

		/**
		 * Content type template with {@code $version} placeholder which will be filled
		 * by the {@link ZookeeperDependency#version} variable.
		 *
		 * e.g. {@code 'application/vnd.some-service.$version+json'}
		 */
		private String contentTypeTemplate = "";

		/**
		 * Provide the current version number of the dependency. This version will be placed under the
		 * {@code $version} placeholder in {@link ZookeeperDependency#contentTypeTemplate}
		 */
		private String version = "";

		/**
		 * You can provide a map of default headers that should be attached when sending a message to the dependency
		 */
		private Map<String, Collection<String>> headers = new HashMap<>();

		/**
		 * If set to true - if the dependency is not present on startup then the application will not boot successfully
		 *
		 * {@link org.springframework.cloud.zookeeper.discovery.watcher.presence.DependencyPresenceOnStartupVerifier;}
		 * {@link org.springframework.cloud.zookeeper.discovery.watcher.DefaultDependencyWatcher}
		 */
		private boolean required;

		/**
		 * Function that will replace the placeholder {@link ZookeeperDependency#VERSION_PLACEHOLDER_REGEX} from the
		 * {@link ZookeeperDependency#contentTypeTemplate} with value from {@link ZookeeperDependency#version}.
		 *
		 * <p>
		 * e.g. having:
		 *  <li>contentTypeTemplate: {@code 'application/vnd.some-service.$version+json'}</li>
		 *  <li>version: {@code 'v1'}</li>
		 * </p>
		 *
		 * the result of the function will be {@code 'application/vnd.some-service.v1+json'}
		 *
		 * @return content type template with version
		 */
		public String getContentTypeWithVersion() {
			if (!StringUtils.hasText(contentTypeTemplate) || !StringUtils.hasText(version)) {
				return "";
			}
			return contentTypeTemplate.replaceAll(VERSION_PLACEHOLDER_REGEX, version);
		}

		public Map<String, Collection<String>> getUpdatedHeaders(Map<String, Collection<String>> headers) {
			Map<String, Collection<String>> newHeaders = new HashMap<>(headers);
			if (hasContentTypeTemplate()) {
				setContentTypeFromTemplate(newHeaders);
			}
			if (hasHeadersSet()) {
				addPredefinedHeaders(newHeaders);
			}
			return newHeaders;
		}

		private void setContentTypeFromTemplate(Map<String, Collection<String>> headers) {
			Collection<String> contentTypes = headers.get(CONTENT_TYPE_HEADER);
			if (contentTypes == null || contentTypes.isEmpty()) {
				headers.put(CONTENT_TYPE_HEADER, singletonList(getContentTypeWithVersion()));
			} else {
				contentTypes.add(getContentTypeWithVersion());
			}
		}

		private void addPredefinedHeaders(Map<String, Collection<String>> newHeaders) {
			for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
				Collection<String> value = newHeaders.get(entry.getKey());
				if (value == null || value.isEmpty()) {
					newHeaders.put(entry.getKey(), entry.getValue());
				} else {
					value.addAll(entry.getValue());
				}
			}
		}

		private boolean hasContentTypeTemplate() {
			return StringUtils.hasText(contentTypeTemplate);
		}

		private boolean hasHeadersSet() {
			return !headers.isEmpty();
		}

	}

	public Collection<ZookeeperDependency> getDependencyConfigurations() {
		return dependencies.values();
	}

	public boolean hasDependencies() {
		return !dependencies.isEmpty();
	}

	public ZookeeperDependency getDependencyForPath(final String path) {
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : dependencies.entrySet()) {
			if (zookeeperDependencyEntry.getValue().getPath().equals(path)) {
				return zookeeperDependencyEntry.getValue();
			}
		}
		return null;
	}

	public ZookeeperDependency getDependencyForAlias(final String alias) {
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : dependencies.entrySet()) {
			if (zookeeperDependencyEntry.getKey().equals(alias)) {
				return zookeeperDependencyEntry.getValue();
			}
		}
		return null;
	}

	public String getPathForAlias(final String alias) {
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : dependencies.entrySet()) {
			if (zookeeperDependencyEntry.getKey().equals(alias)) {
				return zookeeperDependencyEntry.getValue().getPath();
			}
		}
		return "";
	}

	public String getAliasForPath(final String path) {
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : dependencies.entrySet()) {
			if (zookeeperDependencyEntry.getValue().getPath().equals(path)) {
				return zookeeperDependencyEntry.getKey();
			}
		}
		return "";
	}
}
