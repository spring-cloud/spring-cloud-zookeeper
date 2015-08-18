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

import org.apache.curator.x.discovery.ServiceInstance;

import com.netflix.loadbalancer.Server;

/**
 * @author Spencer Gibb
 */
public class ZookeeperServer extends Server {

	private final MetaInfo metaInfo;

	public ZookeeperServer(final ServiceInstance<ZookeeperInstance> instance) {
		// TODO: ssl support
		super(instance.getAddress(), instance.getPort());
		metaInfo = new MetaInfo() {
			@Override
			public String getAppName() {
				return instance.getName();
			}

			@Override
			public String getServerGroup() {
				return null;
			}

			@Override
			public String getServiceIdForDiscovery() {
				return null;
			}

			@Override
			public String getInstanceId() {
				return instance.getId();
			}
		};
	}

	@Override
	public MetaInfo getMetaInfo() {
		return metaInfo;
	}
}
