package org.springframework.cloud.zookeeper.discovery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.zookeeper.discovery.test.CommonTestConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT,
		properties = "management.health.zookeeper.enabled=false")
public class ZookeeperDiscoveryHealthIndicatorDisabledTests {

	@Autowired(required = false)
	private ZookeeperDiscoveryHealthIndicator healthIndicator;

	// Issue: #101 - ZookeeperDiscoveryHealthIndicator should be able to be disabled with a property
	@Test public void healthIndicatorDisabled() {
		// when:
		// then:
		then(this.healthIndicator).isNull();
	}
	
	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(CommonTestConfig.class)
	static class Config {}

}
