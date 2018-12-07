/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * A {@link org.springframework.boot.actuate.health.HealthIndicator} that checks the
 * status of the Zookeeper connection.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
public class ZookeeperHealthIndicator extends AbstractHealthIndicator {
	private final CuratorFramework curator;

	public ZookeeperHealthIndicator(CuratorFramework curator) {
		this.curator = curator;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		try {
			CuratorFrameworkState state = this.curator.getState();
			if (state != CuratorFrameworkState.STARTED) {
				builder.down().withDetail("error", "Client not started");
			}
			else if (this.curator.checkExists().forPath("/") == null) {
				builder.down().withDetail("error", "Root for namespace does not exist");
			}
			else {
				builder.up();
			}
			builder.withDetail("connectionString",
					this.curator.getZookeeperClient().getCurrentConnectionString())
					.withDetail("state", state);
		}
		catch (Exception e) {
			builder.down(e);
		}
	}
}
