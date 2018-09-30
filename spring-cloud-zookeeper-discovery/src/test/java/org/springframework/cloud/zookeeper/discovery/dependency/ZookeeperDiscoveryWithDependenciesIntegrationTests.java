/*
 * Copyright 2016-2018 the original author or authors.
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

import java.util.List;

import com.jayway.awaitility.Awaitility;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZookeeperDiscoveryWithDependenciesIntegrationTests.Config.class,
		properties = {"feign.hystrix.enabled=false", "debug=true", "management.endpoints.web.exposure.include=*"},
	webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dependencies")
public class ZookeeperDiscoveryWithDependenciesIntegrationTests {

	@Autowired
	TestRibbonClient testRibbonClient;

	@Autowired
	DiscoveryClient discoveryClient;

	@Autowired
	AliasUsingFeignClient aliasUsingFeignClient;

	@Autowired
	IdUsingFeignClient idUsingFeignClient;

	@Autowired
	ZookeeperDependencies zookeeperDependencies;

	@Test
	public void should_find_an_instance_via_path_when_alias_is_not_found() {
		// given:
		final DiscoveryClient discoveryClient = this.discoveryClient;
		// expect:
		Awaitility.await().until(() -> !discoveryClient.getInstances("nameWithoutAlias").isEmpty());
	}

	@Test
	public void should_fill_out_properly_the_stub_section_of_a_dependency() {
		// given:
		StubsConfiguration stubsConfiguration = this.zookeeperDependencies.getDependencies().get("someAlias").getStubsConfiguration();
		// expect:
		assertThat(stubsConfiguration.getStubsGroupId()).isEqualTo("org.springframework");
		assertThat(stubsConfiguration.getStubsArtifactId()).isEqualTo("foo");
		assertThat(stubsConfiguration.getStubsClassifier()).isEqualTo("stubs");
	}

	@Ignore //FIXME 2.0.0
	@Test
	public void should_find_an_instance_using_feign_via_serviceID_when_alias_is_not_found() {
		// given:
		final IdUsingFeignClient idUsingFeignClient = this.idUsingFeignClient;
		// expect:
		Awaitility.await().until(() -> {
			assertThat(idUsingFeignClient.getBeans()).isNotEmpty();
			return true;
		});
	}

	@Ignore //FIXME 2.0.0
	@Test
	public void should_find_a_collaborator_via_load_balanced_rest_template_by_using_its_alias_from_dependencies() {
		// expect:
		Awaitility.await().until(() -> callingServiceAtBeansEndpointIsNotEmpty());
	}

	@Ignore //FIXME 2.0.0
	@Test
	public void should_find_a_collaborator_using_feign_by_using_its_alias_from_dependencies() {
		// given:
		final AliasUsingFeignClient aliasUsingFeignClient = this.aliasUsingFeignClient;
		// expect:
		Awaitility.await().until(() -> {
			assertThat(aliasUsingFeignClient.getBeans()).isNotEmpty();
			return true;
		});
	}

	@Test
	public void should_have_headers_from_dependencies_attached_to_the_request_via_load_balanced_rest_template() {
		// expect:
		Awaitility.await().until(() -> {
			callingServiceToCheckIfHeadersArePassed();
			return true;
		});
	}

	@Ignore //FIXME 2.0.0
	@Test
	public void should_have_headers_from_dependencies_attached_to_the_request_via_feign() {
		// given:
		final AliasUsingFeignClient aliasUsingFeignClient = this.aliasUsingFeignClient;
		// expect:
		Awaitility.await().until(() -> {
			aliasUsingFeignClient.checkHeaders();
			return true;
		});
	}

	@Test
	public void should_find_a_collaborator_via_discovery_client() {
		// // given:
		final DiscoveryClient discoveryClient = this.discoveryClient;
		List<ServiceInstance> instances = discoveryClient.getInstances("someAlias");
		final ServiceInstance instance = instances.get(0);
		// expect:
		Awaitility.await().until(() -> callingServiceViaUrlOnBeansEndpointIsNotEmpty(instance));
	}

	@Test
	public void should_have_path_equal_to_prefixed_alias() {
		// given:
		ZookeeperDependency dependency = this.zookeeperDependencies.getDependencyForAlias("aliasIsPath");
		// expect:
		assertThat(dependency.getPath()).isEqualTo("/aliasIsPath");
	}

	@Test
	public void should_have_prefixed_alias_equal_to_path() {
		// given:
		ZookeeperDependency dependency = this.zookeeperDependencies.getDependencyForPath("/aliasIsPath");
		// expect:
		assertThat(dependency.getPath()).isEqualTo("/aliasIsPath");
	}

	@Test
	public void should_have_path_set_via_string_constructor() {
		// given:
		ZookeeperDependency dependency = this.zookeeperDependencies.getDependencyForAlias("anotherAlias");
		// expect:
		assertThat(dependency.getPath()).isEqualTo("/myPath");
	}

	// #138
	@Test
	public void should_parse_dependency_with_path() {
		// given:
		StubsConfiguration someServiceStub = this.zookeeperDependencies.getDependencyForAlias("some-service").getStubsConfiguration();
		// expect:
		assertThat(someServiceStub.getStubsGroupId()).isEqualTo("io.company.department");
		assertThat(someServiceStub.getStubsArtifactId()).isEqualTo("some-service");
		assertThat(someServiceStub.getStubsClassifier()).isEqualTo("stubs");
	}

	private boolean callingServiceAtBeansEndpointIsNotEmpty() {
		return !this.testRibbonClient.callService("someAlias", TestRibbonClient.BASE_PATH + "/beans").isEmpty();
	}

	private boolean callingServiceViaUrlOnBeansEndpointIsNotEmpty(ServiceInstance instance) {
		return !this.testRibbonClient.callOnUrl(instance.getHost() + ":" + instance.getPort(), TestRibbonClient.BASE_PATH + "/beans").isEmpty();
	}

	private void callingServiceToCheckIfHeadersArePassed() {
		this.testRibbonClient.callService("someAlias", "checkHeaders");
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(DependencyConfig.class)
	@Profile("dependencies")
	static class Config {
	}
}
