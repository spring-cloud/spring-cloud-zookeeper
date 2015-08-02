/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.zookeeper.discovery.dependency;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.zookeeper.discovery.ConditionalOnRibbonZookeeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Marcin Grzejszczak, 4financeIT
 */
@AutoConfigureBefore(RibbonAutoConfiguration.class)
@ConditionalOnRibbonZookeeper
@Configuration
public class ZookeeperDependencyRibbonLoadBalancerClientConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnDependenciesPassed
	public LoadBalancerClient loadBalancerClient(SpringClientFactory springClientFactory) {
		return new RibbonLoadBalancerClient(springClientFactory) {
			@Override
			protected Server getServer(String serviceId) {
				ILoadBalancer loadBalancer = this.getLoadBalancer(serviceId);
				return loadBalancer == null ? null : chooseServerByServiceIdOrDefault(loadBalancer, serviceId);
			}

			private Server chooseServerByServiceIdOrDefault(ILoadBalancer loadBalancer, String serviceId) {
				Server server = loadBalancer.chooseServer(serviceId);
				return server != null ? server : loadBalancer.chooseServer("default");
			}
		};
	}

}
