package org.springframework.cloud.zookeeper.discovery;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import lombok.SneakyThrows;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

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

		Iterable<org.springframework.cloud.client.ServiceInstance> instances = transform(
				zkInstances,
				new Function<ServiceInstance<ZookeeperInstance>, org.springframework.cloud.client.ServiceInstance>() {
					@Nullable
					@Override
					public org.springframework.cloud.client.ServiceInstance apply(
							@Nullable ServiceInstance<ZookeeperInstance> input) {
						return new DefaultServiceInstance(serviceId, input.getAddress(),
								input.getPort());
					}
				});

		return Lists.newArrayList(instances);
	}

	@Override
	@SneakyThrows
	public List<org.springframework.cloud.client.ServiceInstance> getAllInstances() {
		Iterable<org.springframework.cloud.client.ServiceInstance> instances = transform(
				concat(transform(
						discovery.queryForNames(),
						new Function<String, Collection<ServiceInstance<ZookeeperInstance>>>() {
							@SneakyThrows
							public Collection<ServiceInstance<ZookeeperInstance>> apply(
									String input) {
								return discovery.queryForInstances(input);
							}
						})),
				new Function<ServiceInstance<ZookeeperInstance>, org.springframework.cloud.client.ServiceInstance>() {
					public org.springframework.cloud.client.ServiceInstance apply(
							ServiceInstance<ZookeeperInstance> input) {
						return new DefaultServiceInstance(input.getName(), input
								.getAddress(), input.getPort());
					}
				});

		return Lists.newArrayList(instances);
	}

	@Override
	public List<String> getServices() {
		ArrayList<String> services = null;
		try {
			services = Lists.newArrayList(discovery.queryForNames());
		}
		catch (Exception e) {
			Throwables.propagate(e);
		}
		return services;
	}
}
