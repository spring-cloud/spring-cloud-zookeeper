package org.springframework.cloud.zookeeper;

import javax.annotation.PreDestroy;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
@Configuration
@EnableConfigurationProperties
public class ZookeeperAutoConfiguration {
	@Bean
	@ConditionalOnMissingBean
	public ZookeeperProperties zookeeperProperties() {
		return new ZookeeperProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public CuratorFramework curatorFramework() {
		CuratorFramework curator = CuratorFrameworkFactory.builder()
		// TODO: configurable retry policy
				.retryPolicy(new ExponentialBackoffRetry(1000, 3))
				// .retryPolicy(new RetryOneTime(100))
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
	public ZookeeperEndpoint zookeeperEndpoint() {
		return new ZookeeperEndpoint();
	}

	@Bean
	@ConditionalOnMissingBean
	public ZookeeperHealthIndicator zookeeperHealthIndicator() {
		return new ZookeeperHealthIndicator();
	}

}
