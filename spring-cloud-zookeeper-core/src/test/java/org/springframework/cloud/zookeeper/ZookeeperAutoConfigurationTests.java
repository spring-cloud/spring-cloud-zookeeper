package org.springframework.cloud.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ZookeeperAutoConfigurationTests.TestConfig.class, ZookeeperAutoConfiguration.class })
public class ZookeeperAutoConfigurationTests {

	@Autowired(required = false) CuratorFramework curator;
	
	@Test
	public void should_successfully_inject_Curator_as_a_Spring_bean() {
		assertNotNull(this.curator);
	}

	static class TestConfig {
		@Bean
		ZookeeperProperties zookeeperProperties(TestingServer testingServer) throws Exception {
			ZookeeperProperties properties = new ZookeeperProperties();
			properties.setConnectString(testingServer.getConnectString());
			return properties;
		}

		@Bean(destroyMethod = "close") TestingServer testingServer() throws Exception {
			return new TestingServer();
		}
	}
}
