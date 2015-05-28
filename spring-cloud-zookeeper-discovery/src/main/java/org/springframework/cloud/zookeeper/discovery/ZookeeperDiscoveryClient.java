package org.springframework.cloud.zookeeper.discovery;

import lombok.SneakyThrows;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * @author Spencer Gibb
 * @author Marcin Grzejszczak, 4financeIT
 */
public class ZookeeperDiscoveryClient implements DiscoveryClient {

	@Autowired
	ApplicationContext context;

	@Autowired(required = false)
	ServiceInstance<ZookeeperInstance> instance;

	@Autowired
	ServiceDiscovery<ZookeeperInstance> discovery;

	@Autowired(required = false)
	ZookeeperDependencies zookeeperDependencies;

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

		return createServiceInstance(instance.getId(), instance);
	}

	private static org.springframework.cloud.client.ServiceInstance createServiceInstance(String serviceId, ServiceInstance<ZookeeperInstance> serviceInstance) {
		boolean secure = serviceInstance.getSslPort() != null;
		Integer port = serviceInstance.getPort();

		if (secure) {
			port = serviceInstance.getSslPort();
		}

		return new DefaultServiceInstance(serviceId, serviceInstance.getAddress(), port, secure);
	}

	@Override
	@SneakyThrows
	public List<org.springframework.cloud.client.ServiceInstance> getInstances(
			final String serviceId) {
		String serviceIdToQuery = getServiceIdToQuery(serviceId);
		Collection<ServiceInstance<ZookeeperInstance>> zkInstances = discovery
				.queryForInstances(serviceIdToQuery);

		ArrayList<org.springframework.cloud.client.ServiceInstance> instances = new ArrayList<>();

		for (ServiceInstance<ZookeeperInstance> instance : zkInstances) {
			instances.add(createServiceInstance(serviceIdToQuery, instance));
		}

		return instances;
	}

	private String getServiceIdToQuery(String serviceId) {
		if (zookeeperDependencies != null && zookeeperDependencies.hasDependencies()) {
			return zookeeperDependencies.getPathForAlias(serviceId);
		}
		return serviceId;
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
