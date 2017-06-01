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

import com.netflix.loadbalancer.Server;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.cloud.netflix.ribbon.DefaultServerIntrospector;

import java.util.Map;

/**
 * @author Spencer Gibb
 */
public class ZookeeperServerIntrospector extends DefaultServerIntrospector {
	@Override
	public boolean isSecure(Server server) {
		if (server instanceof ZookeeperServer) {
			ZookeeperServer zookeeperServer = (ZookeeperServer) server;
			Integer sslPort = zookeeperServer.getInstance().getSslPort();
			return sslPort != null && sslPort > 0;
		}
		return super.isSecure(server);
	}

	@Override
	public Map<String, String> getMetadata(Server server) {
		if (server instanceof ZookeeperServer) {
			ZookeeperServer zookeeperServer = (ZookeeperServer) server;
			ServiceInstance<ZookeeperInstance> instance = zookeeperServer.getInstance();
			if (instance != null && instance.getPayload() != null) {
				return instance.getPayload().getMetadata();
			}
		}
		return super.getMetadata(server);
	}
}
