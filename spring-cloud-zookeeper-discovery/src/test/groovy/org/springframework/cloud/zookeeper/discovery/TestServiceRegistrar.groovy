/*
 * Copyright 2012-2015 the original author or authors.
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
package org.springframework.cloud.zookeeper.discovery

import groovy.transform.CompileStatic
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.x.discovery.ServiceDiscovery
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder
import org.apache.curator.x.discovery.ServiceInstance
import org.apache.curator.x.discovery.UriSpec

@CompileStatic
class TestServiceRegistrar {

	private final int wiremockServerPort
	private final CuratorFramework curatorFramework
	private final ServiceDiscovery serviceDiscovery

	TestServiceRegistrar(int wiremockServerPort, CuratorFramework curatorFramework) {
		this.wiremockServerPort = wiremockServerPort
		this.curatorFramework = curatorFramework
		this.serviceDiscovery = serviceDiscovery()
	}

	void start() {
		serviceDiscovery.start()
	}

	ServiceInstance serviceInstance() {
		return ServiceInstance.builder().uriSpec(new UriSpec("{scheme}://{address}:{port}/"))
				.address('localhost')
				.port(wiremockServerPort)
				.name('testInstance')
				.build()
	}

	ServiceDiscovery serviceDiscovery() {
		return ServiceDiscoveryBuilder
				.builder(Void)
				.basePath('/services')
				.client(curatorFramework)
				.thisInstance(serviceInstance())
				.build()
	}


	void stop() {
		serviceDiscovery.close()
	}
}
