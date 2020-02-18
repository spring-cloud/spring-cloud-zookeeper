/*
 * Copyright 2015-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.zookeeper.discovery.dependency;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.zookeeper.ConditionalOnZookeeperEnabled;
import org.springframework.cloud.zookeeper.discovery.ConditionalOnLoadBalancerForZookeeperEnabled;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
@AutoConfigureBefore({ReactorLoadBalancerClientAutoConfiguration.class,
		BlockingLoadBalancerClientAutoConfiguration.class})
@ConditionalOnProperty(value = "spring.cloud.zookeeper.dependency.loadbalancer.enabled", matchIfMissing = true)
@Import({ReactiveDependencyLoadBalancerConfiguration.class, BlockingDependencyLoadBalancerConfiguration.class})
public class DependencyLoadBalancerAutoConfiguration {

}


