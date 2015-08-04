package org.springframework.cloud.zookeeper.common;

import lombok.Data;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.zookeeper.config")
@Data
public class ZookeeperConfigProperties {
	private boolean enabled = true;

	private String root = "config";

	@NotEmpty
	private String defaultContext = "application";

	@NotEmpty
	private String profileSeparator = ",";
}
