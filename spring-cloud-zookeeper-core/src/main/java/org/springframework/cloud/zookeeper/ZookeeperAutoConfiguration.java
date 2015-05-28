package org.springframework.cloud.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

/**
 * @author Spencer Gibb
 */
@Configuration
@EnableConfigurationProperties
public class ZookeeperAutoConfiguration {

	private @Autowired RetryPolicy retryPolicy;

	@Bean
	@ConditionalOnMissingBean
	public ZookeeperProperties zookeeperProperties() {
		return new ZookeeperProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public CuratorFramework curatorFramework() {
		CuratorFramework curator = CuratorFrameworkFactory.builder()
				.retryPolicy(retryPolicy)
				// TODO: support ensembleProvider via ExhibitorEnsembleProvider
				// .ensembleProvider(new ExhibitorEnsembleProvider())
				.connectString(zookeeperProperties().getConnectString()).build();
		curator.start();
		return curator;
	}

	@PreDestroy
	public void shutdown() {
		curatorFramework().close();
	}

	@Bean
	@ConditionalOnMissingBean
	public RetryPolicy exponentialBackoffRetry() {
		return new ExponentialBackoffRetry(zookeeperProperties().getBaseSleepTimeMs(),
				zookeeperProperties().getMaxRetries(),
				zookeeperProperties().getMaxSleepMs());
	}

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
