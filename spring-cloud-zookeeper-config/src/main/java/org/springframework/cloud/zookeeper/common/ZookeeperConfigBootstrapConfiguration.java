package org.springframework.cloud.zookeeper.common;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Spencer Gibb
 */
@Configuration
@Import(ZookeeperAutoConfiguration.class)
@EnableConfigurationProperties
public class ZookeeperConfigBootstrapConfiguration {
	@Bean
	public ZookeeperPropertySourceLocator zookeeperPropertySourceLocator() {
		return new ZookeeperPropertySourceLocator();
	}

	@Bean
	public ZookeeperConfigProperties zookeeperConfigProperties() {
		return new ZookeeperConfigProperties();
	}
}
