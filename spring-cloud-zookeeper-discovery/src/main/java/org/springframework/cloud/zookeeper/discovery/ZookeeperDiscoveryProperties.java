package org.springframework.cloud.zookeeper.discovery;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("zookeeper.discovery")
@Data
public class ZookeeperDiscoveryProperties {
	private boolean enabled = true;

	private String root = "/services";

	private String uriSpec = "{scheme}://{address}:{port}";

	private String instanceHost;
}
