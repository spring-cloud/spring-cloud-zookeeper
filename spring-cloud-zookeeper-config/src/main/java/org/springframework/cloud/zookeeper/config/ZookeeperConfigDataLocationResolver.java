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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.core.env.MapPropertySource;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class ZookeeperConfigDataLocationResolver implements ConfigDataLocationResolver<ZookeeperConfigDataLocation> {

	private static final Log log = LogFactory.getLog(ZookeeperConfigDataLocationResolver.class);

	/**
	 * Zookeeper Config Data prefix.
	 */
	public static final String PREFIX = "zookeeper:";

	@Override
	public boolean isResolvable(ConfigDataLocationResolverContext context, String location) {
		boolean zkEnabled = context.getBinder().bind(ZookeeperProperties.PREFIX + ".enabled", Boolean.class)
				.orElse(true);
		boolean zkConfigEnabled = context.getBinder().bind(ZookeeperConfigProperties.PREFIX + ".enabled", Boolean.class)
				.orElse(true);
		return location.startsWith(PREFIX) && zkConfigEnabled && zkEnabled;
	}

	@Override
	public List<ZookeeperConfigDataLocation> resolve(ConfigDataLocationResolverContext context, String location,
			boolean optional) throws ConfigDataLocationNotFoundException {
		return Collections.emptyList();
	}

	@Override
	public List<ZookeeperConfigDataLocation> resolveProfileSpecific(ConfigDataLocationResolverContext context,
			String location, boolean optional, Profiles profiles) throws ConfigDataLocationNotFoundException {
		UriComponents locationUri = parseLocation(context, location);

		ZookeeperConfigProperties properties = loadConfigProperties(context.getBinder());

		List<String> contexts = (locationUri == null || CollectionUtils.isEmpty(locationUri.getPathSegments()))
				? getAutomaticContexts(profiles, properties) : getCustomContexts(locationUri, properties);

		context.getBootstrapRegistry()
				.register(CuratorFramework.class,
						() -> curatorFramework(optional, loadProperties(context.getBinder(), locationUri)))
				.onApplicationContextPrepared((ctxt, curatorFramework) -> {
					ctxt.getBeanFactory().registerSingleton("configDataCuratorFramework", curatorFramework);
					HashMap<String, Object> source = new HashMap<>();
					source.put("spring.cloud.zookeeper.config.property-source-contexts", contexts);
					MapPropertySource propertySource = new MapPropertySource("zookeeperConfigData", source);
					ctxt.getEnvironment().getPropertySources().addFirst(propertySource);
				});

		ArrayList<ZookeeperConfigDataLocation> locations = new ArrayList<>();
		contexts.forEach(propertySourceContext -> locations
				.add(new ZookeeperConfigDataLocation(properties, propertySourceContext, optional)));

		return locations;
	}

	protected List<String> getCustomContexts(UriComponents uriComponents, ZookeeperConfigProperties properties) {
		if (StringUtils.isEmpty(uriComponents.getPath())) {
			return Collections.emptyList();
		}

		return Arrays.asList(uriComponents.getPath().split(";"));
	}

	protected List<String> getAutomaticContexts(Profiles profiles, ZookeeperConfigProperties properties) {
		String root = properties.getRoot();
		List<String> contexts = new ArrayList<>();

		String defaultContext = root + "/" + properties.getDefaultContext();
		contexts.add(defaultContext);
		addProfiles(contexts, defaultContext, profiles, properties);

		StringBuilder baseContext = new StringBuilder(root);
		if (!properties.getName().startsWith("/")) {
			baseContext.append("/");
		}
		// getName() defaults to ${spring.application.name} or application
		baseContext.append(properties.getName());
		contexts.add(baseContext.toString());
		addProfiles(contexts, baseContext.toString(), profiles, properties);

		Collections.reverse(contexts);
		return contexts;
	}

	@Nullable
	protected UriComponents parseLocation(ConfigDataLocationResolverContext context, String location) {
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

	private void addProfiles(List<String> contexts, String baseContext, Profiles profiles,
			ZookeeperConfigProperties properties) {
		for (String profile : profiles.getAccepted()) {
			contexts.add(baseContext + properties.getProfileSeparator() + profile);
		}
	}

	protected CuratorFramework curatorFramework(boolean optional, ZookeeperProperties properties) {
		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();

		builder.connectString(properties.getConnectString())
				.sessionTimeoutMs((int) properties.getSessionTimeout().toMillis())
				.connectionTimeoutMs((int) properties.getConnectionTimeout().toMillis())
				.retryPolicy(retryPolicy(properties));

		CuratorFramework curator = builder.build();

		curator.start();
		if (log.isTraceEnabled()) {
			log.trace("blocking until connected to zookeeper for " + properties.getBlockUntilConnectedWait()
					+ properties.getBlockUntilConnectedUnit());
		}
		try {
			curator.blockUntilConnected(properties.getBlockUntilConnectedWait(),
					properties.getBlockUntilConnectedUnit());
		}
		catch (InterruptedException e) {
			if (!optional) {
				log.error("Unable to connect to zookeeper", e);
				throw new ConfigDataLocationNotFoundException("Unable to connect to zookeeper", null, e);
			}
			else if (log.isDebugEnabled()) {
				log.debug("Unable to connect to zookeeper", e);
			}
		}
		if (log.isTraceEnabled()) {
			log.trace("connected to zookeeper");
		}
		return curator;
	}

	protected RetryPolicy retryPolicy(ZookeeperProperties properties) {
		return new ExponentialBackoffRetry(properties.getBaseSleepTimeMs(), properties.getMaxRetries(),
				properties.getMaxSleepMs());
	}

	protected ZookeeperProperties loadProperties(Binder binder, UriComponents location) {
		ZookeeperProperties properties = binder.bind(ZookeeperProperties.PREFIX, Bindable.of(ZookeeperProperties.class))
				.orElse(new ZookeeperProperties());

		if (location != null && StringUtils.hasText(location.getHost())) {
			if (location.getPort() < 0) {
				throw new IllegalArgumentException("zookeeper port must be greater than or equal to zero: " + location.getPort());
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
