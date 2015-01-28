package org.springframework.cloud.zookeeper.discovery;

import java.util.ArrayList;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * @author Spencer Gibb
 */
@Slf4j
public class ZookeeperDiscoveryHealthIndicator extends AbstractHealthIndicator {
	@Autowired
	ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		try {
			Collection<String> names = serviceDiscovery.queryForNames();
			ArrayList<ServiceInstance<ZookeeperInstance>> allInstances = new ArrayList<>();
			for (String name : names) {
				Collection<ServiceInstance<ZookeeperInstance>> instances = serviceDiscovery
						.queryForInstances(name);
				for (ServiceInstance<ZookeeperInstance> instance : instances) {
					allInstances.add(instance);
				}
			}
			builder.up().withDetail("services", allInstances);
		}
		catch (Exception e) {
			log.error("Error", e);
			builder.down(e);
		}
	}
}
