package org.springframework.cloud.zookeeper.discovery;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;

/**
 * @author Spencer Gibb
 */
public class ZookeeperServerList extends AbstractServerList<ZookeeperServer> {

	private String serviceId;
	private final ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

	public ZookeeperServerList(ServiceDiscovery<ZookeeperInstance> serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig clientConfig) {
		this.serviceId = clientConfig.getClientName();
	}

	@Override
	public List<ZookeeperServer> getInitialListOfServers() {
		return getServers();
	}

	@Override
	public List<ZookeeperServer> getUpdatedListOfServers() {
		return getServers();
	}

	@SuppressWarnings("unchecked")
	private List<ZookeeperServer> getServers() {
		try {
			Collection<ServiceInstance<ZookeeperInstance>> instances = serviceDiscovery
					.queryForInstances(serviceId);
			if (instances == null || instances.isEmpty()) {
				return Collections.EMPTY_LIST;
			}
			Collection<ZookeeperServer> servers = transform(instances,
					new Function<ServiceInstance<ZookeeperInstance>, ZookeeperServer>() {
						@Nullable
						@Override
						public ZookeeperServer apply(
								@Nullable ServiceInstance<ZookeeperInstance> instance) {
							ZookeeperServer server = new ZookeeperServer(instance);
							return server;
						}
					});

			return newArrayList(servers);
		}
		catch (Exception e) {
			Throwables.propagate(e);
		}
		return Collections.EMPTY_LIST;
	}
}
