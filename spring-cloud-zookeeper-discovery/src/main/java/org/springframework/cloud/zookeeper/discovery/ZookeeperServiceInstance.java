package org.springframework.cloud.zookeeper.discovery;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;

/**
 * A specific {@link ServiceInstance} describing a zookeeper service instance
 * Created on 01/03/17.
 *
 * @author Reda.Housni-Alaoui
 */
public class ZookeeperServiceInstance implements ServiceInstance {

	private final String serviceId;
	private final String host;
	private final int port;
	private final boolean secure;
	private final URI uri;
	private final Map<String, String> metadata;

	/**
	 * @param serviceId The service id to be used
	 * @param serviceInstance The zookeeper service instance described by this service instance
	 */
	public ZookeeperServiceInstance(String serviceId, org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> serviceInstance) {
		this.serviceId = serviceId;
		this.host = serviceInstance.getAddress();
		this.secure = serviceInstance.getSslPort() != null;
		Integer port = serviceInstance.getPort();
		if (this.secure) {
			port = serviceInstance.getSslPort();
		}
		this.port = port;
		this.uri = URI.create(serviceInstance.buildUriSpec());
		if (serviceInstance.getPayload() != null) {
			this.metadata = serviceInstance.getPayload().getMetadata();
		} else {
			this.metadata = new HashMap<>();
		}
	}

	@Override
	public String getServiceId() {
		return this.serviceId;
	}

	@Override
	public String getHost() {
		return this.host;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public boolean isSecure() {
		return this.secure;
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public Map<String, String> getMetadata() {
		return this.metadata;
	}
}
