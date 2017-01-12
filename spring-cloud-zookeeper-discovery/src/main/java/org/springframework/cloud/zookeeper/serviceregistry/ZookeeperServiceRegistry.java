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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.zookeeper.compat.ServiceDiscoveryHolder;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceDiscovery;

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * @author Spencer Gibb
 */
public class ZookeeperServiceRegistry implements ServiceRegistry<ZookeeperRegistration>, SmartInitializingSingleton,
		Closeable, ServiceDiscoveryHolder {

	private ZookeeperServiceDiscovery zookeeperServiceDiscovery;
	private AtomicBoolean started = new AtomicBoolean();

	protected CuratorFramework curator;

	protected ZookeeperDiscoveryProperties properties;

	protected InstanceSerializer<ZookeeperInstance> instanceSerializer;
	private ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

	@Deprecated
	public ZookeeperServiceRegistry(ZookeeperServiceDiscovery zookeeperServiceDiscovery, CuratorFramework curator,
									ZookeeperDiscoveryProperties properties, InstanceSerializer<ZookeeperInstance> instanceSerializer) {
		this.zookeeperServiceDiscovery = zookeeperServiceDiscovery;
		this.curator = curator;
		this.properties = properties;
		this.instanceSerializer = instanceSerializer;
		configureServiceDiscovery();
	}

	public ZookeeperServiceRegistry(ServiceDiscovery<ZookeeperInstance> serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}

	/**
	 * TODO: add when ZookeeperServiceDiscovery is removed
	 * One can override this method to provide custom way of registering {@link ServiceDiscovery}
	 */
	private void configureServiceDiscovery() {
		this.zookeeperServiceDiscovery.configureServiceDiscovery(this.zookeeperServiceDiscovery.getServiceDiscoveryRef(),
				this.curator, this.properties, this.instanceSerializer, this.zookeeperServiceDiscovery.getServiceInstanceRef());
	}

	@Override
	public void register(ZookeeperRegistration registration) {
		try {
			getServiceDiscovery().registerService(registration.getServiceInstance());
		} catch (Exception e) {
			rethrowRuntimeException(e);
		}
	}

	private ServiceDiscovery<ZookeeperInstance> getServiceDiscovery() {
		if (this.serviceDiscovery != null) {
			return this.serviceDiscovery;
		}
		return this.zookeeperServiceDiscovery.getServiceDiscoveryRef().get();
	}

	@Override
	public void deregister(ZookeeperRegistration registration) {
		try {
			getServiceDiscovery().unregisterService(registration.getServiceInstance());
		} catch (Exception e) {
			rethrowRuntimeException(e);
		}
	}

	@Override
	public void afterSingletonsInstantiated() {
		try {
			getServiceDiscovery().start();
		} catch (Exception e) {
			rethrowRuntimeException(e);
		}
	}

	@Override
	public void close() {
		try {
			getServiceDiscovery().close();
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

	/**
	 * @deprecated for backwards compatibility. Visibility will be tightened when ZookeeperServiceDiscovery is removed.
	 */
	@Deprecated
	public CuratorFramework getCurator() {
		return this.curator;
	}

	/**
	 * @deprecated for backwards compatibility. Visibility will be tightened when ZookeeperServiceDiscovery is removed.
	 */
	@Deprecated
	public AtomicReference<ServiceDiscovery<ZookeeperInstance>> getServiceDiscoveryRef() {
		return this.zookeeperServiceDiscovery.getServiceDiscoveryRef();
	}
}
