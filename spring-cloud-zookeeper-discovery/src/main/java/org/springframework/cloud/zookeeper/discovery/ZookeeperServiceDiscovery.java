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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

/**
 * @author Spencer Gibb
 */
public class ZookeeperServiceDiscovery implements ApplicationContextAware {

	private CuratorFramework curator;

	private ZookeeperDiscoveryProperties properties;

	private InstanceSerializer<ZookeeperInstance> instanceSerializer;

	private InetUtils inetUtils;

	private ApplicationContext context;

	private AtomicBoolean built = new AtomicBoolean(false);

	private AtomicInteger port = new AtomicInteger();

	private AtomicReference<ServiceInstance<ZookeeperInstance>> serviceInstance = new AtomicReference<>();

	private AtomicReference<ServiceDiscovery<ZookeeperInstance>> serviceDiscovery = new AtomicReference<>();

	@Value("${spring.application.name:application}")
	private String appName;

	public ZookeeperServiceDiscovery(CuratorFramework curator,
			ZookeeperDiscoveryProperties properties,
			InstanceSerializer<ZookeeperInstance> instanceSerializer, InetUtils inetUtils) {
		this.curator = curator;
		this.properties = properties;
		this.instanceSerializer = instanceSerializer;
		this.inetUtils = inetUtils;
	}

	public int getPort() {
		return this.port.get();
	}

	public void setPort(int port) {
		this.port.set(port);
	}

	public ServiceInstance<ZookeeperInstance> getServiceInstance() {
		return this.serviceInstance.get();
	}

	public ServiceDiscovery<ZookeeperInstance> getServiceDiscovery() {
		return this.serviceDiscovery.get();
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	public void build() {
		if (this.built.compareAndSet(false, true)) {
			if (this.port.get() <= 0) {
				throw new IllegalStateException("Cannot create instance whose port is not greater than 0");
			}
			String host = this.properties.getInstanceHost() == null ? getIpAddress() :
					this.properties.getInstanceHost();
			UriSpec uriSpec = new UriSpec(this.properties.getUriSpec());
			configureServiceInstance(this.serviceInstance, this.appName,
					this.context, this.port, host, uriSpec);
			configureServiceDiscovery(this.serviceDiscovery, this.curator, this.properties,
					this.instanceSerializer, this.serviceInstance);
		}
	}

	protected void configureServiceInstance(AtomicReference<ServiceInstance<ZookeeperInstance>> serviceInstance,
											String appName,
											ApplicationContext context,
											AtomicInteger port,
											String host,
											UriSpec uriSpec) {
		// @formatter:off
		try {
			serviceInstance.set(ServiceInstance.<ZookeeperInstance>builder()
					.name(appName)
					.payload(new ZookeeperInstance(context.getId(), appName, this.properties.getMetadata()))
					.port(port.get())
					.address(host)
					.uriSpec(uriSpec).build());
		}
		catch (Exception e) {
			ReflectionUtils.rethrowRuntimeException(e);
		}
		// @formatter:on
	}

	protected void configureServiceDiscovery(AtomicReference<ServiceDiscovery<ZookeeperInstance>> serviceDiscovery,
			CuratorFramework curator, ZookeeperDiscoveryProperties properties,
			InstanceSerializer<ZookeeperInstance> instanceSerializer,
			AtomicReference<ServiceInstance<ZookeeperInstance>> serviceInstance) {
		// @formatter:off
		serviceDiscovery.set(ServiceDiscoveryBuilder.builder(ZookeeperInstance.class)
				.client(curator)
				.basePath(properties.getRoot())
				.serializer(instanceSerializer)
				.thisInstance(serviceInstance.get())
				.build());
		// @formatter:on
	}

	public String getIpAddress() {
		return this.inetUtils.findFirstNonLoopbackAddress().getHostAddress();
	}

	protected AtomicReference<ServiceDiscovery<ZookeeperInstance>> getServiceDiscoveryRef() {
		return this.serviceDiscovery;
	}

	protected AtomicReference<ServiceInstance<ZookeeperInstance>> getServiceInstanceRef() {
		return this.serviceInstance;
	}

	protected AtomicBoolean getBuilt() {
		return this.built;
	}

	protected CuratorFramework getCurator() {
		return this.curator;
	}
}
