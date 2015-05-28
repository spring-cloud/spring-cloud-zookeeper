package org.springframework.cloud.zookeeper

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.test.TestingServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * @author Spencer Gibb
 */
@ContextConfiguration(classes = [ TestConfig, ZookeeperAutoConfiguration ])
class ZookeeperAutoConfigurationSpec extends Specification {

	@Autowired(required = false)
	CuratorFramework curator

	def 'should successfully inject Curator as a Spring bean'() {
		expect:
			curator != null
	}

	static class TestConfig {
		@Bean
		ZookeeperProperties zookeeperProperties() throws Exception {
			ZookeeperProperties properties = new ZookeeperProperties()
			properties.connectString = testingServer().connectString
			return properties
		}

		@Bean
		TestingServer testingServer() throws Exception {
			return new TestingServer()
		}
	}
}
