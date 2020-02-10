package org.springframework.cloud.zookeeper.discovery.dependency;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Mono;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.zookeeper.ConditionalOnZookeeperEnabled;
import org.springframework.cloud.zookeeper.discovery.ConditionalOnLoadBalancerForZookeeperEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a {@link ReactorLoadBalancerExchangeFilterFunction} instance that can be used to
 * choose a correct {@link ServiceInstance} based on Zookeeper dependencies from properties.
 *
 * @author Olga Maciaszek-Sharma
 * @since 3.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnZookeeperEnabled
@ConditionalOnLoadBalancerForZookeeperEnabled
@ConditionalOnDependenciesPassed
@AutoConfigureBefore(ReactorLoadBalancerClientAutoConfiguration.class)
@ConditionalOnBean(ReactiveLoadBalancer.Factory.class)
public class DependencyLoadBalancerAutoConfiguration {

	private final ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory;

	public DependencyLoadBalancerAutoConfiguration(ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory) {
		this.loadBalancerFactory = loadBalancerFactory;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(value = "spring.cloud.zookeeper.dependency.loadbalancer.enabled", matchIfMissing = true)
	ReactorLoadBalancerExchangeFilterFunction reactorLoadBalancerExchangeFilterFunction() {
		return new ReactorLoadBalancerExchangeFilterFunction(loadBalancerFactory) {

			private final Log log = LogFactory
					.getLog(ReactorLoadBalancerExchangeFilterFunction.class
							+ "for Zookeeper");

			@Override
			protected Mono<Response<ServiceInstance>> choose(String serviceId) {
				ReactiveLoadBalancer<ServiceInstance> loadBalancer = loadBalancerFactory
						.getInstance(serviceId);
				if (loadBalancer != null) {
					log.debug(String.format(
							"Dependencies are set - will try to load balance via LoadBalancer for key [%s]",
							serviceId));
					return Mono.from(loadBalancer.choose())
							.switchIfEmpty(Mono.defer(this::chooseDefault));
				}
				return chooseDefault();
			}

			private Mono<Response<ServiceInstance>> chooseDefault() {
				log.debug("Could not find instance via LoadBalancer for provided service id. "
						+ "Will try retrieving default instance");
				ReactiveLoadBalancer<ServiceInstance> defaultLoadBalancer = loadBalancerFactory
						.getInstance("default");
				if (defaultLoadBalancer == null) {
					return Mono.just(new EmptyResponse());
				}
				return Mono.from(defaultLoadBalancer.choose());
			}
		};

	}
}


