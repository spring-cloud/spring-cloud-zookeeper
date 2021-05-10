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
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.zookeeper.CuratorFactory;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.cloud.zookeeper.config.ZookeeperPropertySources.Context;
import org.springframework.core.env.MapPropertySource;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class ZookeeperConfigDataLocationResolver implements ConfigDataLocationResolver<ZookeeperConfigDataResource> {

	/**
	 * Zookeeper Config Data prefix.
	 */
	public static final String PREFIX = "zookeeper:";

	private final Log log;

	public ZookeeperConfigDataLocationResolver(Log log) {
		this.log = log;
	}

	@Override
	public boolean isResolvable(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
		if (!location.hasPrefix(PREFIX)) {
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
	public List<ZookeeperConfigDataResource> resolve(ConfigDataLocationResolverContext context, ConfigDataLocation location)
			throws ConfigDataLocationNotFoundException {
		return Collections.emptyList();
	}

	@Override
	public List<ZookeeperConfigDataResource> resolveProfileSpecific(ConfigDataLocationResolverContext context,
			ConfigDataLocation location, Profiles profiles) throws ConfigDataLocationNotFoundException {
		UriComponents locationUri = parseLocation(location);

		// create curator
		CuratorFactory.registerCurator(context.getBootstrapContext(), locationUri, location.isOptional());

		// create locations
		ZookeeperConfigProperties properties = loadConfigProperties(context);
		context.getBootstrapContext().register(ZookeeperConfigProperties.class, InstanceSupplier.of(properties));

		ZookeeperPropertySources sources = new ZookeeperPropertySources(properties, log);

		List<Context> contexts = (locationUri == null || CollectionUtils.isEmpty(locationUri.getPathSegments()))
				? sources.generateAutomaticContexts(profiles.getAccepted(), false) : getCustomContexts(locationUri);

		// promote beans to context
		context.getBootstrapContext().addCloseListener(event -> {
			HashMap<String, Object> source = new HashMap<>();
			source.put("spring.cloud.zookeeper.config.property-source-contexts", contexts.stream().map(Context::getPath).collect(Collectors.toList()));
			MapPropertySource propertySource = new MapPropertySource("zookeeperConfigData", source);
			event.getApplicationContext().getEnvironment().getPropertySources().addFirst(propertySource);
		});

		ArrayList<ZookeeperConfigDataResource> locations = new ArrayList<>();
		contexts.forEach(propertySourceContext -> locations
				.add(new ZookeeperConfigDataResource(propertySourceContext.getPath(), location.isOptional(), propertySourceContext
						.getProfile())));

		return locations;
	}

	private BindHandler getBindHandler(ConfigDataLocationResolverContext context) {
		return context.getBootstrapContext().getOrElse(BindHandler.class, null);
	}

	protected List<Context> getCustomContexts(UriComponents uriComponents) {
		if (!StringUtils.hasLength(uriComponents.getPath())) {
			return Collections.emptyList();
		}

		return Arrays.stream(uriComponents.getPath().split(";")).map(Context::new).collect(Collectors.toList());
	}

	@Nullable
	protected UriComponents parseLocation(ConfigDataLocation location) {
		String originalUri = location.getNonPrefixedValue(PREFIX);
		if (!StringUtils.hasText(originalUri)) {
			return null;
		}
		String uri;
		if (!originalUri.startsWith("//")) {
			uri = PREFIX + "//" + originalUri;
		}
		else {
			uri = originalUri;
		}
		return UriComponentsBuilder.fromUriString(uri).build();
	}

	protected ZookeeperConfigProperties loadConfigProperties(ConfigDataLocationResolverContext context) {
		Binder binder = context.getBinder();
		BindHandler bindHandler = getBindHandler(context);
		ZookeeperConfigProperties properties = binder
				.bind(ZookeeperConfigProperties.PREFIX, Bindable.of(ZookeeperConfigProperties.class), bindHandler)
				.orElseGet(ZookeeperConfigProperties::new);

		if (!StringUtils.hasLength(properties.getName())) {
			properties.setName(binder.bind("spring.application.name", Bindable.of(String.class), bindHandler).orElse("application"));
		}

		return properties;
	}

}
