package org.springframework.cloud.zookeeper.discovery;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Spencer Gibb
 */
@Data
@AllArgsConstructor
public class ZookeeperInstance {
	private String id;

	private ZookeeperInstance() {
	}
}
