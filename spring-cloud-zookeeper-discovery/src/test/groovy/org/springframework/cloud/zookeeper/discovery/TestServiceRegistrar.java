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

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;

public class TestServiceRegistrar {

	private final int serverPort;
	private final CuratorFramework curatorFramework;
	private final ServiceDiscovery serviceDiscovery;

	public TestServiceRegistrar(int serverPort, CuratorFramework curatorFramework) {
		this.serverPort = serverPort;
		this.curatorFramework = curatorFramework;
		this.serviceDiscovery = serviceDiscovery();
	}

	public void start() {
		try {
			this.serviceDiscovery.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ServiceInstance serviceInstance() {
		try {
			return ServiceInstance.builder().uriSpec(new UriSpec("{scheme}://{address}:{port}/"))
					.address("localhost")
					.port(this.serverPort)
					.name("testInstance")
					.build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ServiceDiscovery serviceDiscovery() {
		return ServiceDiscoveryBuilder
				.builder(Void.class)
				.basePath("/services")
				.client(this.curatorFramework)
				.thisInstance(serviceInstance())
				.build();
	}


	public void stop() {
		try {
			this.serviceDiscovery.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
