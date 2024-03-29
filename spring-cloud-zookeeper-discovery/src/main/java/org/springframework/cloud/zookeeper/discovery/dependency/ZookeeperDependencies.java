/*
 * Copyright 2015-2019 the original author or authors.
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

package org.springframework.cloud.zookeeper.discovery.dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.zookeeper.discovery.dependency.StubsConfiguration.DependencyPath;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.zookeeper.discovery.DependencyPathUtils.sanitize;

/**
 * Representation of this service's dependencies in Zookeeper.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 1.0.0
 */
@ConfigurationProperties(ZookeeperDependencies.PREFIX)
public class ZookeeperDependencies {

	/**
	 * Zookeeper Dependencies prefix.
	 */
	public static final String PREFIX = "spring.cloud.zookeeper";

	/**
	 * Common prefix that will be applied to all Zookeeper dependencies' paths.
	 */
	private String prefix = "";

	/**
	 * Mapping of alias to ZookeeperDependency. From LoadBalancer perspective the alias is
	 * actually serviceID since SC LoadBalancer can't accept nested structures in serviceID.
	 */
	private Map<String, ZookeeperDependency> dependencies = new LinkedHashMap<>();

	@PostConstruct
	public void init() {
		if (StringUtils.hasText(this.prefix)) {
			this.prefix = sanitize(this.prefix);
		}
		for (Map.Entry<String, ZookeeperDependency> entry : this.dependencies
				.entrySet()) {
			ZookeeperDependency value = entry.getValue();

			if (!StringUtils.hasText(value.getPath())) {
				value.setPath(entry.getKey());
			}

			value.setPath(sanitize(value.getPath()));

			if (StringUtils.hasText(this.prefix)) {
				value.setPath(this.prefix + value.getPath());
			}

			setStubDefinition(value);
		}
	}

	private void setStubDefinition(ZookeeperDependency value) {
		if (!StringUtils.hasText(value.getStubs())) {
			value.setStubsConfiguration(
					new StubsConfiguration(new DependencyPath(value.getPath())));
		}
		else {
			value.setStubsConfiguration(new StubsConfiguration(value.getStubs()));
		}
	}

	public Collection<ZookeeperDependency> getDependencyConfigurations() {
		return this.dependencies.values();
	}

	public boolean hasDependencies() {
		return !this.dependencies.isEmpty();
	}

	public ZookeeperDependency getDependencyForPath(final String path) {
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : this.dependencies
				.entrySet()) {
			if (zookeeperDependencyEntry.getValue().getPath().equals(path)) {
				return zookeeperDependencyEntry.getValue();
			}
		}
		return null;
	}

	public ZookeeperDependency getDependencyForAlias(final String alias) {
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : this.dependencies
				.entrySet()) {
			if (zookeeperDependencyEntry.getKey().equals(alias)) {
				return zookeeperDependencyEntry.getValue();
			}
		}
		return null;
	}

	public String getPathForAlias(final String alias) {
		ZookeeperDependency dependency = getDependencyForAlias(alias);
		if (dependency != null) {
			return dependency.getPath();
		}
		return "";
	}

	public String getAliasForPath(final String path) {
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : this.dependencies
				.entrySet()) {
			if (zookeeperDependencyEntry.getValue().getPath().equals(path)) {
				return zookeeperDependencyEntry.getKey();
			}
		}
		return "";
	}

	public Collection<String> getDependencyNames() {
		List<String> names = new ArrayList<>();
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : this.dependencies
				.entrySet()) {
			names.add(zookeeperDependencyEntry.getValue().getPath());
		}
		return names;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public Map<String, ZookeeperDependency> getDependencies() {
		return this.dependencies;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setDependencies(Map<String, ZookeeperDependency> dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("ZookeeperDependencies{");
		sb.append("prefix='").append(this.prefix).append('\'');
		sb.append(", dependencies=").append(this.dependencies);
		sb.append('}');
		return sb.toString();
	}

}
