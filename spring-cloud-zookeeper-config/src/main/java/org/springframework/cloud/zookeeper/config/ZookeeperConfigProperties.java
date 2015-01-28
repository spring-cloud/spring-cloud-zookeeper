package org.springframework.cloud.zookeeper.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("zookeeper.config")
@Data
public class ZookeeperConfigProperties {
	private boolean enabled = true;

	private String root = "config";
}
