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

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties related to Zookeeper's Service Discovery.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
@ConfigurationProperties("spring.cloud.zookeeper.discovery")
public class ZookeeperDiscoveryProperties {
	private boolean enabled = true;

	/**
	 * Root Zookeeper folder in which all instances are registered
	 */
	private String root = "/services";

	/**
	 * The URI specification to resolve during service registration in Zookeeper
	 */
	private String uriSpec = "{scheme}://{address}:{port}";

	/**
	 * Predefined host with which a service can register itself in Zookeeper. Corresponds
	 * to the {code address} from the URI spec.
	 */
	private String instanceHost;

	/**
	 * Gets the metadata name/value pairs associated with this instance. This information
	 * is sent to zookeeper and can be used by other instances.
	 */
	private Map<String, String> metadata = new HashMap<>();

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

	public Map<String, String> getMetadata() {
		return this.metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "ZookeeperDiscoveryProperties{" + "enabled=" + this.enabled +
				", root='" + this.root + '\'' +
				", uriSpec='" + this.uriSpec + '\'' +
				", instanceHost='" + this.instanceHost + '\'' +
				", metadata=" + this.metadata +
				'}';
	}
}
