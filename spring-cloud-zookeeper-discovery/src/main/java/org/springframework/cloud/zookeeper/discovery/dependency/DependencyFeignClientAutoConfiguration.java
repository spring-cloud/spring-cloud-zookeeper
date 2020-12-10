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

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import feign.Client;
import feign.Request;
import feign.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.loadbalancer.FeignLoadBalancerAutoConfiguration;
import org.springframework.cloud.zookeeper.ConditionalOnZookeeperEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for ensuring that headers are set for a given dependency when Feign is
 * used.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDependenciesPassed
@ConditionalOnZookeeperEnabled
@ConditionalOnProperty(value = "spring.cloud.zookeeper.dependency.headers.enabled", matchIfMissing = true)
@ConditionalOnClass({Client.class, FeignBlockingLoadBalancerClient.class})
@ConditionalOnBean(BlockingLoadBalancerClient.class)
@AutoConfigureAfter({FeignLoadBalancerAutoConfiguration.class, BlockingLoadBalancerClientAutoConfiguration.class})
public class DependencyFeignClientAutoConfiguration {

	private final FeignBlockingLoadBalancerClient feignLoadBalancerClient;

	private final ZookeeperDependencies zookeeperDependencies;

	private final BlockingLoadBalancerClient loadBalancerClient;

	private final LoadBalancerProperties loadBalancerProperties;

	private final LoadBalancerClientFactory loadBalancerClientFactory;

	public DependencyFeignClientAutoConfiguration(@Autowired(required = false) FeignBlockingLoadBalancerClient feignLoadBalancerClient,
			ZookeeperDependencies zookeeperDependencies, BlockingLoadBalancerClient loadBalancerClient, LoadBalancerProperties loadBalancerProperties, LoadBalancerClientFactory loadBalancerClientFactory) {
		this.feignLoadBalancerClient = feignLoadBalancerClient;
		this.zookeeperDependencies = zookeeperDependencies;
		this.loadBalancerClient = loadBalancerClient;
		this.loadBalancerProperties = loadBalancerProperties;
		this.loadBalancerClientFactory = loadBalancerClientFactory;
	}

	@Bean
	@Primary
	Client dependencyBasedFeignClient() {
		return new FeignBlockingLoadBalancerClient(new Client.Default(null, null),
				loadBalancerClient, loadBalancerProperties, loadBalancerClientFactory) {

			@Override
			public Response execute(Request request, Request.Options options)
					throws IOException {
				URI asUri = URI.create(request.url());
				String clientName = asUri.getHost();
				ZookeeperDependency dependencyForAlias = DependencyFeignClientAutoConfiguration.this.zookeeperDependencies
						.getDependencyForAlias(clientName);
				Map<String, Collection<String>> headers = getUpdatedHeadersIfPossible(
						request, dependencyForAlias);
				if (DependencyFeignClientAutoConfiguration.this.feignLoadBalancerClient != null) {
					return DependencyFeignClientAutoConfiguration.this.feignLoadBalancerClient
							.execute(request(request, headers), options);
				}
				return super.execute(request(request, headers), options);
			}

			private Request request(Request request,
					Map<String, Collection<String>> headers) {
				return Request.create(request.httpMethod(), request.url(), headers,
						request.body(), request.charset(), request.requestTemplate());
			}

			private Map<String, Collection<String>> getUpdatedHeadersIfPossible(
					Request request, ZookeeperDependency dependencyForAlias) {
				if (dependencyForAlias != null) {
					return Collections.unmodifiableMap(new HashMap<>(
							dependencyForAlias.getUpdatedHeaders(request.headers())));
				}
				return request.headers();
			}

		};
	}

}
