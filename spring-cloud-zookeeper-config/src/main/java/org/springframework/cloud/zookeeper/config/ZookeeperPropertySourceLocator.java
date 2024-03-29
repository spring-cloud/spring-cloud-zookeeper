/*
 * Copyright 2015-2019 the original author or authors.
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

import java.util.Arrays;
import java.util.List;

import jakarta.annotation.PreDestroy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;

import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

/**
 * Zookeeper provides a <a href=
 * "https://zookeeper.apache.org/doc/current/zookeeperOver.html#sc_dataModelNameSpace">hierarchical
 * namespace</a> that allows clients to store arbitrary data, such as configuration data.
 * Spring Cloud Zookeeper Config is an alternative to the
 * <a href="https://github.com/spring-cloud/spring-cloud-config">Config Server and
 * Client</a>. Configuration is loaded into the Spring Environment during the special
 * "bootstrap" phase. Configuration is stored in the {@code /config} namespace by default.
 * Multiple {@code PropertySource} instances are created based on the application's name
 * and the active profiles that mimicks the Spring Cloud Config order of resolving
 * properties. For example, an application with the name "testApp" and with the "dev"
 * profile will have the following property sources created:
 *
 * <pre>{@code
 * config/testApp,dev
 * config/testApp
 * config/application,dev
 * config/application
 * }</pre>
 *
 * The most specific property source is at the top, with the least specific at the bottom.
 * Properties is the {@code config/application} namespace are applicable to all
 * applications using zookeeper for configuration. Properties in the
 * {@code config/testApp} namespace are only available to the instances of the service
 * named "testApp".
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
public class ZookeeperPropertySourceLocator implements PropertySourceLocator {

	private ZookeeperConfigProperties properties;

	private CuratorFramework curator;

	private List<String> contexts;

	private static final Log log = LogFactory
			.getLog(ZookeeperPropertySourceLocator.class);

	public ZookeeperPropertySourceLocator(CuratorFramework curator,
			ZookeeperConfigProperties properties) {
		this.curator = curator;
		Assert.hasText(properties.getName(), ZookeeperConfigProperties.PREFIX + ".name must not be empty");
		this.properties = properties;
	}

	public List<String> getContexts() {
		return this.contexts;
	}

	@Override
	public PropertySource<?> locate(Environment environment) {
		if (environment instanceof ConfigurableEnvironment) {
			ConfigurableEnvironment env = (ConfigurableEnvironment) environment;

			List<String> profiles = Arrays.asList(env.getActiveProfiles());

			ZookeeperPropertySources sources = new ZookeeperPropertySources(properties, log);
			this.contexts = sources.getAutomaticContexts(profiles);

			CompositePropertySource composite = new CompositePropertySource("zookeeper");

			for (String propertySourceContext : this.contexts) {
				PropertySource<CuratorFramework> propertySource = sources.createPropertySource(propertySourceContext, true, this.curator);
				composite.addPropertySource(propertySource);
			}

			return composite;
		}
		return null;
	}

	@PreDestroy
	public void destroy() {
	}

	private PropertySource<CuratorFramework> create(String context) {
		return new ZookeeperPropertySource(context, this.curator);
	}

	private void addProfiles(List<String> contexts, String baseContext,
			List<String> profiles) {
		for (String profile : profiles) {
			contexts.add(baseContext + this.properties.getProfileSeparator() + profile);
		}
	}

}
