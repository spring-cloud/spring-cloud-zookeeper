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

package org.springframework.cloud.zookeeper.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.zookeeper.discovery")
public class ZookeeperDiscoveryProperties {
	private boolean enabled = true;

	private String root = "/services";

	private String uriSpec = "{scheme}://{address}:{port}";

	private String instanceHost;

	public boolean isEnabled() {
		return this.enabled;
	}

	public String getRoot() {
		return this.root;
	}

	public String getUriSpec() {
		return this.uriSpec;
	}

	public String getInstanceHost() {
		return this.instanceHost;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public void setUriSpec(String uriSpec) {
		this.uriSpec = uriSpec;
	}

	public void setInstanceHost(String instanceHost) {
		this.instanceHost = instanceHost;
	}
}
