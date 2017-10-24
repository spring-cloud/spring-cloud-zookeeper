package org.springframework.cloud.zookeeper.discovery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wmz7year
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = {"pring.application.name=testZookeeperDiscovery",
		"spring.cloud.zookeeper.discovery.instance-id=zkpropstestid-123",
		"spring.cloud.zookeeper.discovery.preferIpAddress=true",
		"spring.cloud.zookeeper.discovery.instanceIpAddress=1.1.1.1"},
		classes = ZookeeperDiscoveryPropertiesTests.Config.class,
		webEnvironment = WebEnvironment.RANDOM_PORT)
public class ZookeeperDiscoveryPropertiesTests {

	@Autowired
	private ZookeeperDiscoveryProperties discoveryProperties;

	@Test
	public void testPreferIpAddress() {
		assertThat(this.discoveryProperties.getInstanceId()).isEqualTo("zkpropstestid-123");
		assertThat(this.discoveryProperties.getInstanceHost()).isEqualTo("1.1.1.1");
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	static class Config {

	}
}
