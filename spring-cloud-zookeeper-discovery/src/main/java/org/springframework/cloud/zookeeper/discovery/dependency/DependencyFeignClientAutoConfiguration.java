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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import feign.Client;
import feign.Request;
import feign.Response;
import feign.ribbon.RibbonClient;

/**
 *
 * Configuration for ensuring that headers are set for a given dependency.
 *
 * @author Marcin Grzejszczak, 4financeIT
 */
@Configuration
@ConditionalOnDependenciesPassed
@ConditionalOnProperty(value = "spring.cloud.zookeeper.dependencies.headers.enabled", matchIfMissing = true)
@ConditionalOnClass({ Client.class, RibbonClient.class })
public class DependencyFeignClientAutoConfiguration {

	@Bean
	@Primary
	@SuppressWarnings("deprecation")
	Client dependencyBasedFeignClient(final LoadBalancerFeignClient ribbonClient,
			final ZookeeperDependencies zookeeperDependencies) {
		// TODO: remove dependency on feign-ribbon
		return new RibbonClient() {
			@Override
			public Response execute(Request request, Request.Options options)
					throws IOException {
				URI asUri = URI.create(request.url());
				String clientName = asUri.getHost();
				ZookeeperDependency dependencyForAlias = zookeeperDependencies
						.getDependencyForAlias(clientName);
				Map<String, Collection<String>> headers = getUpdatedHeadersIfPossible(
						request, dependencyForAlias);
				return ribbonClient.execute(Request.create(request.method(),
						request.url(), headers, request.body(), request.charset()),
						options);
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
