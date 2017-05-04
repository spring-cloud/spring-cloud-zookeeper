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

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Properties related to keeping configuration in Zookeeper.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 *
 * @see ZookeeperPropertySourceLocator
 */
@Validated
@ConfigurationProperties("spring.cloud.zookeeper.config")
public class ZookeeperConfigProperties {
	private boolean enabled = true;

	/**
	 * Root folder where the configuration for Zookeeper is kept
	 */
	private String root = "config";

	/**
	 * The name of the default context
	 */
	@NotEmpty
	private String defaultContext = "application";

	/**
	 * Separator for profile appended to the application name
	 */
	@NotEmpty
	private String profileSeparator = ",";

	/**
	 * Throw exceptions during config lookup if true, otherwise, log warnings.
	 */
	private boolean failFast = true;

	public boolean isEnabled() {
		return this.enabled;
	}

	public String getRoot() {
		return this.root;
	}

	public String getDefaultContext() {
		return this.defaultContext;
	}

	public String getProfileSeparator() {
		return this.profileSeparator;
	}

	public boolean isFailFast() {
		return this.failFast;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public void setDefaultContext(String defaultContext) {
		this.defaultContext = defaultContext;
	}

	public void setProfileSeparator(String profileSeparator) {
		this.profileSeparator = profileSeparator;
	}

	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}
}
