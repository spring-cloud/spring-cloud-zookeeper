package org.springframework.cloud.zookeeper.discovery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author wmz7year
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = {"pring.application.name=testZookeeperDiscovery",
		"spring.cloud.zookeeper.discovery.preferIpAddress=true",
		"spring.cloud.zookeeper.discovery.instanceIpAddress=1.1.1.1"},
		classes = ZookeeperDiscoveryPropertiesTests.Config.class,
		webEnvironment = WebEnvironment.NONE)
public class ZookeeperDiscoveryPropertiesTests {

	@Autowired
	private ZookeeperDiscoveryProperties discoveryProperties;

	@Test
	public void testPreferIpAddress() {
		assertEquals("1.1.1.1", discoveryProperties.getInstanceHost());
        }

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	@EnableDiscoveryClient
	static class Config {

	}
}
