package org.springframework.cloud.zookeeper.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.SneakyThrows;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;

import static org.springframework.util.ReflectionUtils.*;

/**
 * @author Spencer Gibb
 */
public class ZookeeperDiscoveryClient implements DiscoveryClient {

	@Autowired
	ApplicationContext context;

	@Autowired(required = false)
	ServiceInstance<ZookeeperInstance> instance;

	@Autowired
	ServiceDiscovery<ZookeeperInstance> discovery;

	@Override
	public String description() {
		return "Spring Cloud Zookeeper Discovery Client";
	}

	@Override
	public org.springframework.cloud.client.ServiceInstance getLocalServiceInstance() {
		if (instance == null) {
			throw new IllegalStateException("Unable to locate instance in zookeeper: "
					+ context.getId());
		}
		return new DefaultServiceInstance(instance.getId(), instance.getAddress(),
				instance.getPort());
	}

	@Override
	@SneakyThrows
	public List<org.springframework.cloud.client.ServiceInstance> getInstances(
			final String serviceId) {
		Collection<ServiceInstance<ZookeeperInstance>> zkInstances = discovery
				.queryForInstances(serviceId);

		ArrayList<org.springframework.cloud.client.ServiceInstance> instances = new ArrayList<>();

		for (ServiceInstance<ZookeeperInstance> instance : zkInstances) {
			instances.add(new DefaultServiceInstance(serviceId, instance.getAddress(),
					instance.getPort()));
		}

		return instances;
	}

	@Override
	@SneakyThrows
	public List<org.springframework.cloud.client.ServiceInstance> getAllInstances() {
		List<org.springframework.cloud.client.ServiceInstance> instances = new ArrayList<>();

		for (String name : discovery.queryForNames()) {
			for (ServiceInstance<ZookeeperInstance> instance: discovery.queryForInstances(name)) {
				instances.add(new DefaultServiceInstance(instance.getName(), instance
						.getAddress(), instance.getPort()));
			}
		}

		return instances;
	}

	@Override
	public List<String> getServices() {
		ArrayList<String> services = null;
		try {
			services = new ArrayList<>(discovery.queryForNames());
		}
		catch (Exception e) {
			rethrowRuntimeException(e);
		}
		return services;
	}
}
