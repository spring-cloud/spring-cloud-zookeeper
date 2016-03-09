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

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.zookeeper.discovery.dependency.StubsConfiguration.DependencyPath;
import org.springframework.util.StringUtils;

import lombok.Data;

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

	/**
	 * Default health endpoint that will be checked to verify that a dependency is alive
	 */
	@Value("${spring.cloud.zookeeper.dependencies.ribbon.loadbalancer.defaulthealthendpoint:/health}")
	private String defaultHealthEndpoint;

	@PostConstruct
	public void init() {
		if (StringUtils.hasText(this.prefix) && !this.prefix.endsWith("/")) {
			this.prefix = this.prefix + "/";
		}
		for (Map.Entry<String, ZookeeperDependency> entry : this.dependencies.entrySet()) {
			ZookeeperDependency value = entry.getValue();

			if (!StringUtils.hasText(value.getPath())) {
				value.setPath(entry.getKey());
			}

			if (StringUtils.hasText(this.prefix)) {
				value.setPath(this.prefix + value.getPath());
			}

			setStubDefinition(value);
		}
	}

	private void setStubDefinition(ZookeeperDependency value) {
		if (!StringUtils.hasText(value.getStubs())) {
			value.setStubsConfiguration(new StubsConfiguration(new DependencyPath(value.getPath())));
		} else {
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
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : this.dependencies.entrySet()) {
			if (zookeeperDependencyEntry.getValue().getPath().equals(path)) {
				return zookeeperDependencyEntry.getValue();
			}
		}
		return null;
	}

	public ZookeeperDependency getDependencyForAlias(final String alias) {
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : this.dependencies.entrySet()) {
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
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : this.dependencies.entrySet()) {
			if (zookeeperDependencyEntry.getValue().getPath().equals(path)) {
				return zookeeperDependencyEntry.getKey();
			}
		}
		return "";
	}

	public Collection<String> getDependencyNames() {
		List<String> names = new ArrayList<>();
		for (Map.Entry<String, ZookeeperDependency> zookeeperDependencyEntry : this.dependencies.entrySet()) {
			names.add(zookeeperDependencyEntry.getValue().getPath());
		}
		return names;
	}
}
