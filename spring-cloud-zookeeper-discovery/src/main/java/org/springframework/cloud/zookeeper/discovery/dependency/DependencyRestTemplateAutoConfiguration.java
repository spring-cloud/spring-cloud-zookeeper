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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.zookeeper.discovery.ConditionalOnRibbonZookeeper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 *
 * Customizes RestTemplate to support passing of params from dependency
 *
 * @author Marcin Grzejszczak, 4financeIT
 */
@AutoConfigureAfter(DependencyRibbonAutoConfiguration.class)
@ConditionalOnRibbonZookeeper
@Configuration
@ConditionalOnDependenciesPassed
@ConditionalOnProperty(value = "spring.cloud.zookeeper.dependencies.resttemplate.enabled", matchIfMissing = true)
public class DependencyRestTemplateAutoConfiguration {

	@Autowired @LoadBalanced RestTemplate restTemplate;
	@Autowired ZookeeperDependencies zookeeperDependencies;

	@PostConstruct
	void customizeRestTemplate() {
		this.restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
				String clientName = request.getURI().getHost();
				ZookeeperDependency dependencyForAlias = DependencyRestTemplateAutoConfiguration.this.zookeeperDependencies.getDependencyForAlias(clientName);
				HttpHeaders headers = getUpdatedHeadersIfPossible(request, dependencyForAlias);
				request.getHeaders().putAll(headers);
				return execution.execute(request, body);
			}

			private HttpHeaders getUpdatedHeadersIfPossible(HttpRequest request, ZookeeperDependency dependencyForAlias) {
				HttpHeaders httpHeaders = new HttpHeaders();
				if (dependencyForAlias != null) {
					Map<String, Collection<String>> updatedHeaders = dependencyForAlias.getUpdatedHeaders(convertHeadersFromListToCollection(request.getHeaders()));
					httpHeaders.putAll(convertHeadersFromCollectionToList(updatedHeaders));
					return httpHeaders;
				}
				httpHeaders.putAll(request.getHeaders());
				return httpHeaders;
			}

			private Map<String, Collection<String>> convertHeadersFromListToCollection(HttpHeaders headers) {
				return Maps.transformValues(headers, new Function<List<String>, Collection<String>>() {
					@Override
					public Collection<String> apply(List<String> input) {
						return input;
					}
				});
			}

			private Map<String, List<String>> convertHeadersFromCollectionToList(Map<String, Collection<String>>  headers) {
				return Maps.transformValues(headers, new Function<Collection<String>, List<String>>() {
					@Override
					public List<String> apply(Collection<String> input) {
						return (List<String>) input;
					}
				});
			}
		});
	}

}
