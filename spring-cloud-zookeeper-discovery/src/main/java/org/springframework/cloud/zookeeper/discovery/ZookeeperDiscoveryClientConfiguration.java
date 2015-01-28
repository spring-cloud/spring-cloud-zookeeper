package org.springframework.cloud.zookeeper.discovery;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * @author Spencer Gibb
 */
@Configuration
@EnableConfigurationProperties
public class ZookeeperDiscoveryClientConfiguration {
	@Autowired
	ApplicationContext context;

	@Bean
	public ZookeeperDiscoveryProperties zookeeperDiscoveryProperties() {
		return new ZookeeperDiscoveryProperties();
	}

	@Bean
	public ZookeeperLifecycle zookeeperLifecycle() {
		return new ZookeeperLifecycle();
	}

	@Bean
	public ZookeeperDiscoveryClient zookeeperDiscoveryClient() {
		return new ZookeeperDiscoveryClient();
	}

	@Bean
	public ServiceInstance<ZookeeperInstance> serviceInstance() throws Exception {
		Environment environment = context.getEnvironment();
		Integer port = new Integer(environment.getProperty("server.port", "8080"));
		UriSpec uriSpec = new UriSpec(zookeeperDiscoveryProperties().getUriSpec());
		return ServiceInstance.<ZookeeperInstance> builder()
				.name(environment.getProperty("spring.application.name"))
				.payload(new ZookeeperInstance(context.getId())).port(port)
				.uriSpec(uriSpec).build();
	}

	@Bean
	public InstanceSerializer<ZookeeperInstance> instanceSerializer() {
		return new JsonInstanceSerializer<>(ZookeeperInstance.class);
	}

	@Bean
	public ServiceDiscovery<ZookeeperInstance> serviceDiscovery(CuratorFramework curator)
			throws Exception {
		return ServiceDiscoveryBuilder.builder(ZookeeperInstance.class).client(curator)
				.basePath(zookeeperDiscoveryProperties().getRoot())
				.serializer(instanceSerializer()).thisInstance(serviceInstance()).build();
	}

	@Bean
	public ZookeeperDiscoveryHealthIndicator zookeeperDiscoveryHealthIndicator() {
		return new ZookeeperDiscoveryHealthIndicator();
	}
}
