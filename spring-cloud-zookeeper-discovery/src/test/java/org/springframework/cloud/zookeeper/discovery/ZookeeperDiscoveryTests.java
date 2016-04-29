package org.springframework.cloud.zookeeper.discovery;

import java.util.List;
import java.util.concurrent.Callable;

import com.jayway.awaitility.Awaitility;
import com.toomuchcoding.jsonassert.JsonAssertion;
import com.toomuchcoding.jsonassert.JsonPath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ZookeeperDiscoveryTests.Config.class)
@ActiveProfiles("ribbon")
@WebIntegrationTest(randomPort = true)
public class ZookeeperDiscoveryTests {

	@Autowired TestRibbonClient testRibbonClient;
	@Autowired DiscoveryClient discoveryClient;
	@Autowired ZookeeperServiceDiscovery serviceDiscovery;
	@Value("${spring.application.name}") String springAppName;
	@Autowired IdUsingFeignClient idUsingFeignClient;

	@Test public void should_find_the_app_by_its_name_via_Ribbon() {
		//expect:
		then(registeredServiceStatusViaServiceName()).isEqualTo("UP");
	}

	@Test public void should_find_a_collaborator_via_discovery_client() {
		//given:
		List<ServiceInstance> instances = this.discoveryClient.getInstances(this.springAppName);
		ServiceInstance instance = instances.get(0);
		//expect:
		then(registeredServiceStatus(instance)).isEqualTo("UP");
		then(instance.getMetadata().get("testMetadataKey")).isEqualTo("testMetadataValue");
	}

	@Test public void should_present_application_name_as_id_of_the_service_instance() {
		//given:
		ServiceInstance instance = this.discoveryClient.getLocalServiceInstance();
		//expect:
		then(this.springAppName).isEqualTo(instance.getServiceId());
	}

	@Test public void should_find_an_instance_using_feign_via_service_id() {
		final IdUsingFeignClient idUsingFeignClient = this.idUsingFeignClient;
		//expect:
		Awaitility.await().until(new Callable<Boolean>() {
			@Override public Boolean call() throws Exception {
				then(idUsingFeignClient.getBeans()).isNotEmpty();
				return true;
			}
		});
	}

	private String registeredServiceStatusViaServiceName() {
		return JsonPath.builder(this.testRibbonClient.thisHealthCheck()).field("status").read(String.class);
	}

	private String registeredServiceStatus(ServiceInstance instance) {
		return JsonPath.builder(this.testRibbonClient.callOnUrl(instance.getHost()+":"+instance.getPort(), "health")).field("status").read(String.class);
	}

	@Test public void should_properly_find_local_instance() {
		//expect:
		then(this.serviceDiscovery.getServiceInstance().getAddress()).isEqualTo(this.discoveryClient.getLocalServiceInstance().getHost());
	}
	
	
	@FeignClient("ribbonApp")
	public static interface IdUsingFeignClient {
		@RequestMapping(method = RequestMethod.GET, value = "/beans")
		String getBeans();
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	@EnableDiscoveryClient 
	@EnableFeignClients(clients = { IdUsingFeignClient.class })
	@Profile("ribbon")
	static class Config {

		@Bean TestRibbonClient testRibbonClient(@LoadBalanced RestTemplate restTemplate,
				@Value("${spring.application.name}") String springAppName) {
			return new TestRibbonClient(restTemplate, springAppName);
		}
	}

	@Controller
	@Profile("ribbon")
	class PingController {

		@RequestMapping("/ping") String ping() {
			return "pong";
		}
	}
}
