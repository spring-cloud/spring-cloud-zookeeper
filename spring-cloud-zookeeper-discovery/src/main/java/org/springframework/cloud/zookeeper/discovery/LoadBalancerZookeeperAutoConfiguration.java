package org.springframework.cloud.zookeeper.discovery;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.springframework.cloud.zookeeper.ConditionalOnZookeeperEnabled;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependenciesAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} that sets up Spring Cloud LoadBalancer for Zookeeper.
 *
 * @author Olga Maciaszek-Sharma
 * @since 3.0.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnZookeeperEnabled
@ConditionalOnBean(ReactiveLoadBalancer.Factory.class)
@ConditionalOnLoadBalancerForZookeeperEnabled
@AutoConfigureAfter({LoadBalancerAutoConfiguration.class, ZookeeperDependenciesAutoConfiguration.class})
@LoadBalancerClients(defaultConfiguration = ZookeeperLoadBalancerConfiguration.class)
public class LoadBalancerZookeeperAutoConfiguration {
}
