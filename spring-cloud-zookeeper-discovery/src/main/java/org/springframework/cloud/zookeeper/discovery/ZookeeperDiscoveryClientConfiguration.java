package org.springframework.cloud.zookeeper.discovery;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnProperty(value = "zookeeper.discovery.enabled", matchIfMissing = true)
@EnableConfigurationProperties
public class ZookeeperDiscoveryClientConfiguration {

	@Autowired(required = false)
	private ZookeeperDependencies zookeeperDependencies;

	@Autowired
	private CuratorFramework curator;

	@Bean
	public ZookeeperDiscoveryProperties zookeeperDiscoveryProperties() {
		return new ZookeeperDiscoveryProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public ZookeeperServiceDiscovery zookeeperServiceDiscovery() {
		return new ZookeeperServiceDiscovery(curator, zookeeperDiscoveryProperties(),
				instanceSerializer());
	}

	@Bean
	public ZookeeperLifecycle zookeeperLifecycle() {
		return new ZookeeperLifecycle(zookeeperDiscoveryProperties(), zookeeperServiceDiscovery());
	}

	@Bean
	public ZookeeperDiscoveryClient zookeeperDiscoveryClient() {
		return new ZookeeperDiscoveryClient(zookeeperServiceDiscovery(), zookeeperDependencies);
	}

	@Bean
	public InstanceSerializer<ZookeeperInstance> instanceSerializer() {
		return new JsonInstanceSerializer<>(ZookeeperInstance.class);
	}

	@Bean
	public ZookeeperDiscoveryHealthIndicator zookeeperDiscoveryHealthIndicator() {
		return new ZookeeperDiscoveryHealthIndicator(zookeeperServiceDiscovery());
	}

	@Bean
	public ZookeeperServiceWatch zookeeperServiceWatch() {
		return new ZookeeperServiceWatch(curator, zookeeperDiscoveryProperties());
	}

}
