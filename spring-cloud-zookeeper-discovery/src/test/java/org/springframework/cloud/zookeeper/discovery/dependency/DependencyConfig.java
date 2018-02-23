package org.springframework.cloud.zookeeper.discovery.dependency;

import java.util.Collection;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.BDDAssertions.then;

@Configuration
@EnableAutoConfiguration
@Import(CommonTestConfig.class)
@EnableFeignClients(basePackageClasses = {AliasUsingFeignClient.class, IdUsingFeignClient.class})
public class DependencyConfig {

	@Bean
	TestRibbonClient testRibbonClient(@LoadBalanced RestTemplate restTemplate) {
		return new TestRibbonClient(restTemplate);
	}

	@Bean
	PingController pingController() {
		return new PingController(portListener());
	}

	@Bean
	PortListener portListener() {
		return new PortListener();
	}

}

class PortListener implements ApplicationListener<WebServerInitializedEvent> {

	private int port;

	@Override
	public void onApplicationEvent(WebServerInitializedEvent event) {
		this.port = event.getWebServer().getPort();
	}

	public int getPort() {
		return this.port;
	}

}

@FeignClient("someAlias")
interface AliasUsingFeignClient {
	@RequestMapping(method = RequestMethod.GET, value = "/application/beans")
	String getBeans();

	@RequestMapping(method = RequestMethod.GET, value = "/checkHeaders")
	String checkHeaders();
}

@FeignClient("nameWithoutAlias")
interface IdUsingFeignClient {
	@RequestMapping(method = RequestMethod.GET, value = "/application/beans")
	String getBeans();
}

@RestController
class PingController {

	private final PortListener portListener;

	PingController(PortListener portListener) {
		this.portListener = portListener;
	}

	@RequestMapping("/ping") String ping() {
		return "pong";
	}

	@RequestMapping("/port") Integer port() {
		return this.portListener.getPort();
	}

	@RequestMapping("/checkHeaders") String checkHeaders(@RequestHeader("Content-Type") String contentType,
														 @RequestHeader("header1")
														 Collection<String> header1,
														 @RequestHeader("header2") Collection<String> header2) {
		then(contentType).isEqualTo("application/vnd.newsletter.v1+json");
		then(header1).containsExactly("value1");
		then(header2).containsExactly("value2");
		return "ok";
	}
}
