package org.springframework.cloud.zookeeper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.ensemble.fixed.FixedEnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Konrad Kamil Dobrzyński
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ZookeeperAutoConfigurationEnsembleTests.TestConfig.class, ZookeeperAutoConfiguration.class })
public class ZookeeperAutoConfigurationEnsembleTests {

	@Autowired(required = false) CuratorFramework curator;

	@Autowired TestingServer testingServer;
	
	@Test
	public void should_successfully_inject_Curator_with_ensemble_connection_string() {
		assertEquals(testingServer.getConnectString(), curator.getZookeeperClient().getCurrentConnectionString());
		assertNotEquals(TestConfig.DUMMY_CONNECTION_STRING, curator.getZookeeperClient().getCurrentConnectionString());
	}

	static class TestConfig {

		static final String DUMMY_CONNECTION_STRING = "dummy-connection-string:2111";

		@Bean
		EnsembleProvider ensembleProvider(TestingServer testingServer){
			return new FixedEnsembleProvider(testingServer.getConnectString());
		}

		@Bean
		ZookeeperProperties zookeeperProperties() {
			ZookeeperProperties properties = new ZookeeperProperties();
			properties.setConnectString(DUMMY_CONNECTION_STRING);
			return properties;
		}

		@Bean(destroyMethod = "close") TestingServer testingServer() throws Exception {
			return new TestingServer();
		}
	}
}
