package org.springframework.cloud.zookeeper.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

/**
 * Properties used for configuring the Zookeeper implementation of
 * {@link org.springframework.cloud.client.discovery.DiscoveryClient}
 * 
 * @author Olga Maciaszek-Sharma
 */
@ConfigurationProperties("spring.cloud.discovery.client.zookeeper")
public class ZookeeperDiscoveryClientProperties {

	private int order = Ordered.LOWEST_PRECEDENCE;

	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
