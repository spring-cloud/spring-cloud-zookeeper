/*
 * Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.zookeeper.support;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;

/**
 * @author Spencer Gibb
 */
public class DefaultServiceDiscoveryCustomizer implements ServiceDiscoveryCustomizer{
	protected CuratorFramework curator;

	protected ZookeeperDiscoveryProperties properties;

	protected InstanceSerializer<ZookeeperInstance> instanceSerializer;

	public DefaultServiceDiscoveryCustomizer(CuratorFramework curator, ZookeeperDiscoveryProperties properties, InstanceSerializer<ZookeeperInstance> instanceSerializer) {
		this.curator = curator;
		this.properties = properties;
		this.instanceSerializer = instanceSerializer;
	}

	@Override
	public ServiceDiscovery<ZookeeperInstance> customize(ServiceDiscoveryBuilder<ZookeeperInstance> builder) {
		// @formatter:off
		return builder
				.client(this.curator)
				.basePath(this.properties.getRoot())
				.serializer(this.instanceSerializer)
				.build();
		// @formatter:on
	}
}
