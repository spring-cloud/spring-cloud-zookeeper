package org.springframework.cloud.zookeeper.discovery;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.loadbalancer.cache.LoadBalancerCacheManager;
import org.springframework.cloud.loadbalancer.core.CachingServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.DiscoveryClientServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Olga Maciaszek-Sharma
 */
@Configuration(proxyBeanMethods = false)
public class ZookeeperLoadBalancerConfiguration {

	@Bean
	@ConditionalOnBean(DiscoveryClient.class)
	@ConditionalOnMissingBean
	public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(
			DiscoveryClient discoveryClient, Environment env,
			ApplicationContext context,
			ZookeeperDependencies zookeeperDependencies) {
		DiscoveryClientServiceInstanceListSupplier firstDelegate = new DiscoveryClientServiceInstanceListSupplier(
				discoveryClient, env);
		ZookeeperServiceInstanceListSupplier secondDelegate = new ZookeeperServiceInstanceListSupplier(firstDelegate,
				zookeeperDependencies);
		ObjectProvider<LoadBalancerCacheManager> cacheManagerProvider = context
				.getBeanProvider(LoadBalancerCacheManager.class);
		if (cacheManagerProvider.getIfAvailable() != null) {
			return new CachingServiceInstanceListSupplier(secondDelegate,
					cacheManagerProvider.getIfAvailable());
		}
		return secondDelegate;
	}

}
