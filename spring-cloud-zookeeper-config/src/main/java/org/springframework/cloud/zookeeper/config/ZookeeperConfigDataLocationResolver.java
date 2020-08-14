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
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.util.ReflectionUtils;

public class ZookeeperConfigDataLocationResolver implements ConfigDataLocationResolver<ZookeeperConfigDataLocation> {

	private static final Log log = LogFactory.getLog(ZookeeperAutoConfiguration.class);

	@Override
	public boolean isResolvable(ConfigDataLocationResolverContext context, String location) {
		boolean zkEnabled = context.getBinder().bind(ZookeeperProperties.PREFIX + ".enabled", Boolean.class)
				.orElse(true);
		boolean zkConfigEnabled = context.getBinder().bind(ZookeeperConfigProperties.PREFIX + ".enabled", Boolean.class)
				.orElse(true);
		return location.startsWith("zookeeper:") && zkConfigEnabled && zkEnabled;
	}

	@Override
	public List<ZookeeperConfigDataLocation> resolve(ConfigDataLocationResolverContext context, String location) {
		return Collections.emptyList();
	}

	@Override
	public List<ZookeeperConfigDataLocation> resolveProfileSpecific(ConfigDataLocationResolverContext context, String location, Profiles profiles) {
		// TODO use location for host:port
		CuratorFramework curator = curatorFramework(loadProperties(context.getBinder()));
		String appName = context.getBinder().bind("spring.application.name", String.class)
				.orElse("application");

		ZookeeperConfigProperties properties = loadConfigProperties(context.getBinder());
		String root = properties.getRoot();
		List<String> contexts = new ArrayList<>();

		String defaultContext = root + "/" + properties.getDefaultContext();
		contexts.add(defaultContext);
		addProfiles(contexts, defaultContext, profiles, properties);

		StringBuilder baseContext = new StringBuilder(root);
		if (!appName.startsWith("/")) {
			baseContext.append("/");
		}
		baseContext.append(appName);
		contexts.add(baseContext.toString());
		addProfiles(contexts, baseContext.toString(), profiles, properties);

		Collections.reverse(contexts);

		ArrayList<ZookeeperConfigDataLocation> locations = new ArrayList<>();
		contexts.forEach(propertySourceContext -> locations.add(new ZookeeperConfigDataLocation(curator, properties, propertySourceContext)));

		return locations;
	}

	private void addProfiles(List<String> contexts, String baseContext,
			Profiles profiles, ZookeeperConfigProperties properties) {
		for (String profile : profiles.getAccepted()) {
			contexts.add(baseContext + properties.getProfileSeparator() + profile);
		}
	}

	protected CuratorFramework curatorFramework(ZookeeperProperties properties) {
		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();

		builder.connectString(properties.getConnectString())
				.sessionTimeoutMs((int) properties.getSessionTimeout().toMillis())
				.connectionTimeoutMs((int) properties.getConnectionTimeout().toMillis())
				.retryPolicy(retryPolicy(properties));

		CuratorFramework curator = builder.build();

		curator.start();
		if (log.isTraceEnabled()) {
			log.trace("blocking until connected to zookeeper for "
					+ properties.getBlockUntilConnectedWait()
					+ properties.getBlockUntilConnectedUnit());
		}
		try {
			curator.blockUntilConnected(properties.getBlockUntilConnectedWait(),
					properties.getBlockUntilConnectedUnit());
		}
		catch (InterruptedException e) {
			log.error("Unable to connect to zookeepr", e);
			ReflectionUtils.rethrowRuntimeException(e);
		}
		if (log.isTraceEnabled()) {
			log.trace("connected to zookeeper");
		}
		return curator;
	}

	protected RetryPolicy retryPolicy(ZookeeperProperties properties) {
		return new ExponentialBackoffRetry(properties.getBaseSleepTimeMs(),
				properties.getMaxRetries(), properties.getMaxSleepMs());
	}

	protected ZookeeperProperties loadProperties(Binder binder) {
		ZookeeperProperties properties = binder
				.bind(ZookeeperProperties.PREFIX,
						Bindable.of(ZookeeperProperties.class))
				.orElse(new ZookeeperProperties());
		return properties;
	}

	protected ZookeeperConfigProperties loadConfigProperties(Binder binder) {
		ZookeeperConfigProperties properties = binder
				.bind(ZookeeperConfigProperties.PREFIX,
						Bindable.of(ZookeeperConfigProperties.class))
				.orElse(new ZookeeperConfigProperties());
		return properties;
	}

}
