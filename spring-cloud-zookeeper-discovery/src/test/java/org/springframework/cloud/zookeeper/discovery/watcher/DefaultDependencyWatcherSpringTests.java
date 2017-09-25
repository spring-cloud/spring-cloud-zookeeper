package org.springframework.cloud.zookeeper.discovery.watcher;

import java.util.concurrent.Callable;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.x.discovery.ServiceCache;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.zookeeper.discovery.watcher.presence.DependencyPresenceOnStartupVerifier;
import org.springframework.cloud.zookeeper.discovery.watcher.presence.LogMissingDependencyChecker;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

import com.jayway.awaitility.Awaitility;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DefaultDependencyWatcherSpringTests.Config.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("watcher")
public class DefaultDependencyWatcherSpringTests {

	@Autowired AssertableDependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier;
	@Autowired AssertableDependencyWatcherListener dependencyWatcherListener;
	@Autowired ZookeeperRegistration zookeeperRegistration;
	@Autowired ZookeeperServiceRegistry registry;


	@Test public void should_verify_that_presence_of_a_dependency_has_been_checked() {
		then(this.dependencyPresenceOnStartupVerifier.startupPresenceVerified).isTrue();
	}

	@Ignore //FIXME 2.0.0
	@Test public void should_verify_that_dependency_watcher_listener_is_successfully_registered_and_operational()
			throws Exception {
		//when:
		this.registry.deregister(this.zookeeperRegistration);

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

		@Bean(initMethod = "start", destroyMethod = "close")
		CuratorFramework curatorFramework() throws Exception {
			CuratorFramework curatorFramework = CuratorFrameworkFactory
					.newClient(testingServer().getConnectString(), new ExponentialBackoffRetry(20, 20, 500));
			return curatorFramework;
		}

		@Bean
		DependencyWatcherListener dependencyWatcherListener() {
			return new AssertableDependencyWatcherListener();
		}

		@Bean DependencyPresenceOnStartupVerifier dependencyPresenceOnStartupVerifier() {
			return new AssertableDependencyPresenceOnStartupVerifier();
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
