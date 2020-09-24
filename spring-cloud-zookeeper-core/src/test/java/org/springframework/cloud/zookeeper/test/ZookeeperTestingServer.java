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

package org.springframework.cloud.zookeeper.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.test.TestingServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

public class ZookeeperTestingServer implements SmartApplicationListener, Ordered {

	private final Log log = LogFactory.getLog(getClass());

	private AtomicBoolean started = new AtomicBoolean();

	private TestingServer testingServer;

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	public int getPort() {
		Assert.notNull(testingServer, "testingServer not started");
		return testingServer.getPort();
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return ApplicationEnvironmentPreparedEvent.class.isAssignableFrom(eventType)
				|| ApplicationPreparedEvent.class.isAssignableFrom(eventType);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationEnvironmentPreparedEvent) {
			envPrepared(((ApplicationEnvironmentPreparedEvent) event).getEnvironment());
		}
		else if (event instanceof ApplicationPreparedEvent) {
			appPrepared(((ApplicationPreparedEvent) event).getApplicationContext());
		}
	}

	public void envPrepared(ConfigurableEnvironment environment) {
		MutablePropertySources sources = environment.getPropertySources();

		start();

		if (!sources.contains("zookeeperTestingServer")) {
			HashMap<String, Object> map = new HashMap<>();
			map.put(ZookeeperProperties.PREFIX + ".connect-string", "localhost:" + testingServer.getPort());

			sources.addFirst(new MapPropertySource("zookeeperTestingServer", map));
		}

	}

	public void start() {
		if (started.compareAndSet(false, true)) {
			try {
				testingServer = new TestingServer();
			}
			catch (Exception e) {
				ReflectionUtils.rethrowRuntimeException(e);
			}
			log.debug("Starting TestingServer on port " + testingServer.getPort());
		}
	}

	public void appPrepared(ConfigurableApplicationContext context) {
		context.addApplicationListener(new CloseListener(this));
	}

	public void close() {
		try {
			if (started.compareAndSet(true, false)) {
				log.debug("Closing TestingServer on port " + testingServer.getPort());
				testingServer.close();
			}
		}
		catch (IOException ex) {
			ReflectionUtils.rethrowRuntimeException(ex);
		}
	}

	static class CloseListener implements ApplicationListener<ContextClosedEvent>, Ordered {

		private final ZookeeperTestingServer testingServer;

		CloseListener(ZookeeperTestingServer testingServer) {
			this.testingServer = testingServer;
		}

		@Override
		public void onApplicationEvent(ContextClosedEvent event) {
			testingServer.close();
		}

		@Override
		public int getOrder() {
			return Ordered.LOWEST_PRECEDENCE;
		}
	}

	public static class Loader extends SpringBootContextLoader {

		@Override
		protected SpringApplication getSpringApplication() {
			SpringApplication application = super.getSpringApplication();
			ZookeeperTestingServer testingServer = new ZookeeperTestingServer();
			application.addListeners(testingServer);
			application.addListeners(new CloseListener(testingServer));
			return application;
		}

	}

	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext context) {
			ZookeeperTestingServer testingServer = new ZookeeperTestingServer();
			testingServer.envPrepared(context.getEnvironment());
			testingServer.appPrepared(context);
		}

	}

}
