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

package org.springframework.cloud.zookeeper.config;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;

/**
 * @author Spencer Gibb
 */
@Configuration
@Import(ZookeeperAutoConfiguration.class)
@EnableConfigurationProperties
public class ZookeeperConfigBootstrapConfiguration {
	@Bean
	public ZookeeperPropertySourceLocator zookeeperPropertySourceLocator(
			CuratorFramework curator, ZookeeperConfigProperties properties) {
		return new ZookeeperPropertySourceLocator(curator, properties);
	}

	@Bean
	@ConditionalOnMissingBean
	public ZookeeperConfigProperties zookeeperConfigProperties() {
		return new ZookeeperConfigProperties();
	}

	@Bean
	public ApplicationListener<ContextRefreshedEvent> zookeeperTreeCachePropertySourceContextRefreshedApplicationListener() {
		return new ApplicationListener<ContextRefreshedEvent>() {

			@Override
			public void onApplicationEvent(ContextRefreshedEvent event) {
				ApplicationContext context = event.getApplicationContext();
				if (!(context instanceof ConfigurableApplicationContext)) {
					return;
				}
				@SuppressWarnings("resource")
				ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
				for (PropertySource<?> propertySource : configurableContext.getEnvironment().getPropertySources()) {
					walkPropertySourceTree(propertySource, configurableContext);
				}
			}

			private void walkPropertySourceTree(PropertySource<?> propertySource, ConfigurableApplicationContext context) {
				if (propertySource instanceof CompositePropertySource) {
					CompositePropertySource compositePropertySource = (CompositePropertySource) propertySource;
					for (PropertySource<?> childPropertySource : compositePropertySource.getPropertySources()) {
						walkPropertySourceTree(childPropertySource, context);
					}
				} else if (propertySource instanceof ZookeeperTreeCachePropertySource) {
					ZookeeperTreeCachePropertySource zkPropertySource = (ZookeeperTreeCachePropertySource) propertySource;
					zkPropertySource.setApplicationContext(context);
				}
			}
		};
	}
}
