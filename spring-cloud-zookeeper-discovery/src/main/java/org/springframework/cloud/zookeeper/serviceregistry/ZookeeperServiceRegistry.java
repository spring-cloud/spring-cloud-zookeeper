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

	private ZookeeperServiceDiscovery serviceDiscovery;
	private AtomicBoolean started = new AtomicBoolean();

	protected CuratorFramework curator;

	protected ZookeeperDiscoveryProperties properties;

	protected InstanceSerializer<ZookeeperInstance> instanceSerializer;

	@Deprecated
	public ZookeeperServiceRegistry(ZookeeperServiceDiscovery serviceDiscovery, CuratorFramework curator,
									ZookeeperDiscoveryProperties properties, InstanceSerializer<ZookeeperInstance> instanceSerializer) {
		this.serviceDiscovery = serviceDiscovery;
		this.curator = curator;
		this.properties = properties;
		this.instanceSerializer = instanceSerializer;
		configureServiceDiscovery();
	}

	public ZookeeperServiceRegistry(CuratorFramework curator,
									ZookeeperDiscoveryProperties properties, InstanceSerializer<ZookeeperInstance> instanceSerializer) {
		this.curator = curator;
		this.properties = properties;
		this.instanceSerializer = instanceSerializer;
		this.serviceDiscovery = new ZookeeperServiceDiscovery(curator, properties, instanceSerializer);
		this.serviceDiscovery.setRegister(false);
		configureServiceDiscovery();
	}

	/**
	 * TODO: add when ZookeeperServiceDiscovery is removed
	 * One can override this method to provide custom way of registering {@link ServiceDiscovery}
	 */
	protected void configureServiceDiscovery() {
		// @formatter:off
		/*this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ZookeeperInstance.class)
				.client(this.curator)
				.basePath(this.properties.getRoot())
				.serializer(this.instanceSerializer)
				.build();
		// @formatter:on*/
		this.serviceDiscovery.configureServiceDiscovery(this.serviceDiscovery.getServiceDiscoveryRef(),
				this.curator, this.properties, this.instanceSerializer, this.serviceDiscovery.getServiceInstanceRef());
	}

	@Override
	public void register(ZookeeperRegistration registration) {
		try {
			this.serviceDiscovery.getServiceDiscoveryRef().get().registerService(registration.getServiceInstance());
		} catch (Exception e) {
			rethrowRuntimeException(e);
		}
	}

	@Override
	public void deregister(ZookeeperRegistration registration) {
		try {
			this.serviceDiscovery.getServiceDiscoveryRef().get().unregisterService(registration.getServiceInstance());
		} catch (Exception e) {
			rethrowRuntimeException(e);
		}
	}

	@Override
	public void afterSingletonsInstantiated() {
		try {
			this.serviceDiscovery.getServiceDiscoveryRef().get().start();
		} catch (Exception e) {
			rethrowRuntimeException(e);
		}
	}

	@Override
	public void close() {
		try {
			this.serviceDiscovery.getServiceDiscoveryRef().get().close();
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
		return this.serviceDiscovery.getServiceDiscoveryRef();
	}
}
