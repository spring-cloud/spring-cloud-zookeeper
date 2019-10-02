/*
 * Copyright 2019-2019 the original author or authors.
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

package org.springframework.cloud.zookeeper.discovery.reactive;

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceInstance;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;

/**
 * Zookeeper version of {@link ReactiveDiscoveryClient}. Capable of resolving aliases from
 * {@link org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies} to service names in Zookeeper.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
public class ZookeeperReactiveDiscoveryClient implements ReactiveDiscoveryClient {

	private static final Logger logger = LoggerFactory.getLogger(ZookeeperReactiveDiscoveryClient.class);

	private final ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

	private final ZookeeperDependencies zookeeperDependencies;

	private final ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;

	public ZookeeperReactiveDiscoveryClient(ServiceDiscovery<ZookeeperInstance> serviceDiscovery,
			ZookeeperDependencies zookeeperDependencies, ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
		this.serviceDiscovery = serviceDiscovery;
		this.zookeeperDependencies = zookeeperDependencies;
		this.zookeeperDiscoveryProperties = zookeeperDiscoveryProperties;
	}

	@Override
	public String description() {
		return "Spring Cloud Zookeeper Reactive Discovery Client";
	}

	@Override
	public Flux<ServiceInstance> getInstances(String serviceId) {
		String serviceIdToQuery = serviceIdToQuery(serviceId);
		return Mono.justOrEmpty(serviceIdToQuery)
				.flatMapMany(getInstancesFromZookeeper())
				.subscribeOn(Schedulers.boundedElastic())
				.map(zkInstance -> toZookeeperServiceInstance(serviceIdToQuery, zkInstance));
	}

	private Function<String, Publisher<org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance>>> getInstancesFromZookeeper() {
		return service -> {
			try {
				return Flux.fromIterable(serviceDiscovery.queryForInstances(service));
			}
			catch (Exception e) {
				logger.error("Error getting instances from zookeeper. Possibly, no service has registered.", e);
				return Flux.empty();
			}
		};
	}

	private ZookeeperServiceInstance toZookeeperServiceInstance(String serviceId,
			org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> zkInstanceServiceInstance) {
		return new ZookeeperServiceInstance(serviceId, zkInstanceServiceInstance);
	}

	@Override
	public Flux<String> getServices() {
		return Flux.defer(getServicesFromZookeeper())
				.subscribeOn(Schedulers.boundedElastic());
	}

	private Supplier<Publisher<String>> getServicesFromZookeeper() {
		return () -> {
					try {
						return Flux.fromIterable(serviceDiscovery.queryForNames());
					}
					catch (Exception e) {
						logger.error("Error getting services from zookeeper. Possibly, no service has registered.", e);
						return Flux.empty();
					}
				};
	}

	private String serviceIdToQuery(String serviceId) {
		if (zookeeperDependencies != null
				&& zookeeperDependencies.hasDependencies()) {
			String pathForAlias = zookeeperDependencies.getPathForAlias(serviceId);
			return pathForAlias.isEmpty() ? serviceId : pathForAlias;
		}
		return serviceId;
	}

	@Override
	public int getOrder() {
		return zookeeperDiscoveryProperties.getOrder();
	}
}
