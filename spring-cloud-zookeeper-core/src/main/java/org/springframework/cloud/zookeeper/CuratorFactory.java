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

package org.springframework.cloud.zookeeper;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.drivers.TracerDriver;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.context.config.ConfigDataException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;

public abstract class CuratorFactory {

	private static final Log log = LogFactory.getLog(ZookeeperAutoConfiguration.class);

	public static CuratorFramework curatorFramework(ZookeeperProperties properties, RetryPolicy retryPolicy,
			Supplier<Stream<CuratorFrameworkCustomizer>> optionalCuratorFrameworkCustomizerProvider,
			Supplier<EnsembleProvider> optionalEnsembleProvider, Supplier<TracerDriver> optionalTracerDriverProvider)
			throws Exception {
		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();

		EnsembleProvider ensembleProvider = optionalEnsembleProvider.get();
		if (ensembleProvider != null) {
			builder.ensembleProvider(ensembleProvider);
		}
		else {
			builder.connectString(properties.getConnectString());
		}
		builder.sessionTimeoutMs((int) properties.getSessionTimeout().toMillis())
				.connectionTimeoutMs((int) properties.getConnectionTimeout().toMillis()).retryPolicy(retryPolicy);

		Stream<CuratorFrameworkCustomizer> customizers = optionalCuratorFrameworkCustomizerProvider.get();
		if (customizers != null) {
			customizers.forEach(curatorFrameworkCustomizer -> curatorFrameworkCustomizer.customize(builder));
		}

		CuratorFramework curator = builder.build();
		TracerDriver tracerDriver = optionalTracerDriverProvider.get();
		if (tracerDriver != null && curator.getZookeeperClient() != null) {
			curator.getZookeeperClient().setTracerDriver(tracerDriver);
		}

		curator.start();
		if (log.isTraceEnabled()) {
			log.trace("blocking until connected to zookeeper for " + properties.getBlockUntilConnectedWait()
					+ properties.getBlockUntilConnectedUnit());
		}
		curator.blockUntilConnected(properties.getBlockUntilConnectedWait(), properties.getBlockUntilConnectedUnit());
		if (log.isTraceEnabled()) {
			log.trace("connected to zookeeper");
		}
		return curator;
	}

	public static RetryPolicy retryPolicy(ZookeeperProperties properties) {
		return new ExponentialBackoffRetry(properties.getBaseSleepTimeMs(), properties.getMaxRetries(),
				properties.getMaxSleepMs());
	}

	public static void registerCurator(BootstrapRegistry registery, UriComponents location, boolean optional) {
		registerCurator(registery, location, optional, bootstrapContext -> true);
	}

	public static void registerCurator(BootstrapRegistry registery, UriComponents location, boolean optional,
			Predicate<BootstrapContext> predicate) {
		registery.registerIfAbsent(ZookeeperProperties.class, context -> {
			if (!predicate.test(context)) {
				return null;
			}
			return loadProperties(context.get(Binder.class), location);
		});

		registery.registerIfAbsent(RetryPolicy.class, context -> {
			if (!predicate.test(context)) {
				return null;
			}
			return retryPolicy(context.get(ZookeeperProperties.class));
		});

		registery.registerIfAbsent(CuratorFramework.class, context -> {
			if (!predicate.test(context)) {
				return null;
			}
			return curatorFramework(context, context.get(ZookeeperProperties.class), optional);
		});

		// promote beans to context
		registery.addCloseListener(event -> {
			BootstrapContext context = event.getBootstrapContext();
			if (predicate.test(context)) {
				CuratorFramework curatorFramework = context.get(CuratorFramework.class);
				event.getApplicationContext().getBeanFactory().registerSingleton("configDataCuratorFramework",
						curatorFramework);
			}
		});

	}

	static ZookeeperProperties loadProperties(Binder binder, UriComponents location) {
		ZookeeperProperties properties = binder.bind(ZookeeperProperties.PREFIX, Bindable.of(ZookeeperProperties.class))
				.orElse(new ZookeeperProperties());

		if (location != null && StringUtils.hasText(location.getHost())) {
			if (location.getPort() < 0) {
				throw new IllegalArgumentException(
						"zookeeper port must be greater than or equal to zero: " + location.getPort());
			}
			properties.setConnectString(location.getHost() + ":" + location.getPort());
		}

		return properties;
	}

	private static CuratorFramework curatorFramework(BootstrapContext context, ZookeeperProperties properties,
			boolean optional) {

		Supplier<Stream<CuratorFrameworkCustomizer>> customizers;
		// TODO: use new apis after milestone release
		try {
			CuratorFrameworkCustomizer customizer = context.get(CuratorFrameworkCustomizer.class);
			customizers = () -> Stream.of(customizer);
		}
		catch (IllegalStateException e) {
			customizers = () -> null;
		}
		try {
			return CuratorFactory.curatorFramework(properties, context.get(RetryPolicy.class), customizers,
					supplier(context, EnsembleProvider.class), supplier(context, TracerDriver.class));
		}
		catch (Exception e) {
			if (!optional) {
				log.error("Unable to connect to zookeeper", e);
				throw new ZookeeperConnectException("Unable to connect to zookeeper", e);
			}
			if (log.isDebugEnabled()) {
				log.debug("Unable to connect to zookeeper", e);
			}
		}
		return null;
	}

	private static <T> Supplier<T> supplier(BootstrapContext context, Class<T> type) {
		try {
			// TODO: use new apis after milestone release
			T instance = context.get(type);
			return () -> instance;
		}
		catch (IllegalStateException e) {
			return () -> null;
		}
	}

	private static class ZookeeperConnectException extends ConfigDataException {

		/**
		 * Create a new {@link ConfigDataException} instance.
		 * @param message the exception message
		 * @param cause the exception cause
		 */
		protected ZookeeperConnectException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
