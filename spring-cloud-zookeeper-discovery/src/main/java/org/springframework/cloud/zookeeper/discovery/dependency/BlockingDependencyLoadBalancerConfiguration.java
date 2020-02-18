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

package org.springframework.cloud.zookeeper.discovery.dependency;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Mono;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author Olga Maciaszek-Sharma
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RestTemplate.class)
@ConditionalOnBean(LoadBalancerClientFactory.class)
public class BlockingDependencyLoadBalancerConfiguration {

	private final LoadBalancerClientFactory loadBalancerClientFactory;

	public BlockingDependencyLoadBalancerConfiguration(LoadBalancerClientFactory loadBalancerClientFactory) {
		this.loadBalancerClientFactory = loadBalancerClientFactory;
	}

	@Bean
	@ConditionalOnMissingBean
	BlockingLoadBalancerClient zookeeperBlockingLoadBalancerClient() {
		return new BlockingLoadBalancerClient(loadBalancerClientFactory) {

			private final Log log = LogFactory
					.getLog(BlockingLoadBalancerClient.class
							+ "for Zookeeper");

			@Override
			public ServiceInstance choose(String serviceId) {
				ReactiveLoadBalancer<ServiceInstance> loadBalancer = loadBalancerClientFactory
						.getInstance(serviceId);
				if (loadBalancer != null) {
					log.debug(String.format(
							"Dependencies are set - will try to load-balance via LoadBalancer for key [%s]",
							serviceId));
					Response<ServiceInstance> loadBalancerResponse = Mono
							.from(loadBalancer.choose())
							.switchIfEmpty(Mono.from(chooseDefault())).block();
					return getServiceInstance(loadBalancerResponse);
				}
				return getServiceInstance(chooseDefault().block());

			}

			private ServiceInstance getServiceInstance(Response<ServiceInstance> loadBalancerResponse) {
				if (loadBalancerResponse == null) {
					return null;
				}
				return loadBalancerResponse.getServer();
			}

			private Mono<Response<ServiceInstance>> chooseDefault() {
				log.debug("Could not find instance via LoadBalancer for provided service id. "
						+ "Will try retrieving default instance");
				ReactiveLoadBalancer<ServiceInstance> defaultLoadBalancer = loadBalancerClientFactory
						.getInstance("default");
				if (defaultLoadBalancer == null) {
					return Mono.just(new EmptyResponse());
				}
				return Mono.from(defaultLoadBalancer.choose());
			}
		};
	}

}
