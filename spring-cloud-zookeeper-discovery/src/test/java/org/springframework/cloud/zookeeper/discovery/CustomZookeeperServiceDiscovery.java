package org.springframework.cloud.zookeeper.discovery;

import javax.annotation.PreDestroy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.context.ApplicationContext;

public class CustomZookeeperServiceDiscovery extends ZookeeperServiceDiscovery {

	private final String applicationName;
	private final String basePath;

	public CustomZookeeperServiceDiscovery(String applicationName, String basePath, CuratorFramework curator) {
		super(curator, new ZookeeperDiscoveryProperties(new InetUtils(new InetUtilsProperties())), null);
		this.applicationName = applicationName;
		this.basePath = basePath;
	}

	public CustomZookeeperServiceDiscovery(String applicationName, CuratorFramework curator) {
		this(applicationName, "/", curator);
	}

	@Override
	public void configureServiceInstance(AtomicReference<ServiceInstance<ZookeeperInstance>> serviceInstance, String appName, ApplicationContext context, AtomicInteger port, String host, UriSpec uriSpec) {
		setPort(10);
		try {
			ServiceInstance instance = ServiceInstance.builder().uriSpec(new UriSpec("{scheme}://{address}:{port}/"))
					.address("anyUrl")
					.port(10)
					.name(this.applicationName)
					.build();
			serviceInstance.set(instance);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void configureServiceDiscovery(AtomicReference<ServiceDiscovery<ZookeeperInstance>> serviceDiscovery, CuratorFramework curator, ZookeeperDiscoveryProperties properties, InstanceSerializer<ZookeeperInstance> instanceSerializer, AtomicReference<ServiceInstance<ZookeeperInstance>> serviceInstance) {
		ServiceDiscovery discovery = ServiceDiscoveryBuilder
				.builder(ZookeeperInstance.class)
				.basePath(this.basePath)
				.client(getCurator())
				//.thisInstance(serviceInstance.get())
				.build();
		serviceDiscovery.set(discovery);
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
