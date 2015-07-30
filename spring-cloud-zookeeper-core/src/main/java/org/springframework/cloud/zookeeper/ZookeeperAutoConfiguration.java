package org.springframework.cloud.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnProperty(value = "zookeeper.enabled", matchIfMissing = true)
@EnableConfigurationProperties
public class ZookeeperAutoConfiguration {

	@Autowired(required = false)
	private EnsembleProvider ensembleProvider;

	@Bean
	@ConditionalOnMissingBean
	public ZookeeperProperties zookeeperProperties() {
		return new ZookeeperProperties();
	}

	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean
	public CuratorFramework curatorFramework(RetryPolicy retryPolicy) {
		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
		if (ensembleProvider != null) {
			builder.ensembleProvider(ensembleProvider);
		}
		CuratorFramework curator = builder
				.retryPolicy(retryPolicy)
				.connectString(zookeeperProperties().getConnectString())
				.build();
		curator.start();
		return curator;
	}

	@Bean
	@ConditionalOnMissingBean
	public RetryPolicy exponentialBackoffRetry() {
		return new ExponentialBackoffRetry(zookeeperProperties().getBaseSleepTimeMs(),
				zookeeperProperties().getMaxRetries(),
				zookeeperProperties().getMaxSleepMs());
	}

	@Configuration
	@ConditionalOnClass(Endpoint.class)
	protected static class ZookeeperHealthConfig {
		@Bean
		@ConditionalOnMissingBean
		public ZookeeperEndpoint zookeeperEndpoint() {
			return new ZookeeperEndpoint();
		}
        @Bean
        @ConditionalOnMissingBean
        public ZookeeperHealthIndicator zookeeperHealthIndicator() {
		return new ZookeeperHealthIndicator();
	}
    }
}
