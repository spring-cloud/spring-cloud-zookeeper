package org.springframework.cloud.zookeeper.discovery.test;

import org.apache.curator.test.TestingServer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CommonTestConfig {

	@Bean
	@LoadBalanced RestTemplate loadBalancedRestTemplate() {
		return new RestTemplate();
	}

	@Bean(destroyMethod = "close") TestingServer testingServer() throws Exception {
		return new TestingServer(SocketUtils.findAvailableTcpPort());
	}

	@Bean ZookeeperProperties zookeeperProperties(TestingServer testingServer) throws Exception {
		ZookeeperProperties zookeeperProperties = new ZookeeperProperties();
		zookeeperProperties.setConnectString("localhost:" + testingServer.getPort());
		return zookeeperProperties;
	}
}
