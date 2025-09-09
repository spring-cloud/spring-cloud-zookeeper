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

package org.springframework.cloud.zookeeper.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.curator.framework.CuratorFramework;

import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.util.StringUtils;

public class ZookeeperConfigDataLoader implements ConfigDataLoader<ZookeeperConfigDataResource> {

	private final Log log;

	public ZookeeperConfigDataLoader(DeferredLogFactory logFactory) {
		this.log = logFactory.getLog(ZookeeperConfigDataLoader.class);
	}

	@Override
	public ConfigData load(ConfigDataLoaderContext context, ZookeeperConfigDataResource resource) {
		try {
			CuratorFramework curator = context.getBootstrapContext().get(CuratorFramework.class);
			if (curator == null) {
				// this can happen if certain conditions are met
				return null;
			}
			ZookeeperPropertySource propertySource = new ZookeeperPropertySource(resource.getContext(),
					curator);
			List<ZookeeperPropertySource> propertySources = Collections.singletonList(propertySource);

			return new ConfigData(propertySources, source -> {
				List<ConfigData.Option> options = new ArrayList<>();
				options.add(ConfigData.Option.IGNORE_IMPORTS);
				options.add(ConfigData.Option.IGNORE_PROFILES);
				if (StringUtils.hasText(resource.getProfile())) {
					options.add(ConfigData.Option.PROFILE_SPECIFIC);
				}
				return ConfigData.Options.of(options.toArray(new ConfigData.Option[0]));
			});

		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Error getting properties from zookeeper: " + resource, e);
			}
			throw new ConfigDataResourceNotFoundException(resource, e);
		}
	}

}
