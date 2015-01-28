package org.springframework.cloud.zookeeper.discovery;

import org.apache.curator.x.discovery.ServiceInstance;

import com.netflix.loadbalancer.Server;

/**
 * @author Spencer Gibb
 */
public class ZookeeperServer extends Server {

	private final MetaInfo metaInfo;

	public ZookeeperServer(final ServiceInstance<ZookeeperInstance> instance) {
		// TODO: ssl support
		super(instance.getAddress(), instance.getPort());
		metaInfo = new MetaInfo() {
			@Override
			public String getAppName() {
				return instance.getName();
			}

			@Override
			public String getServerGroup() {
				return null;
			}

			@Override
			public String getServiceIdForDiscovery() {
				return null;
			}

			@Override
			public String getInstanceId() {
				return instance.getId();
			}
		};
	}

	@Override
	public MetaInfo getMetaInfo() {
		return metaInfo;
	}
}
