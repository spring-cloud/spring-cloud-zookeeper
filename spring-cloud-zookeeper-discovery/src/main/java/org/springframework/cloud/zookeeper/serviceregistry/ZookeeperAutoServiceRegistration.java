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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;

/**
 * Zookeeper {@link AbstractAutoServiceRegistration}
 * that uses {@link ZookeeperServiceRegistry} to register and de-register instances.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
public class ZookeeperAutoServiceRegistration extends AbstractAutoServiceRegistration<ZookeeperRegistration> {

	private static final Log log = LogFactory.getLog(ZookeeperAutoServiceRegistration.class);

	private ZookeeperRegistration registration;
	private ZookeeperDiscoveryProperties properties;

	public ZookeeperAutoServiceRegistration(ZookeeperServiceRegistry registry,
											ZookeeperRegistration registration,
											ZookeeperDiscoveryProperties properties) {
		this(registry, registration, properties, null);
	}

	public ZookeeperAutoServiceRegistration(ZookeeperServiceRegistry registry,
											ZookeeperRegistration registration,
											ZookeeperDiscoveryProperties properties,
											AutoServiceRegistrationProperties arProperties) {
		super(registry, arProperties);
		this.registration = registration;
		this.properties = properties;
		if (this.properties.getInstancePort() != null) {
			this.registration.setPort(this.properties.getInstancePort());
		}
	}

	@Override
	protected ZookeeperRegistration getRegistration() {
		return this.registration;
	}

	@Override
	protected ZookeeperRegistration getManagementRegistration() {
		return null;
	}

	@Override
	protected void register() {
		if (!this.properties.isRegister()) {
			log.debug("Registration disabled.");
			return;
		}
		if (this.registration.getPort() == 0) {
			this.registration.setPort(getPort().get());
		}
		super.register();
	}

	@Override
	protected void deregister() {
		if (!this.properties.isRegister()) {
			return;
		}
		super.deregister();
	}

	@Override
	protected boolean isEnabled() {
		return this.properties.isEnabled();
	}

	@Override
	protected Object getConfiguration() {
		return this.properties;
	}
}
