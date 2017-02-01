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

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.springframework.beans.BeansException;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Service discovery for Zookeeper that sets up {@link ServiceDiscovery}
 * and {@link ServiceInstance}.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 * @deprecated replaced by {@link org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry}
 * and {@link org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration}. Remove in Edgware
 */
@Deprecated
public class ZookeeperServiceDiscovery implements ZookeeperRegistration, ApplicationContextAware {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private CuratorFramework curator;

	private ZookeeperDiscoveryProperties properties;
	private InstanceSerializer<ZookeeperInstance> instanceSerializer;

	private AtomicBoolean built = new AtomicBoolean(false);

	private AtomicInteger port = new AtomicInteger();

	private AtomicReference<ServiceInstance<ZookeeperInstance>> serviceInstance = new AtomicReference<>();

	private AtomicReference<ServiceDiscovery<ZookeeperInstance>> serviceDiscovery = new AtomicReference<>();

	private String appName;
	private ApplicationContext context;

	private boolean register;

	public ZookeeperServiceDiscovery(CuratorFramework curator,
			ZookeeperDiscoveryProperties properties,
			InstanceSerializer<ZookeeperInstance> instanceSerializer) {
		this.curator = curator;
		this.properties = properties;
		this.instanceSerializer = instanceSerializer;

		this.register = this.properties.isRegister();
	}

	public int getPort() {
		return this.port.get();
	}

	public void setPort(int port) {
		this.port.set(port);
	}

	/**
	 * Override the register property, useful when auto-register == false
	 * @param register
	 */
	public void setRegister(boolean register) {
		this.register = register;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
		RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(this.context.getEnvironment());
		this.appName = resolver.getProperty("spring.application.name", "application");
	}

	@Override
	public String getServiceId() {
		return this.appName;
	}

	/**
	 * Builds Service Instance - needs to be used when you want to register your application
	 * in Zookeeper
	 */
	public void build() {
		if (this.built.compareAndSet(false, true)) {
			if (this.port.get() <= 0 && this.register) {
				throw new IllegalStateException("Cannot create instance whose port is not greater than 0");
			}
			String host = this.properties.getInstanceHost();
			if (!StringUtils.hasText(host)) {
				throw new IllegalStateException("instanceHost must not be empty");
			}
			UriSpec uriSpec = new UriSpec(this.properties.getUriSpec());
			if (this.register) {
				configureServiceInstance(this.serviceInstance, this.appName,
						this.context, this.port, host, uriSpec);
			}
			if (this.serviceDiscovery.get() == null) {
				configureServiceDiscovery(this.serviceDiscovery, this.curator, this.properties,
						this.instanceSerializer, this.serviceInstance);
			}
		}
	}

	/**
	 * Builds Service Discovery - needs to be used if you want to use Zookeeper as a client application.
	 * You don't have to register in Zookeeper to use Service Discovery.
	 */
	public void buildServiceDiscovery() {
		if (log.isDebugEnabled()) {
			log.debug("Configuring service discovery for service instance [" + this.serviceInstance + "]");
		}
		configureServiceDiscovery(this.serviceDiscovery, this.curator, this.properties,
				this.instanceSerializer, this.serviceInstance);
	}

	/**
	 * One can override this method to provide custom way of registering a service
	 * instance (e.g. when no payload is required).
	 */
	public void configureServiceInstance(AtomicReference<ServiceInstance<ZookeeperInstance>> serviceInstance,
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
	/**
	 * One can override this method to provide custom way of registering {@link ServiceDiscovery}
	 */
	public void configureServiceDiscovery(AtomicReference<ServiceDiscovery<ZookeeperInstance>> serviceDiscovery,
			CuratorFramework curator, ZookeeperDiscoveryProperties properties,
			InstanceSerializer<ZookeeperInstance> instanceSerializer,
			AtomicReference<ServiceInstance<ZookeeperInstance>> serviceInstance) {
		// @formatter:off
		ServiceDiscoveryBuilder<ZookeeperInstance> builder = ServiceDiscoveryBuilder.builder(ZookeeperInstance.class)
				.client(curator)
				.basePath(properties.getRoot())
				.serializer(instanceSerializer);

		if (serviceInstance != null) {
			builder.thisInstance(serviceInstance.get());
		}

		serviceDiscovery.set(builder.build());
		// @formatter:on
	}

	@Override
	public ServiceInstance<ZookeeperInstance> getServiceInstance() {
		build();
		return this.serviceInstance.get();
	}

	public AtomicReference<ServiceDiscovery<ZookeeperInstance>> getServiceDiscoveryRef() {
		return this.serviceDiscovery;
	}

	public AtomicReference<ServiceInstance<ZookeeperInstance>> getServiceInstanceRef() {
		return this.serviceInstance;
	}

	protected AtomicBoolean getBuilt() {
		return this.built;
	}

	public CuratorFramework getCurator() {
		return this.curator;
	}
}
