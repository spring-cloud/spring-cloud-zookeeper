/*
 * Copyright 2013-2017 the original author or authors.
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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;

/**
 * A specific {@link ServiceInstance} describing a zookeeper service instance
 *
 * @author Reda.Housni-Alaoui
 * @since 1.1.0
 */
public class ZookeeperServiceInstance implements ServiceInstance {

	private final String serviceId;
	private final String host;
	private final int port;
	private final boolean secure;
	private final URI uri;
	private final Map<String, String> metadata;
	private final org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> serviceInstance;

	/**
	 * @param serviceId The service id to be used
	 * @param serviceInstance The zookeeper service instance described by this service instance
	 */
	public ZookeeperServiceInstance(String serviceId, org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> serviceInstance) {
		this.serviceId = serviceId;
		this.serviceInstance = serviceInstance;
		this.host = this.serviceInstance.getAddress();
		this.secure = serviceInstance.getSslPort() != null;
		Integer port = serviceInstance.getPort();
		if (this.secure) {
			port = serviceInstance.getSslPort();
		}
		this.port = port;
		this.uri = URI.create(serviceInstance.buildUriSpec());
		if (serviceInstance.getPayload() != null) {
			this.metadata = serviceInstance.getPayload().getMetadata();
		} else {
			this.metadata = new HashMap<>();
		}
	}

	@Override
	public String getServiceId() {
		return this.serviceId;
	}

	@Override
	public String getHost() {
		return this.host;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public boolean isSecure() {
		return this.secure;
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public Map<String, String> getMetadata() {
		return this.metadata;
	}

	public org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> getServiceInstance() {
		return this.serviceInstance;
	}
}
