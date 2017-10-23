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
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.zookeeper.support.StatusConstants;
import org.springframework.util.StringUtils;

/**
 * Properties related to Zookeeper's Service Discovery.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
@ConfigurationProperties("spring.cloud.zookeeper.discovery")
public class ZookeeperDiscoveryProperties {

	public static final String DEFAULT_URI_SPEC = "{scheme}://{address}:{port}";

	private InetUtils.HostInfo hostInfo;

	private boolean enabled = true;

	/**
	 * Root Zookeeper folder in which all instances are registered
	 */
	private String root = "/services";

	/**
	 * The URI specification to resolve during service registration in Zookeeper
	 */
	private String uriSpec = DEFAULT_URI_SPEC;

	/** Id used to register with zookeeper. Defaults to a random UUID. */
	private String instanceId;

	/**
	 * Predefined host with which a service can register itself in Zookeeper. Corresponds
	 * to the {code address} from the URI spec.
	 */
	private String instanceHost;

	/** IP address to use when accessing service (must also set preferIpAddress
            to use) */
	private String instanceIpAddress;

	/**
	 * Use ip address rather than hostname during registration
	 */
	private boolean preferIpAddress = false;

	/** Port to register the service under (defaults to listening port) */
	private Integer instancePort;

	/** Ssl port of the registered service. */
	private Integer instanceSslPort;

	/**
	 * Register as a service in zookeeper.
	 */
	private boolean register = true;

	/**
	 * Gets the metadata name/value pairs associated with this instance. This information
	 * is sent to zookeeper and can be used by other instances.
	 */
	private Map<String, String> metadata = new HashMap<>();

	/**
	 * The initial status of this instance (defaults to {@link StatusConstants#STATUS_UP}).
	 */
	private String initialStatus = StatusConstants.STATUS_UP;

	@SuppressWarnings("unused")
	private ZookeeperDiscoveryProperties() {}

	public ZookeeperDiscoveryProperties(InetUtils inetUtils) {
		this.hostInfo = inetUtils.findFirstNonLoopbackHostInfo();
		this.instanceHost = this.hostInfo.getHostname();
		this.instanceIpAddress = this.hostInfo.getIpAddress();
	}

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
		if (this.preferIpAddress && StringUtils.hasText(this.instanceIpAddress)) {
			return this.instanceIpAddress;
		}
		return this.instanceHost;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setRoot(String root) {
		this.root = DependencyPathUtils.sanitize(root);
	}

	public void setUriSpec(String uriSpec) {
		this.uriSpec = uriSpec;
	}

	public String getInstanceId() {
		return this.instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public void setInstanceHost(String instanceHost) {
		this.instanceHost = instanceHost;
		this.hostInfo.override = true;
	}

	public void setInstanceIpAddress(String instanceIpAddress) {
		this.instanceIpAddress = instanceIpAddress;
		this.hostInfo.override = true;
	}

	public void setPreferIpAddress(boolean preferIpAddress) {
		this.preferIpAddress = preferIpAddress;
	}

	public Map<String, String> getMetadata() {
		return this.metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public boolean isRegister() {
		return this.register;
	}

	public void setRegister(boolean register) {
		this.register = register;
	}

	public Integer getInstancePort() {
		return this.instancePort;
	}

	public void setInstancePort(Integer instancePort) {
		this.instancePort = instancePort;
	}

	public Integer getInstanceSslPort() {
		return this.instanceSslPort;
	}

	public void setInstanceSslPort(Integer instanceSslPort) {
		this.instanceSslPort = instanceSslPort;
	}

	public String getInitialStatus() {
		return this.initialStatus;
	}

	public void setInitialStatus(String initialStatus) {
		this.initialStatus = initialStatus;
	}

	@Override
	public String toString() {
		return "ZookeeperDiscoveryProperties{" + "enabled=" + this.enabled +
				", root='" + this.root + '\'' +
				", uriSpec='" + this.uriSpec + '\'' +
				", instanceId='" + this.instanceId + '\'' +
				", instanceHost='" + this.instanceHost + '\'' +
				", instancePort='" + this.instancePort + '\'' +
				", instanceSslPort='" + this.instanceSslPort + '\'' +
				", metadata=" + this.metadata +
				", register=" + this.register +
				", initialStatus=" + this.initialStatus +
				'}';
	}
}
