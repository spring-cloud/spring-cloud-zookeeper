/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.zookeeper.serviceregistry;

import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.UriSpec;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties.DEFAULT_URI_SPEC;

/**
 * {@link org.springframework.cloud.client.serviceregistry.Registration} that lazily builds
 * a {@link ServiceInstance} so the port can by dynamically set (for instance, when the
 * user wants a dynamic port for spring boot.
 *
 * @author Spencer Gibb
 */
public class ServiceInstanceRegistration implements ZookeeperRegistration {

	public static RegistrationBuilder builder() {
		try {
			return new RegistrationBuilder(ServiceInstance.<ZookeeperInstance>builder());
		} catch (Exception e) {
			throw new RuntimeException("Error creating ServiceInstanceBuilder", e);
		}
	}

	public static RegistrationBuilder builder(ServiceInstanceBuilder<ZookeeperInstance> builder) {
		return new RegistrationBuilder(builder);
	}

	public static class RegistrationBuilder {
		protected ServiceInstanceBuilder<ZookeeperInstance> builder;

		public RegistrationBuilder(ServiceInstanceBuilder<ZookeeperInstance> builder) {
			this.builder = builder;
		}

		public ServiceInstanceRegistration build() {
			return new ServiceInstanceRegistration(this.builder);
		}

		public RegistrationBuilder name(String name) {
			this.builder.name(name);
			return this;
		}

		public RegistrationBuilder address(String address) {
			this.builder.address(address);
			return this;
		}

		public RegistrationBuilder id(String id) {
			this.builder.id(id);
			return this;
		}

		public RegistrationBuilder port(int port) {
			this.builder.port(port);
			return this;
		}

		public RegistrationBuilder sslPort(int port) {
			this.builder.sslPort(port);
			return this;
		}

		public RegistrationBuilder payload(ZookeeperInstance payload) {
			this.builder.payload(payload);
			return this;
		}

		public RegistrationBuilder serviceType(ServiceType serviceType) {
			this.builder.serviceType(serviceType);
			return this;
		}

		public RegistrationBuilder registrationTimeUTC(long registrationTimeUTC) {
			this.builder.registrationTimeUTC(registrationTimeUTC);
			return this;
		}

		public RegistrationBuilder uriSpec(UriSpec uriSpec) {
			this.builder.uriSpec(uriSpec);
			return this;
		}

		public RegistrationBuilder uriSpec(String uriSpec) {
			this.builder.uriSpec(new UriSpec(uriSpec));
			return this;
		}

		public RegistrationBuilder defaultUriSpec() {
			this.builder.uriSpec(new UriSpec(DEFAULT_URI_SPEC));
			return this;
		}
	}

	protected ServiceInstance<ZookeeperInstance> serviceInstance;
	protected ServiceInstanceBuilder<ZookeeperInstance> builder;

	public ServiceInstanceRegistration(ServiceInstanceBuilder<ZookeeperInstance> builder) {
		this.builder = builder;
	}

	public ServiceInstance<ZookeeperInstance> getServiceInstance() {
		if (this.serviceInstance == null) {
			build();
		}
		return this.serviceInstance;
	}

	protected void build() {
		this.serviceInstance = this.builder.build();
	}

	@Override
	public String getServiceId() {
		if (this.serviceInstance == null) {
			return null;
		}
		return this.serviceInstance.getName();
	}

	public int getPort() {
		if (this.serviceInstance == null) {
			return 0;
		}
		return this.serviceInstance.getPort();
	}

	public void setPort(int port) {
		this.builder.port(port);
		this.build();
	}

	@Override
	public String getHost() {
		if (this.serviceInstance == null) {
			return null;
		}
		return this.serviceInstance.getAddress();
	}

	@Override
	public boolean isSecure() {
		if (this.serviceInstance == null) {
			return false;
		}
		return this.serviceInstance.getSslPort() != null;
	}

	@Override
	public URI getUri() {
		if (this.serviceInstance == null) {
			return null;
		}
		return URI.create(this.serviceInstance.buildUriSpec());
	}

	@Override
	public Map<String, String> getMetadata() {
		if (this.serviceInstance == null || this.serviceInstance.getPayload() == null) {
			return Collections.emptyMap();
		}
		return this.serviceInstance.getPayload().getMetadata();
	}
}
