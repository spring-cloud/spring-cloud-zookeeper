/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.zookeeper.ConditionalOnZookeeperEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import feign.Client;
import feign.Request;
import feign.Response;

/**
 * Configuration for ensuring that headers are set for a given dependency when
 * Feign is used.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
@Configuration
@ConditionalOnDependenciesPassed
@ConditionalOnZookeeperEnabled
@ConditionalOnProperty(value = "spring.cloud.zookeeper.dependency.headers.enabled", matchIfMissing = true)
@ConditionalOnClass({ Client.class, LoadBalancerFeignClient.class })
@AutoConfigureAfter({ RibbonAutoConfiguration.class, FeignRibbonClientAutoConfiguration.class })
public class DependencyFeignClientAutoConfiguration {
	@Autowired(required = false) private LoadBalancerFeignClient ribbonClient;
	@Autowired private ZookeeperDependencies zookeeperDependencies;
	@Autowired private CachingSpringLoadBalancerFactory loadBalancerFactory;
	@Autowired private SpringClientFactory springClientFactory;

	@Bean
	@Primary
	Client dependencyBasedFeignClient() {
		return new LoadBalancerFeignClient(
				new Client.Default(null, null), this.loadBalancerFactory, this.springClientFactory) {

			@Override
			public Response execute(Request request, Request.Options options)
					throws IOException {
				URI asUri = URI.create(request.url());
				String clientName = asUri.getHost();
				ZookeeperDependency dependencyForAlias =
						DependencyFeignClientAutoConfiguration.this.zookeeperDependencies
						.getDependencyForAlias(clientName);
				Map<String, Collection<String>> headers = getUpdatedHeadersIfPossible(
						request, dependencyForAlias);
				if (DependencyFeignClientAutoConfiguration.this.ribbonClient != null) {
					return DependencyFeignClientAutoConfiguration.this.ribbonClient.execute(
							request(request, headers), options);
				}
				return super.execute(request(request, headers), options);
			}

			private Request request(Request request,
					Map<String, Collection<String>> headers) {
				return Request.create(request.method(), request.url(), headers,
						request.body(), request.charset());
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
