package org.springframework.cloud.zookeeper.discovery;

import javax.annotation.PreDestroy;

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;

public class CustomZookeeperServiceDiscovery extends ZookeeperServiceDiscovery {

	private final String applicationName;
	private final String basePath;

	public CustomZookeeperServiceDiscovery(String applicationName, String basePath, CuratorFramework curator) {
		super(curator, null, null);
		this.applicationName = applicationName;
		this.basePath = basePath;
		build();
	}

	public CustomZookeeperServiceDiscovery(String applicationName, CuratorFramework curator) {
		this(applicationName, "/", curator);
	}

	@Override
	public void build(){
		try {
			setPort(10);
			ServiceInstance instance = ServiceInstance.builder().uriSpec(new UriSpec("{scheme}://{address}:{port}/"))
					.address("anyUrl")
					.port(10)
					.name(this.applicationName)
					.build();
			getServiceInstanceRef().set(instance);
			ServiceDiscovery discovery = ServiceDiscoveryBuilder
					.builder(Void.class)
					.basePath(this.basePath)
					.client(getCurator())
					.thisInstance(instance)
					.build();
			getServiceDiscoveryRef().set(discovery);
			discovery.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@PreDestroy
	void close() {
		try {
			getServiceDiscoveryRef().get().close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}