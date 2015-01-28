package org.springframework.cloud.zookeeper;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties(prefix = "endpoints.zookeeper", ignoreUnknownFields = false)
public class ZookeeperEndpoint extends AbstractEndpoint<ZookeeperEndpoint.ZookeeperData> {

	@Autowired
	public ZookeeperEndpoint() {
		super("zookeeper", false, true);
	}

	@Override
	public ZookeeperData invoke() {
		ZookeeperData data = new ZookeeperData();
		return data;
	}

	@Data
	public static class ZookeeperData {
	}
}
