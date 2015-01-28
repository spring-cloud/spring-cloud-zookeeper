package org.springframework.cloud.zookeeper.discovery;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.AbstractDiscoveryLifecycle;

/**
 * @author Spencer Gibb
 */
@Slf4j
public class ZookeeperLifecycle extends AbstractDiscoveryLifecycle {

	@Autowired
	private ZookeeperDiscoveryProperties properties;

	@Autowired
	private ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

	@Autowired
	private ServiceInstance<ZookeeperInstance> instance;

	@Override
	@SneakyThrows
	protected void register() {
		serviceDiscovery.start();
	}

	// TODO: implement registerManagement

	@Override
	@SneakyThrows
	protected void deregister() {
		serviceDiscovery.unregisterService(instance);
	}

	// TODO: implement deregisterManagement

	@Override
	protected boolean isEnabled() {
		return properties.isEnabled();
	}

	@Override
	protected Object getConfiguration() {
		return properties;
	}
}
