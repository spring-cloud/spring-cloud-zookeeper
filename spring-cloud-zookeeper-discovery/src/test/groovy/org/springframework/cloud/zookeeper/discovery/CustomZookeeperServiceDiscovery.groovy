package org.springframework.cloud.zookeeper.discovery

import javax.annotation.PreDestroy

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder
import org.apache.curator.x.discovery.ServiceInstance
import org.apache.curator.x.discovery.UriSpec

class CustomZookeeperServiceDiscovery extends ZookeeperServiceDiscovery {

	private final String applicationName
	private final String basePath

	CustomZookeeperServiceDiscovery(String applicationName, String basePath, CuratorFramework curator) {
		super(curator, null, null)
		this.applicationName = applicationName
		this.basePath = basePath
		build()
	}

	CustomZookeeperServiceDiscovery(String applicationName, CuratorFramework curator) {
		this(applicationName, '/', curator)
	}

	@Override
	void build() {
		setPort(10)
		def instance = ServiceInstance.builder().uriSpec(new UriSpec("{scheme}://{address}:{port}/"))
				.address('anyUrl')
				.port(10)
				.name(applicationName)
				.build()
		getServiceInstanceRef().set(instance)
		def discovery = ServiceDiscoveryBuilder
				.builder(Void)
				.basePath(basePath)
				.client(getCurator())
				.thisInstance(instance)
				.build()
		getServiceDiscoveryRef().set(discovery)
		discovery.start()
	}

	@PreDestroy
	void close() {
		getServiceDiscoveryRef().get().close()
	}
}