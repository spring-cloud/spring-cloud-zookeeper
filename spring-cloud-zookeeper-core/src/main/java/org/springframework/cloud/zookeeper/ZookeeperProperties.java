package org.springframework.cloud.zookeeper;

import javax.validation.constraints.NotNull;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("zookeeper")
@Data
public class ZookeeperProperties {
	@NotNull
	private String connectString = "localhost:2181";

	private boolean enabled = true;
}
