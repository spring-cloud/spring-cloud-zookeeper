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
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Olga Maciaszek-Sharma
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(WebClient.class)
@ConditionalOnBean(ReactiveLoadBalancer.Factory.class)
public class ReactiveDependencyLoadBalancerConfiguration {

	private final ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory;

	public ReactiveDependencyLoadBalancerConfiguration(ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory) {
		this.loadBalancerFactory = loadBalancerFactory;
	}

	@Bean
	@ConditionalOnMissingBean
	ReactorLoadBalancerExchangeFilterFunction zookeeperReactorLoadBalancerExchangeFilterFunction() {
		return new ReactorLoadBalancerExchangeFilterFunction(loadBalancerFactory) {

			private final Log log = LogFactory
					.getLog(ReactorLoadBalancerExchangeFilterFunction.class
							+ "for Zookeeper");

			@Override
			protected Mono<Response<ServiceInstance>> choose(String serviceId) {
				ReactiveLoadBalancer<ServiceInstance> loadBalancer = loadBalancerFactory
						.getInstance(serviceId);
				if (loadBalancer != null) {
					log.debug(String.format(
							"Dependencies are set - will try to load-balance via LoadBalancer for key [%s]",
							serviceId));
					return Mono.from(loadBalancer.choose())
							.flatMap(response -> {
								if (response instanceof EmptyResponse) {
									return Mono.defer(this::chooseDefault);
								}
								return Mono.defer(() -> Mono.just(response));
							})
							.switchIfEmpty(Mono.defer(this::chooseDefault));
				}
				return chooseDefault();
			}

			private Mono<Response<ServiceInstance>> chooseDefault() {
				log.debug("Could not find instance via LoadBalancer for provided service id. "
						+ "Will try retrieving default instance");
				ReactiveLoadBalancer<ServiceInstance> defaultLoadBalancer = loadBalancerFactory
						.getInstance("default");
				if (defaultLoadBalancer == null) {
					return Mono.defer(() -> Mono.just(new EmptyResponse()));
				}
				return Mono.from(defaultLoadBalancer.choose());
			}
		};

	}
}


