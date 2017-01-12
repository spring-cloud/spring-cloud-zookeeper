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
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;

/**
 * @author Spencer Gibb
 */
public class ZookeeperBuilderRegistration implements ZookeeperRegistration {

	protected ServiceInstance<ZookeeperInstance> serviceInstance;
	protected ServiceInstanceBuilder<ZookeeperInstance> builder;

	public ZookeeperBuilderRegistration(ServiceInstanceBuilder<ZookeeperInstance> builder) {
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
	public String toString() {
		if (this.serviceInstance == null) {
			return super.toString() + ", builder: "+ this.builder.toString();
		}
		return this.serviceInstance.toString();
	}
}
