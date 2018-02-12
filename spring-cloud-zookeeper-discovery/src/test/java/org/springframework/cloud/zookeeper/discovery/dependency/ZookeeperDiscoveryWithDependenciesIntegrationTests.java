package org.springframework.cloud.zookeeper.discovery.dependency;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient.BASE_PATH;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZookeeperDiscoveryWithDependenciesIntegrationTests.Config.class,
		properties = {"feign.hystrix.enabled=false", "debug=true", "management.endpoints.web.exposure.include=*"},
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dependencies")
public class ZookeeperDiscoveryWithDependenciesIntegrationTests {

	@Autowired TestRibbonClient testRibbonClient;
	@Autowired DiscoveryClient discoveryClient;
	@Autowired AliasUsingFeignClient aliasUsingFeignClient;
	@Autowired IdUsingFeignClient idUsingFeignClient;
	@Autowired ZookeeperDependencies zookeeperDependencies;

	@Test public void should_find_an_instance_via_path_when_alias_is_not_found() {
		// given:
		final DiscoveryClient discoveryClient = this.discoveryClient;
		// expect:
		await().until(new Callable<Boolean>() {
			@Override public Boolean call() throws Exception {
				return !discoveryClient.getInstances("nameWithoutAlias").isEmpty();
			}
		});
	}

	@Test public void should_fill_out_properly_the_stub_section_of_a_dependency() {
		// given:
		StubsConfiguration stubsConfiguration = this.zookeeperDependencies.getDependencies().get("someAlias").getStubsConfiguration();
		// expect:
		then(stubsConfiguration.getStubsGroupId()).isEqualTo("org.springframework");
		then(stubsConfiguration.getStubsArtifactId()).isEqualTo("foo");
		then(stubsConfiguration.getStubsClassifier()).isEqualTo("stubs");
	}

	@Ignore //FIXME 2.0.0
	@Test public void should_find_an_instance_using_feign_via_serviceID_when_alias_is_not_found() {
		// given:
		final IdUsingFeignClient idUsingFeignClient = this.idUsingFeignClient;
		// expect:
		await().until(new Callable<Boolean>() {
			@Override public Boolean call() throws Exception {
				then(idUsingFeignClient.getBeans()).isNotEmpty();
				return true;
			}
		});
	}

	@Ignore //FIXME 2.0.0
	@Test public void should_find_a_collaborator_via_load_balanced_rest_template_by_using_its_alias_from_dependencies() {
		// expect:
		await().until(new Callable<Boolean>() {
			@Override public Boolean call() throws Exception {
				return callingServiceAtBeansEndpointIsNotEmpty();
			}
		});
	}

	@Ignore //FIXME 2.0.0
	@Test public void should_find_a_collaborator_using_feign_by_using_its_alias_from_dependencies() {
		// given:
		final AliasUsingFeignClient aliasUsingFeignClient = this.aliasUsingFeignClient;
		// expect:
		await().until(new Callable<Boolean>() {
			@Override public Boolean call() throws Exception {
				then(aliasUsingFeignClient.getBeans()).isNotEmpty();
				return true;
			}
		});
	}

	@Test public void should_have_headers_from_dependencies_attached_to_the_request_via_load_balanced_rest_template() {
		// expect:
		await().until(new Callable<Boolean>() {
			@Override public Boolean call() throws Exception {
				callingServiceToCheckIfHeadersArePassed();
				return true;
			}
		});
	}

	@Ignore //FIXME 2.0.0
	@Test public void should_have_headers_from_dependencies_attached_to_the_request_via_feign() {
		// given:
		final AliasUsingFeignClient aliasUsingFeignClient = this.aliasUsingFeignClient;
		// expect:
		await().until(new Callable<Boolean>() {
			@Override public Boolean call() throws Exception {
				aliasUsingFeignClient.checkHeaders();
				return true;
			}
		});
	}

	@Test public void should_find_a_collaborator_via_discovery_client() {
		// // given:
		final DiscoveryClient discoveryClient = this.discoveryClient;
		List<ServiceInstance> instances = discoveryClient.getInstances("someAlias");
		final ServiceInstance instance = instances.get(0);
		// expect:
		await().until(new Callable<Boolean>() {
			@Override public Boolean call() throws Exception {
				return callingServiceViaUrlOnBeansEndpointIsNotEmpty(instance);
			}
		});
	}

	@Test public void should_have_path_equal_to_prefixed_alias() {
		// given:
		ZookeeperDependency dependency = this.zookeeperDependencies.getDependencyForAlias("aliasIsPath");
		// expect:
		then(dependency.getPath()).isEqualTo("/aliasIsPath");
	}

	@Test public void should_have_prefixed_alias_equal_to_path() {
		// given:
		ZookeeperDependency dependency = this.zookeeperDependencies.getDependencyForPath("/aliasIsPath");
		// expect:
		then(dependency.getPath()).isEqualTo("/aliasIsPath");
	}

	@Test public void should_have_path_set_via_string_constructor() {
		// given:
		ZookeeperDependency dependency = this.zookeeperDependencies.getDependencyForAlias("anotherAlias");
		// expect:
		then(dependency.getPath()).isEqualTo("/myPath");
	}

	// #138
	@Test public void should_parse_dependency_with_path() {
		// given:
		StubsConfiguration someServiceStub = this.zookeeperDependencies.getDependencyForAlias("some-service").getStubsConfiguration();
		// expect:
		then(someServiceStub.getStubsGroupId()).isEqualTo("io.company.department");
		then(someServiceStub.getStubsArtifactId()).isEqualTo("some-service");
		then(someServiceStub.getStubsClassifier()).isEqualTo("stubs");
	}

	private boolean callingServiceAtBeansEndpointIsNotEmpty() {
		return !this.testRibbonClient.callService("someAlias", BASE_PATH + "/beans").isEmpty();
	}

	private boolean callingServiceViaUrlOnBeansEndpointIsNotEmpty(ServiceInstance instance) {
		return !this.testRibbonClient.callOnUrl(instance.getHost() + ":" + instance.getPort(), BASE_PATH + "/beans").isEmpty();
	}

	private void callingServiceToCheckIfHeadersArePassed() {
		this.testRibbonClient.callService("someAlias", "checkHeaders");
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(DependencyConfig.class)
	@Profile("dependencies")
	static class Config { }
}
