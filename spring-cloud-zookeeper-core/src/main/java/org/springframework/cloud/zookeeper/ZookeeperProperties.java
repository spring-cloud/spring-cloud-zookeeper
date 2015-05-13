package org.springframework.cloud.zookeeper;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("zookeeper")
@Data
public class ZookeeperProperties {
	@NotNull
	private String connectString = "localhost:2181";

	private boolean enabled = true;

	/**
	 * @param baseSleepTimeMs initial amount of time to wait between retries
	 */
	private Integer baseSleepTimeMs = 50;

	/**
	 * @param maxRetries max number of times to retry
	 */
	private Integer maxRetries = 50;

	/**
	 * @param maxSleepMs max time in ms to sleep on each retry
	 */
	private Integer maxSleepMs = 500;

}
