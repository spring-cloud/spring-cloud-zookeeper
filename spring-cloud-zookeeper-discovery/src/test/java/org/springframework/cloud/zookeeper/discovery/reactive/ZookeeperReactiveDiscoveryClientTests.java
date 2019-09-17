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

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Tim Ysewyn
 */
@ExtendWith(MockitoExtension.class)
class ZookeeperReactiveDiscoveryClientTests {

	@Mock
	private ServiceDiscovery<ZookeeperInstance> zkClient;

	@Mock
	private ZookeeperDependencies zookeeperDependencies;

	@Mock
	private ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;

	@Mock
	private org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> serviceInstance;

	@InjectMocks
	private ZookeeperReactiveDiscoveryClient client;

	@Test
	public void verifyDefaults() {
		when(zookeeperDiscoveryProperties.getOrder()).thenReturn(1);
		assertThat(client.description()).isEqualTo("Spring Cloud Zookeeper Reactive Discovery Client");
		assertThat(client.getOrder()).isEqualTo(1);
	}

	@Test
	public void shouldReturnFluxOfServices() throws Exception {
		when(zkClient.queryForNames()).thenReturn(singletonList("my-service"));
		Flux<String> services = this.client.getServices();
		StepVerifier.create(services).expectNext("my-service").expectComplete().verify();
	}

	@Test
	public void shouldReturnEmptyFluxOfServicesWhenZookeeperFails() throws Exception {
		when(zkClient.queryForNames()).thenThrow(new RuntimeException());
		Flux<String> services = this.client.getServices();
		StepVerifier.create(services).expectNextCount(0).expectComplete().verify();
	}

	@Test
	public void shouldReturnEmptyFluxForNonExistingService() {
		Flux<ServiceInstance> services = this.client.getInstances("nonexistent-service");
		StepVerifier.create(services).expectNextCount(0).expectComplete().verify();
	}

	@Test
	public void shouldReturnEmptyFluxWhenZookeeperFails() throws Exception {
		when(zkClient.queryForInstances("existing-service")).thenThrow(new RuntimeException());
		Flux<ServiceInstance> services = this.client.getInstances("existing-service");
		StepVerifier.create(services).expectNextCount(0).expectComplete().verify();
	}

	@Test
	public void shouldReturnFluxOfServiceInstances() throws Exception {
		configureServiceInstance();
		when(zookeeperDependencies.hasDependencies()).thenReturn(false);
		when(zkClient.queryForInstances("existing-service")).thenReturn(singletonList(serviceInstance));
		Flux<ServiceInstance> services = this.client.getInstances("existing-service");
		StepVerifier.create(services).expectNextCount(1).expectComplete().verify();
	}

	@Test
	public void shouldReturnFluxOfServiceInstancesWhenNotHavingPathForAlias() throws Exception {
		configureServiceInstance();
		when(zookeeperDependencies.hasDependencies()).thenReturn(true);
		when(zookeeperDependencies.getPathForAlias("existing-service")).thenReturn("");
		when(zkClient.queryForInstances("existing-service")).thenReturn(singletonList(serviceInstance));
		Flux<ServiceInstance> services = this.client.getInstances("existing-service");
		StepVerifier.create(services).expectNextCount(1).expectComplete().verify();
	}

	@Test
	public void shouldReturnFluxOfServiceInstancesWhenHavingPathForAlias() throws Exception {
		configureServiceInstance();
		when(zookeeperDependencies.hasDependencies()).thenReturn(true);
		when(zookeeperDependencies.getPathForAlias("existing-service")).thenReturn("path-for-existing-service");
		when(zkClient.queryForInstances("path-for-existing-service")).thenReturn(singletonList(serviceInstance));
		Flux<ServiceInstance> services = this.client.getInstances("existing-service");
		StepVerifier.create(services).expectNextCount(1).expectComplete().verify();
	}

	private void configureServiceInstance() {
		when(serviceInstance.getAddress()).thenReturn("http://localhost");
		when(serviceInstance.getPort()).thenReturn(80);
		when(serviceInstance.buildUriSpec()).thenReturn("http://localhost:80");
	}

}
