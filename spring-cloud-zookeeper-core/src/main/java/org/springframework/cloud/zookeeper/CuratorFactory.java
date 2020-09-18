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


public abstract class CuratorFactory {
	private static final Log log = LogFactory.getLog(ZookeeperAutoConfiguration.class);

	public static CuratorFramework curatorFramework(
			ZookeeperProperties properties,
			RetryPolicy retryPolicy,
			Supplier<Stream<CuratorFrameworkCustomizer>> optionalCuratorFrameworkCustomizerProvider,
			Supplier<EnsembleProvider> optionalEnsembleProvider,
			Supplier<TracerDriver> optionalTracerDriverProvider) throws Exception {
		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();

		EnsembleProvider ensembleProvider = optionalEnsembleProvider.get();
		if (ensembleProvider != null) {
			builder.ensembleProvider(ensembleProvider);
		}
		else {
			builder.connectString(properties.getConnectString());
		}
		builder.sessionTimeoutMs((int) properties.getSessionTimeout().toMillis())
				.connectionTimeoutMs((int) properties.getConnectionTimeout().toMillis())
				.retryPolicy(retryPolicy);

		Stream<CuratorFrameworkCustomizer> customizers = optionalCuratorFrameworkCustomizerProvider
				.get();
		if (customizers != null) {
			customizers
					.forEach(curatorFrameworkCustomizer -> curatorFrameworkCustomizer
							.customize(builder));
		}

		CuratorFramework curator = builder.build();
		TracerDriver tracerDriver = optionalTracerDriverProvider.get();
		if (tracerDriver != null && curator.getZookeeperClient() != null) {
			curator.getZookeeperClient().setTracerDriver(tracerDriver);
		}

		curator.start();
		if (log.isTraceEnabled()) {
			log.trace("blocking until connected to zookeeper for "
					+ properties.getBlockUntilConnectedWait()
					+ properties.getBlockUntilConnectedUnit());
		}
		curator.blockUntilConnected(properties.getBlockUntilConnectedWait(),
				properties.getBlockUntilConnectedUnit());
		if (log.isTraceEnabled()) {
			log.trace("connected to zookeeper");
		}
		return curator;
	}

	public static RetryPolicy retryPolicy(ZookeeperProperties properties) {
		return new ExponentialBackoffRetry(properties.getBaseSleepTimeMs(), properties.getMaxRetries(),
				properties.getMaxSleepMs());
	}
}
