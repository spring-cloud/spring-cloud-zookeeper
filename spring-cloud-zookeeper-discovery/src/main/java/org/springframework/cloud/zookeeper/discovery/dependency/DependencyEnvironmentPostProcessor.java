/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.zookeeper.discovery.dependency;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * EnvironmentPostProcessor that sets spring.application.name.
 * Specifically, if spring.application.name doesn't contain a / and
 * spring.cloud.zookeeper.prefix has text, it sets spring.application.name
 * to /${spring.cloud.zookeeper.prefix}/${spring.application.name}
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
public class DependencyEnvironmentPostProcessor
		implements EnvironmentPostProcessor, Ordered {

	// after ConfigFileEnvironmentPostProcessorr
	private int order = ConfigFileApplicationListener.DEFAULT_ORDER + 1;

	@Override public int getOrder() {
		return this.order;
	}

	@Override public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		String appName = environment.getProperty("spring.application.name");
		if (StringUtils.hasText(appName) && !appName.contains("/")) {
			String prefix = environment.getProperty("spring.cloud.zookeeper.prefix");
			if (StringUtils.hasText(prefix)) {
				StringBuilder prefixedName = new StringBuilder();
				if (!prefix.startsWith("/")) {
					prefixedName.append("/");
				}
				prefixedName.append(prefix);
				if (!prefix.endsWith("/")) {
					prefixedName.append("/");
				}
				prefixedName.append(appName);
				MapPropertySource propertySource = new MapPropertySource(
						"zookeeperDependencyEnvironment", Collections
						.singletonMap("spring.application.name",
								(Object) prefixedName.toString()));
				environment.getPropertySources().addFirst(propertySource);
			}
		}
	}
}
