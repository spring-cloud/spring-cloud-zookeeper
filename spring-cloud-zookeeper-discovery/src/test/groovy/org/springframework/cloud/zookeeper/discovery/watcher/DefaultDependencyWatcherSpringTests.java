package org.springframework.cloud.zookeeper.discovery.watcher;

import java.util.concurrent.Callable;

import com.jayway.awaitility.Awaitility;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.x.discovery.ServiceCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.zookeeper.discovery.CustomZookeeperServiceDiscovery;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceDiscovery;
import org.springframework.cloud.zookeeper.discovery.watcher.presence.DependencyPresenceOnStartupVerifier;
import org.springframework.cloud.zookeeper.discovery.watcher.presence.LogMissingDependencyChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DefaultDependencyWatcherSpringTests.Config.class)
@ActiveProfiles("watcher")
public class DefaultDependencyWatcherSpringTests {

	@Autowired AssertableDependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier;
	@Autowired AssertableDependencyWatcherListener dependencyWatcherListener;
	@Autowired ZookeeperServiceDiscovery serviceDiscovery;

	@Test public void should_verify_that_presence_of_a_dependency_has_been_checked() {
		then(this.dependencyPresenceOnStartupVerifier.startupPresenceVerified).isTrue();
	}

	@Test public void should_verify_that_dependency_watcher_listener_is_successfully_registered_and_operational()
			throws Exception {
		//when:
		this.serviceDiscovery.getServiceDiscovery().unregisterService(this.serviceDiscovery.getServiceInstance());

		//then:
		Awaitility.await().until(new Callable<Boolean>() {
			@Override public Boolean call() throws Exception {
				then(DefaultDependencyWatcherSpringTests.this.dependencyWatcherListener.dependencyState).isEqualTo(DependencyState.DISCONNECTED);
				return true;
			}
		});
	}

	@Configuration
	@EnableAutoConfiguration
	@Profile("watcher")
	static class Config {

		@Bean
		@LoadBalanced RestTemplate loadBalancedRestTemplate() {
			return new RestTemplate();
		}

		@Bean
		static PropertySourcesPlaceholderConfigurer propertiesConfigurer() {
			return new PropertySourcesPlaceholderConfigurer();
		}

		@Bean(destroyMethod = "close") TestingServer testingServer() throws Exception {
			return new TestingServer(SocketUtils.findAvailableTcpPort());
		}

		@Bean ZookeeperServiceDiscovery zookeeperServiceDiscovery() throws Exception {
			return new MyZookeeperServiceDiscovery(curatorFramework());
		}

		@Bean(initMethod = "start", destroyMethod = "close")
		CuratorFramework curatorFramework() throws Exception {
			return CuratorFrameworkFactory
					.newClient(testingServer().getConnectString(), new ExponentialBackoffRetry(20, 20, 500));
		}

		@Bean
		DependencyWatcherListener dependencyWatcherListener() {
			return new AssertableDependencyWatcherListener();
		}

		@Bean DependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier() {
			return new AssertableDependencyPresenceOnStartupVerifier();
		}

	}

	static class MyZookeeperServiceDiscovery extends CustomZookeeperServiceDiscovery {
		MyZookeeperServiceDiscovery(CuratorFramework curator) {
			super("testInstance", curator);
		}
	}

	static class AssertableDependencyWatcherListener implements DependencyWatcherListener {

		DependencyState dependencyState = DependencyState.CONNECTED;

		@Override
		public void stateChanged(String dependencyName, DependencyState newState) {
			dependencyState = newState;
		}
	}

	static class AssertableDependencyPresenceOnStartupVerifier extends DependencyPresenceOnStartupVerifier {

		boolean startupPresenceVerified = false;

		AssertableDependencyPresenceOnStartupVerifier() {
			super(new LogMissingDependencyChecker());
		}

		@Override public void verifyDependencyPresence(String dependencyName,
				ServiceCache serviceCache, boolean required) {
			startupPresenceVerified = true;
		}
	}
}
