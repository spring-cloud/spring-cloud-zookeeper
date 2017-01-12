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

import java.io.Closeable;
import java.io.IOException;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * @author Spencer Gibb
 */
public class ZookeeperServiceRegistry implements ServiceRegistry<ZookeeperRegistration>, SmartInitializingSingleton,
		Closeable {

	protected ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

	public ZookeeperServiceRegistry(ServiceDiscovery<ZookeeperInstance> serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}

	@Override
	public void register(ZookeeperRegistration registration) {
		try {
			this.serviceDiscovery.registerService(registration.getServiceInstance());
		} catch (Exception e) {
			rethrowRuntimeException(e);
		}
	}

	@Override
	public void deregister(ZookeeperRegistration registration) {
		try {
			this.serviceDiscovery.unregisterService(registration.getServiceInstance());
		} catch (Exception e) {
			rethrowRuntimeException(e);
		}
	}

	@Override
	public void afterSingletonsInstantiated() {
		try {
			this.serviceDiscovery.start();
		} catch (Exception e) {
			rethrowRuntimeException(e);
		}
	}

	@Override
	public void close() {
		try {
			this.serviceDiscovery.close();
		} catch (IOException e) {
			rethrowRuntimeException(e);
		}
	}

	@Override
	public void setStatus(ZookeeperRegistration registration, String status) {
		//TODO:
	}

	@Override
	public Object getStatus(ZookeeperRegistration registration) {
		//TODO:
		return null;
	}

}
