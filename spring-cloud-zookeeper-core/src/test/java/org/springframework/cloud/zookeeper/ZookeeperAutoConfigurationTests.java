package org.springframework.cloud.zookeeper;

import static org.junit.Assert.assertNotNull;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ZookeeperAutoConfigurationTests.TestConfig.class,
		ZookeeperAutoConfiguration.class })
public class ZookeeperAutoConfigurationTests {

	@Autowired(required = false)
	CuratorFramework curator;

	@Test
	public void testZookeeperFramework() {
		assertNotNull("curator is null", curator);
	}

	static class TestConfig {
		@Bean
		public ZookeeperProperties zookeeperProperties() throws Exception {
			ZookeeperProperties properties = new ZookeeperProperties();
			properties.setConnectString(testingServer().getConnectString());
			return properties;
		}

		@Bean
		public TestingServer testingServer() throws Exception {
			return new TestingServer();
		}
	}
}
