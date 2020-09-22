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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.curator.RetryPolicy;
import org.apache.curator.drivers.TracerDriver;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;

import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.zookeeper.CuratorFactory;
import org.springframework.cloud.zookeeper.CuratorFrameworkCustomizer;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.core.env.MapPropertySource;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class ZookeeperConfigDataLocationResolver implements ConfigDataLocationResolver<ZookeeperConfigDataLocation> {

	/**
	 * Zookeeper Config Data prefix.
	 */
	public static final String PREFIX = "zookeeper:";

	private final Log log;

	public ZookeeperConfigDataLocationResolver(Log log) {
		this.log = log;
	}

	@Override
	public boolean isResolvable(ConfigDataLocationResolverContext context, String location) {
		if (!location.startsWith(PREFIX)) {
			return false;
		}
		// only bind on correct prefix
		boolean zkEnabled = context.getBinder().bind(ZookeeperProperties.PREFIX + ".enabled", Boolean.class)
				.orElse(true);
		boolean zkConfigEnabled = context.getBinder().bind(ZookeeperConfigProperties.PREFIX + ".enabled", Boolean.class)
				.orElse(true);
		return zkConfigEnabled && zkEnabled;
	}

	@Override
	public List<ZookeeperConfigDataLocation> resolve(ConfigDataLocationResolverContext context, String location,
			boolean optional) throws ConfigDataLocationNotFoundException {
		return Collections.emptyList();
	}

	@Override
	public List<ZookeeperConfigDataLocation> resolveProfileSpecific(ConfigDataLocationResolverContext context,
			String location, boolean optional, Profiles profiles) throws ConfigDataLocationNotFoundException {
		UriComponents locationUri = parseLocation(location);

		// create curator
		ZookeeperProperties zookeeperProperties = loadProperties(context.getBinder(), locationUri);
		context.getBootstrapContext().register(ZookeeperProperties.class, InstanceSupplier.of(zookeeperProperties));

		context.getBootstrapContext().registerIfAbsent(RetryPolicy.class,
				InstanceSupplier.from(() -> CuratorFactory.retryPolicy(zookeeperProperties)));

		context.getBootstrapContext().registerIfAbsent(CuratorFramework.class, InstanceSupplier
				.from(() -> curatorFramework(context.getBootstrapContext(), zookeeperProperties, optional)));

		// create locations
		ZookeeperConfigProperties properties = loadConfigProperties(context.getBinder());
		context.getBootstrapContext().register(ZookeeperConfigProperties.class, InstanceSupplier.of(properties));

		ZookeeperPropertySources sources = new ZookeeperPropertySources(properties, log);

		List<String> contexts = (locationUri == null || CollectionUtils.isEmpty(locationUri.getPathSegments()))
				? sources.getAutomaticContexts(profiles.getAccepted()) : getCustomContexts(locationUri);

		// promote beans to context
		context.getBootstrapContext().addCloseListener(event -> {
			CuratorFramework curatorFramework = event.getBootstrapContext().get(CuratorFramework.class);
			event.getApplicationContext().getBeanFactory().registerSingleton("configDataCuratorFramework",
					curatorFramework);
			HashMap<String, Object> source = new HashMap<>();
			source.put("spring.cloud.zookeeper.config.property-source-contexts", contexts);
			MapPropertySource propertySource = new MapPropertySource("zookeeperConfigData", source);
			event.getApplicationContext().getEnvironment().getPropertySources().addFirst(propertySource);
		});

		ArrayList<ZookeeperConfigDataLocation> locations = new ArrayList<>();
		contexts.forEach(propertySourceContext -> locations
				.add(new ZookeeperConfigDataLocation(propertySourceContext, optional)));

		return locations;
	}

	protected List<String> getCustomContexts(UriComponents uriComponents) {
		if (StringUtils.isEmpty(uriComponents.getPath())) {
			return Collections.emptyList();
		}

		return Arrays.asList(uriComponents.getPath().split(";"));
	}

	@Nullable
	protected UriComponents parseLocation(String location) {
		String uri = location.substring(PREFIX.length());
		if (!StringUtils.hasText(uri)) {
			return null;
		}
		if (!uri.startsWith("//")) {
			uri = PREFIX + "//" + uri;
		}
		else {
			uri = location;
		}
		return UriComponentsBuilder.fromUriString(uri).build();
	}

	protected CuratorFramework curatorFramework(ConfigurableBootstrapContext context, ZookeeperProperties properties,
			boolean optional) {

		try {
			Supplier<Stream<CuratorFrameworkCustomizer>> customizers;
			if (context.isRegistered(CuratorFrameworkCustomizer.class)) {
				customizers = () -> Stream.of(context.get(CuratorFrameworkCustomizer.class));
			}
			else {
				customizers = () -> null;
			}
			return CuratorFactory.curatorFramework(properties, context.get(RetryPolicy.class), customizers,
					supplier(context, EnsembleProvider.class), supplier(context, TracerDriver.class));
		}
		catch (Exception e) {
			if (!optional) {
				log.error("Unable to connect to zookeeper", e);
				throw new ConfigDataLocationNotFoundException("Unable to connect to zookeeper", null, e);
			}
			else if (log.isDebugEnabled()) {
				log.debug("Unable to connect to zookeeper", e);
			}
		}
		return null;
	}

	private <T> Supplier<T> supplier(ConfigurableBootstrapContext context, Class<T> type) {
		return () -> context.isRegistered(type) ? context.get(type) : null;
	}

	protected ZookeeperProperties loadProperties(Binder binder, UriComponents location) {
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

	protected ZookeeperConfigProperties loadConfigProperties(Binder binder) {
		ZookeeperConfigProperties properties = binder
				.bind(ZookeeperConfigProperties.PREFIX, Bindable.of(ZookeeperConfigProperties.class))
				.orElse(new ZookeeperConfigProperties());

		if (StringUtils.isEmpty(properties.getName())) {
			properties.setName(binder.bind("spring.application.name", String.class).orElse("application"));
		}

		return properties;
	}

}
