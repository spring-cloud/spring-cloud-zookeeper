/*
 * Copyright 2012-2015 the original author or authors.
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Marcin Grzejszczak, 4financeIT
 */
@Data
@ConfigurationProperties("zookeeper")
public class ZookeeperDependencies {

	private String prefix = "";

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

		private String id;

		private String path;

		private LoadBalancerType loadBalancerType;

		private String contentTypeTemplate;

		private String version;

		private Map<String, String> headers;

		private boolean required;
	}

	public Collection<ZookeeperDependency> getDependencyConfigurations() {
		return dependencies.values();
	}

	public boolean hasDependencies() {
		return !dependencies.isEmpty();
	}

	public String getPathForAlias(final String alias) {
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : dependencies.entrySet()) {
			if (zookeeperDependencyEntry.getKey().equals(alias)) {
				return zookeeperDependencyEntry.getValue().getPath();
			}
		}
		return "";
	}
}
