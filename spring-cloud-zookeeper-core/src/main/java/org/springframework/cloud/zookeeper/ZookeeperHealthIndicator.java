package org.springframework.cloud.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * @author Spencer Gibb
 */
public class ZookeeperHealthIndicator extends AbstractHealthIndicator {
	@Autowired
	CuratorFramework curator;

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		try {
			if (curator.getState() != CuratorFrameworkState.STARTED) {
				builder.down().withDetail("error", "Client not started");
			}
			else if (curator.checkExists().forPath("/") == null) {
				builder.down().withDetail("error", "Root for namespace does not exist");
			}
			else {
				builder.up();
			}
			builder.withDetail("connectionString",
					curator.getZookeeperClient().getCurrentConnectionString())
					.withDetail("state", curator.getState());
		}
		catch (Exception e) {
			builder.down(e);
		}
	}
}
