/*
 * Copyright 2015-present the original author or authors.
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

package org.springframework.cloud.zookeeper.discovery.configclient;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.curator.RetryPolicy;
import org.apache.curator.drivers.TracerDriver;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.ensemble.fixed.FixedEnsembleProvider;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.DefaultTracerDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.BootstrapRegistryInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.cloud.config.client.ConfigServerInstanceProvider;
import org.springframework.cloud.zookeeper.CuratorFrameworkCustomizer;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClient;
import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Ryan Baxter
 */
public class ZookeeperConfigServerBootstrapperCustomizerTests {
	private ConfigurableApplicationContext context;

	@AfterEach
	public void after() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	public void enabledAddsInstanceProviderFn() {
		AtomicReference<ZookeeperDiscoveryClient> bootstrapDiscoveryClient = new AtomicReference<>();
		BindHandlerBootstrapper bindHandlerBootstrapper = new BindHandlerBootstrapper();
		context = new SpringApplicationBuilder(ZookeeperConfigServerBootstrapperTests.TestConfig.class)
				.listeners(new ZookeeperTestingServer())
				.properties("--server.port=0", "spring.cloud.config.discovery.enabled=true",
						"spring.cloud.zookeeper.discovery.metadata[mymetadataprop]=mymetadataval",
						"spring.cloud.service-registry.auto-registration.enabled=false")
				.addBootstrapRegistryInitializer(bindHandlerBootstrapper)
				.addBootstrapRegistryInitializer(registry -> registry.addCloseListener(event -> {
					ConfigServerInstanceProvider.Function providerFn = event.getBootstrapContext()
							.get(ConfigServerInstanceProvider.Function.class);
					assertThat(providerFn.apply("id", event.getBootstrapContext()
							.get(Binder.class), event.getBootstrapContext()
							.get(BindHandler.class), mock(Log.class))).as("Should return empty list.")
							.isNotNull();
					bootstrapDiscoveryClient.set(event.getBootstrapContext().get(ZookeeperDiscoveryClient.class));
					CuratorFrameworkCustomizer curatorFrameworkCustomizer = event.getBootstrapContext()
							.get(CuratorFrameworkCustomizer.class);
					assertThat(curatorFrameworkCustomizer).isInstanceOf(MyCuratorFrameworkCustomizer.class);
					RetryPolicy retryPolicy = event.getBootstrapContext().get(RetryPolicy.class);
					assertThat(retryPolicy).isInstanceOf(RetryPolicy.class);
					EnsembleProvider ensembleProvider = event.getBootstrapContext().get(EnsembleProvider.class);
					assertThat(ensembleProvider).isInstanceOf(MyEnsembleProvider.class);
					TracerDriver tracerDriver = event.getBootstrapContext().get(TracerDriver.class);
					assertThat(tracerDriver).isInstanceOf(MyTracerDriver.class);
				})).run();

		ZookeeperDiscoveryClient discoveryClient = context.getBean(ZookeeperDiscoveryClient.class);

		assertThat(discoveryClient == bootstrapDiscoveryClient.get()).isTrue();
		assertThat(bindHandlerBootstrapper.onSuccessCount).isGreaterThan(0);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestConfig {

	}

	static class BindHandlerBootstrapper implements BootstrapRegistryInitializer, Ordered {

		private int onSuccessCount = 0;

		private static <T> void registerIfAbsentAndEnabled(
				BootstrapRegistry registry, Class<T> type, BootstrapRegistry.InstanceSupplier<T> supplier) {
			registry.registerIfAbsent(type, supplier);
		}

		@Override
		public void initialize(BootstrapRegistry registry) {
			registry.register(BindHandler.class, context -> new BindHandler() {
				@Override
				public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context,
						Object result) {
					onSuccessCount++;
					return result;
				}
			});

			registerIfAbsentAndEnabled(registry, RetryPolicy.class, context ->
					new MyRetryPolicy(context.get(ZookeeperProperties.class)));

			registerIfAbsentAndEnabled(registry, CuratorFrameworkCustomizer.class, context ->
					new MyCuratorFrameworkCustomizer());

			registerIfAbsentAndEnabled(registry, EnsembleProvider.class, context ->
					new MyEnsembleProvider(context.get(ZookeeperProperties.class)));

			registerIfAbsentAndEnabled(registry, TracerDriver.class, context ->
					new MyTracerDriver());
		}

		@Override
		public int getOrder() {
			return Ordered.HIGHEST_PRECEDENCE;
		}
	}

	static class MyRetryPolicy extends ExponentialBackoffRetry {

		MyRetryPolicy(ZookeeperProperties properties) {
			super(properties.getBaseSleepTimeMs(), properties.getMaxRetries(), properties.getMaxSleepMs());
		}
	}

	static class MyCuratorFrameworkCustomizer implements CuratorFrameworkCustomizer {
		@Override
		public void customize(CuratorFrameworkFactory.Builder builder) {

		}
	}

	static class MyEnsembleProvider extends FixedEnsembleProvider {
		MyEnsembleProvider(ZookeeperProperties properties) {
			super(properties.getConnectString());
		}

	}

	static class MyTracerDriver extends DefaultTracerDriver {

	}

}
