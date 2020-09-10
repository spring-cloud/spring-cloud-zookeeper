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

package org.springframework.cloud.zookeeper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * Properties related to keeping configuration in Zookeeper.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 * @see ZookeeperPropertySourceLocator
 */
@ConfigurationProperties(ZookeeperConfigProperties.PREFIX)
public class ZookeeperConfigProperties {

	/**
	 * Configuration prefix for config properties.
	 */
	public static final String PREFIX = "spring.cloud.zookeeper.config";

	private boolean enabled = true;

	/**
	 * Root folder where the configuration for Zookeeper is kept.
	 */
	private String root = "config";

	/**
	 * Alternative to spring.application.name to use in looking up values in zookeeper.
	 */
	private String name;

	/**
	 * The name of the default context.
	 */
	private String defaultContext = "application";

	/**
	 * Separator for profile appended to the application name.
	 */
	private String profileSeparator = ",";

	/**
	 * Throw exceptions during config lookup if true, otherwise, log warnings.
	 */
	private boolean failFast = true;

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getRoot() {
		return this.root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefaultContext() {
		return this.defaultContext;
	}

	public void setDefaultContext(String defaultContext) {
		Assert.hasText(defaultContext, "spring.cloud.zookeeper.config.default-context may not be empty");
		this.defaultContext = defaultContext;
	}

	public String getProfileSeparator() {
		return this.profileSeparator;
	}

	public void setProfileSeparator(String profileSeparator) {
		Assert.hasText(profileSeparator, "spring.cloud.zookeeper.config.profile-separator may not be empty");
		this.profileSeparator = profileSeparator;
	}

	public boolean isFailFast() {
		return this.failFast;
	}

	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this)
				.append("enabled", enabled)
				.append("root", root)
				.append("name", name)
				.append("defaultContext", defaultContext)
				.append("profileSeparator", profileSeparator)
				.append("failFast", failFast)
				.toString();

	}
}
