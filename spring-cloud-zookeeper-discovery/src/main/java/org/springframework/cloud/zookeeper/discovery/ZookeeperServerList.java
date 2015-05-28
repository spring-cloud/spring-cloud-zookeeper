package org.springframework.cloud.zookeeper.discovery;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * @author Spencer Gibb
 * @author Marcin Grzejszczak, 4financeIT
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

	public void initFromDependencies(IClientConfig clientConfig, ZookeeperDependencies zookeeperDependencies) {
		this.serviceId = zookeeperDependencies.getPathForAlias(clientConfig.getClientName());
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
			List<ZookeeperServer> servers = new ArrayList<>();
			for (ServiceInstance<ZookeeperInstance> instance : instances) {
				servers.add(new ZookeeperServer(instance));
			}

			return servers;
		}
		catch (Exception e) {
			rethrowRuntimeException(e);
		}
		return Collections.EMPTY_LIST;
	}
}
