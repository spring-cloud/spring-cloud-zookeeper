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

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * @author Marcin Grzejszczak, 4financeIT
 * @author Spencer Gibb
 */
@Data
public class ZookeeperDependency {

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
	 * <p/>
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
	 * <p/>
	 * {@link org.springframework.cloud.zookeeper.discovery.watcher.presence.DependencyPresenceOnStartupVerifier;}
	 * {@link org.springframework.cloud.zookeeper.discovery.watcher.DefaultDependencyWatcher}
	 */
	private boolean required;

	/**
	 * Colon separated notation of the stubs. E.g. {@code org.springframework:zookeeper-sample:stubs}. If not provided
	 * the {@code path} will be parsed to try to split it into groupId and artifactId. If not provided the classifier
	 * will by default equal {@code stubs}
	 */
	private String stubs;

	public ZookeeperDependency() {
	}

	public ZookeeperDependency(String path, LoadBalancerType loadBalancerType, String contentTypeTemplate,
							   String version, Map<String, Collection<String>> headers, boolean required, String stubs) {
		this.path = path;
		this.loadBalancerType = loadBalancerType;
		this.contentTypeTemplate = contentTypeTemplate;
		this.version = version;
		this.headers = headers;
		this.required = required;
		this.stubs = stubs;
	}

	/**
	 * Parsed stubs path
	 */
	private StubsConfiguration stubsConfiguration;

	public ZookeeperDependency(String path) {
		if (StringUtils.hasText(path)) {
			this.path = path;
		}
	}

	/**
	 * Function that will replace the placeholder {@link ZookeeperDependency#VERSION_PLACEHOLDER_REGEX} from the
	 * {@link ZookeeperDependency#contentTypeTemplate} with value from {@link ZookeeperDependency#version}.
	 * <p/>
	 * <p>
	 * e.g. having:
	 * <li>contentTypeTemplate: {@code 'application/vnd.some-service.$version+json'}</li>
	 * <li>version: {@code 'v1'}</li>
	 * </p>
	 * <p/>
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
