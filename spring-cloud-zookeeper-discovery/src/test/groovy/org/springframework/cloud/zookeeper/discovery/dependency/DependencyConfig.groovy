package org.springframework.cloud.zookeeper.discovery.dependency
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.autoconfigure.EndpointMBeanExportAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.netflix.feign.EnableFeignClients
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceDiscovery
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@Configuration
@EnableAutoConfiguration(exclude = [EndpointMBeanExportAutoConfiguration])
@Import(CommonTestConfig)
@EnableDiscoveryClient
@EnableFeignClients(basePackageClasses = [AliasUsingFeignClient, IdUsingFeignClient])
class DependencyConfig {

	@Autowired ZookeeperServiceDiscovery zookeeperServiceDiscovery

	@Bean
	TestRibbonClient testRibbonClient(@LoadBalanced RestTemplate restTemplate) {
		return new TestRibbonClient(restTemplate)
	}

	@Bean
	PingController pingController() {
		return new PingController(portListener())
	}

	@Bean
	PortListener portListener() {
		return new PortListener()
	}

}

class PortListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

	private int port

	@Override
	public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
		this.port = event.getEmbeddedServletContainer().getPort()
	}

	public int getPort() {
		return port
	}

}

@FeignClient("someAlias")
interface AliasUsingFeignClient {
	@RequestMapping(method = RequestMethod.GET, value = "/beans")
	String getBeans()

	@RequestMapping(method = RequestMethod.GET, value = "/checkHeaders")
	String checkHeaders()
}

@FeignClient("nameWithoutAlias")
interface IdUsingFeignClient {
	@RequestMapping(method = RequestMethod.GET, value = "/beans")
	String getBeans()
}

@RestController
class PingController {

	private final PortListener portListener

	PingController(PortListener portListener) {
		this.portListener = portListener
	}

	@RequestMapping('/ping') String ping() {
		return 'pong'
	}

	@RequestMapping('/port') Integer port() {
		return portListener.port
	}

	@RequestMapping('/checkHeaders') String checkHeaders(@RequestHeader('Content-Type') String contentType,
														 @RequestHeader('header1') Collection<String> header1,
														 @RequestHeader('header2') Collection<String> header2) {
		assert  contentType == 'application/vnd.newsletter.v1+json'
		assert  header1 == ['value1'] as Set
		assert  header2 == ['value2'] as Set
		return 'ok'
	}
}
