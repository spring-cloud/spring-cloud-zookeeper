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

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;

public class ZookeeperConfigDataLoader implements ConfigDataLoader<ZookeeperConfigDataLocation> {

	private static final Log log = LogFactory.getLog(ZookeeperPropertySourceLocator.class);

	@Override
	public ConfigData load(ConfigDataLoaderContext context, ZookeeperConfigDataLocation location) {
		try {
			ZookeeperPropertySource propertySource = new ZookeeperPropertySource(location.getContext(),
					location.getCurator());
			return new ConfigData(Collections.singletonList(propertySource));
		}
		catch (Exception e) {
			if (location.getProperties().isFailFast() || !location.isOptional()) {
				throw new ConfigDataLocationNotFoundException(location, e);
			}
			else {
				log.warn("Unable to load zookeeper config from " + location.getContext(), e);
			}
		}
		return null;
	}

}
