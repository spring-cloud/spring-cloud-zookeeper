package org.springframework.cloud.zookeeper.discovery;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

/**
 * @author Spencer Gibb
 */
public class ZookeeperServiceDiscovery implements ApplicationContextAware {

	private CuratorFramework curator;

	private ZookeeperDiscoveryProperties properties;

	private InstanceSerializer<ZookeeperInstance> instanceSerializer;

	private ApplicationContext context;

	private AtomicBoolean built = new AtomicBoolean(false);

	private AtomicInteger port = new AtomicInteger();

	private AtomicReference<ServiceInstance<ZookeeperInstance>> serviceInstance = new AtomicReference<>();

	private AtomicReference<ServiceDiscovery<ZookeeperInstance>> serviceDiscovery = new AtomicReference<>();

	@Value("${spring.application.name:application}")
	private String appName;

	public ZookeeperServiceDiscovery(CuratorFramework curator, ZookeeperDiscoveryProperties properties, InstanceSerializer<ZookeeperInstance> instanceSerializer) {
		this.curator = curator;
		this.properties = properties;
		this.instanceSerializer = instanceSerializer;
	}

	public int getPort() {
		return this.port.get();
	}

	public void setPort(int port) {
		this.port.set(port);
	}

	public ServiceInstance<ZookeeperInstance> getServiceInstance() {
		Assert.notNull(serviceInstance.get(), "serviceInstance has not been built");
		return serviceInstance.get();
	}

	public ServiceDiscovery<ZookeeperInstance> getServiceDiscovery() {
		Assert.notNull(serviceDiscovery.get(), "serviceDiscovery has not been built");
		return serviceDiscovery.get();
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@SneakyThrows
	public void build() {
		if (built.compareAndSet(false, true)) {
			if (port.get() <= 0) {
				throw new IllegalStateException("Cannot create instance whose port is not greater than 0");
			}
			String host = properties.getInstanceHost() == null? getIpAddress() : properties.getInstanceHost();
			UriSpec uriSpec = new UriSpec(properties.getUriSpec());
			// @formatter:off
			serviceInstance.set(ServiceInstance.<ZookeeperInstance>builder()
					.name(appName)
					.payload(new ZookeeperInstance(context.getId()))
					.port(port.get())
					.address(host)
					.uriSpec(uriSpec).build());

			serviceDiscovery.set(ServiceDiscoveryBuilder.builder(ZookeeperInstance.class)
					.client(curator)
					.basePath(properties.getRoot())
					.serializer(instanceSerializer)
					.thisInstance(serviceInstance.get())
					.build());
			// @formatter:on
		}
	}


	/**
	 * Return a non loopback IPv4 address for the machine running this process.
	 * If the machine has multiple network interfaces, the IP address for the
	 * first interface returned by {@link java.net.NetworkInterface#getNetworkInterfaces}
	 * is returned.
	 *
	 * @return non loopback IPv4 address for the machine running this process
	 * @see java.net.NetworkInterface#getNetworkInterfaces
	 * @see java.net.NetworkInterface#getInetAddresses
	 */
	public static String getIpAddress() {
		try {
			for (Enumeration<NetworkInterface> enumNic = NetworkInterface.getNetworkInterfaces();
				 enumNic.hasMoreElements(); ) {
				NetworkInterface ifc = enumNic.nextElement();
				if (ifc.isUp()) {
					for (Enumeration<InetAddress> enumAddr = ifc.getInetAddresses();
						 enumAddr.hasMoreElements(); ) {
						InetAddress address = enumAddr.nextElement();
						if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
							return address.getHostAddress();
						}
					}
				}
			}
		} catch (IOException e) {
			// ignore
		}
		return "unknown";
	}

	protected AtomicReference<ServiceDiscovery<ZookeeperInstance>> getServiceDiscoveryRef() {
		return this.serviceDiscovery;
	}

	protected AtomicReference<ServiceInstance<ZookeeperInstance>> getServiceInstanceRef() {
		return this.serviceInstance;
	}

	protected AtomicBoolean getBuilt() {
		return this.built;
	}

	protected CuratorFramework getCurator() {
		return curator;
	}
}
