package org.springframework.cloud.zookeeper.discovery.dependency;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.test.TestingServer;
import org.assertj.core.api.BDDAssertions;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.zookeeper.discovery.test.TestRibbonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.SocketUtils;

import static com.jayway.awaitility.Awaitility.await;

/**
 * @author Marcin Grzejszczak
 */
@Ignore
public class ZookeeperDiscoveryWithDyingDependenciesTests {

	private static final Log log = LogFactory.getLog(ZookeeperDiscoveryWithDyingDependenciesTests.class);

	// Issue: #45
	@Test public void should_refresh_a_dependency_in_Ribbon_when_the_dependency_has_deregistered_and_registered_in_Zookeeper()
			throws Exception {
		ConfigurableApplicationContext serverContext = null;
		ConfigurableApplicationContext clientContext = null;
		TestingServer testingServer = null;
		try {
			//given:
			int zookeeperPort = SocketUtils.findAvailableTcpPort();
			testingServer = new TestingServer(zookeeperPort);
			System.setProperty("spring.jmx.enabled", "false");
			System.setProperty("spring.cloud.zookeeper.connectString", "127.0.0.1:"+zookeeperPort);
			//and:
			serverContext = contextWithProfile("server");
			clientContext = contextWithProfile("client");
			//and:
			Integer serverPortBeforeDying = callServiceAtPortEndpoint(clientContext);
			//and:
			serverContext = restartContext(serverContext, "server");
			//expect:
			await().atMost(5, TimeUnit.SECONDS).until(
					applicationHasStartedOnANewPort(clientContext, serverPortBeforeDying)
			);
		} finally {
			//cleanup:
			close(serverContext);
			close(clientContext);
			close(testingServer);
		}
	}

	private Callable<Boolean> applicationHasStartedOnANewPort(
			final ConfigurableApplicationContext clientContext,
			final Integer serverPortBeforeDying) {
		return new Callable<Boolean>() {
			@Override public Boolean call() throws Exception {
				try {
					BDDAssertions.then(callServiceAtPortEndpoint(clientContext)).isNotEqualTo(serverPortBeforeDying);
				} catch (Exception e) {
					log.error("Exception occurred while trying to call the server", e);
					return false;
				}
				return true;
			}
		};
	}

	private void close(Closeable closeable) throws IOException {
		if(closeable != null) {
			closeable.close();
		}
	}
	private ConfigurableApplicationContext contextWithProfile(String profile) {
		return new SpringApplicationBuilder(Config.class).profiles(profile).build().run();
	}

	private ConfigurableApplicationContext restartContext(ConfigurableApplicationContext configurableApplicationContext, String profile)
			throws IOException {
		close(configurableApplicationContext);
		return contextWithProfile(profile);
	}

	private Integer callServiceAtPortEndpoint(ApplicationContext applicationContext) {
		return applicationContext.getBean(TestRibbonClient.class).callService("testInstance", "port", Integer.class);
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(DependencyConfig.class)
	static class Config { }
}
