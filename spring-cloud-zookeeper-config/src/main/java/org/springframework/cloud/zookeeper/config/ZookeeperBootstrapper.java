/*
 * Copyright 2015-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.zookeeper.config;

import java.util.function.Function;

import org.apache.curator.RetryPolicy;
import org.apache.curator.drivers.TracerDriver;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;

import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.cloud.zookeeper.CuratorFrameworkCustomizer;

public class ZookeeperBootstrapper implements BootstrapRegistryInitializer {

	private Function<BootstrapContext, RetryPolicy> retryPolicy;

	private Function<BootstrapContext, EnsembleProvider> ensembleProvider;

	private Function<BootstrapContext, TracerDriver> tracerDriver;

	private Function<BootstrapContext, CuratorFrameworkCustomizer> curatorFrameworkCustomizer;

	static BootstrapRegistryInitializer fromBootstrapContext(Function<BootstrapContext, CuratorFramework> factory) {
		return registry -> registry.register(CuratorFramework.class, factory::apply);
	}

	static ZookeeperBootstrapper create() {
		return new ZookeeperBootstrapper();
	}

	public ZookeeperBootstrapper retryPolicy(Function<BootstrapContext, RetryPolicy> retryPolicy) {
		this.retryPolicy = retryPolicy;
		return this;
	}

	public ZookeeperBootstrapper ensembleProvider(Function<BootstrapContext, EnsembleProvider> ensembleProvider) {
		this.ensembleProvider = ensembleProvider;
		return this;
	}

	public ZookeeperBootstrapper tracerDriver(Function<BootstrapContext, TracerDriver> tracerDriver) {
		this.tracerDriver = tracerDriver;
		return this;
	}

	public ZookeeperBootstrapper curatorFrameworkCustomizer(Function<BootstrapContext, CuratorFrameworkCustomizer> curatorFrameworkCustomizer) {
		this.curatorFrameworkCustomizer = curatorFrameworkCustomizer;
		return this;
	}

	@Override
	public void initialize(BootstrapRegistry registry) {
		register(registry, RetryPolicy.class, retryPolicy);
		register(registry, EnsembleProvider.class, ensembleProvider);
		register(registry, TracerDriver.class, tracerDriver);
		register(registry, CuratorFrameworkCustomizer.class, curatorFrameworkCustomizer);
	}

	private <T> void register(BootstrapRegistry registry, Class<T> type, Function<BootstrapContext, T> factory) {
		if (this.retryPolicy != null) {
			registry.register(type, factory::apply);
		}
	}
}
