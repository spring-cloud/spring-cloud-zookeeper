package org.springframework.cloud.zookeeper.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PreDestroy;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * @author Spencer Gibb
 */
public class ZookeeperPropertySourceLocator implements PropertySourceLocator {

	@Autowired
	private ZookeeperConfigProperties properties;

	@Autowired
	private CuratorFramework curator;

	@Override
	public PropertySource<?> locate(Environment environment) {
		if (environment instanceof ConfigurableEnvironment) {
			ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
			String appName = env.getProperty("spring.application.name");
			List<String> profiles = Arrays.asList(env.getActiveProfiles());

			String root = properties.getRoot();
			List<String> contexts = new ArrayList<>();

			String defaultContext = root + "/" + properties.getDefaultContext();
			contexts.add(defaultContext);
			addProfiles(contexts, defaultContext, profiles);

			String baseContext = root + "/" + appName;
			contexts.add(baseContext);
			addProfiles(contexts, baseContext, profiles);

			CompositePropertySource composite = new CompositePropertySource("zookeeper");

			Collections.reverse(contexts);

			for (String propertySourceContext : contexts) {
				ZookeeperPropertySource propertySource = create(propertySourceContext);
				propertySource.start();
				composite.addPropertySource(propertySource);
				// TODO: howto call close when /refresh
			}

			return composite;
		}
		return null;
	}

	@PreDestroy
	public void destroy() {
		// TODO: call close on zkps's
	}

	private ZookeeperPropertySource create(String context) {
		return new ZookeeperPropertySource(context, curator);
	}

	private void addProfiles(List<String> contexts, String baseContext,
			List<String> profiles) {
		for (String profile : profiles) {
			contexts.add(baseContext + properties.getProfileSeparator() + profile);
		}
	}
}
