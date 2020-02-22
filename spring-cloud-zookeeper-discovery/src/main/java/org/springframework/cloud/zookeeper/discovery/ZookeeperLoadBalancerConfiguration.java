/*
 * Copyright 2015-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Zookeeper-specific {@link ServiceInstanceListSupplier} that provides a delegate that
 * filters available instances based on status retrieved from Zookeeper.
 *
 * @author Olga Maciaszek-Sharma
 * @since 3.0.0
 */
@Configuration(proxyBeanMethods = false)
public class ZookeeperLoadBalancerConfiguration {

	@Bean
	@ConditionalOnBean(DiscoveryClient.class)
	@ConditionalOnMissingBean
	public ServiceInstanceListSupplier zookeeperDiscoveryClientServiceInstanceListSupplier(
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
