/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto {@link Configuration} for adding a Zookeeper health endpoint to actuator if
 * required.
 * 
 * @author tgianos
 * @since 2.0.1
 */
@Configuration
@ConditionalOnZookeeperEnabled
@ConditionalOnClass(Endpoint.class)
@AutoConfigureAfter({ ZookeeperAutoConfiguration.class })
public class ZookeeperHealthAutoConfiguration {

	/**
	 * If there is an active curator, if the zookeeper health endpoint is enabled and if a
	 * health indicator hasn't already been added by a user add one.
	 * 
	 * @param curator The curator connection to zookeeper to use
	 * @return An instance of {@link ZookeeperHealthIndicator} to add to actuator health
	 * report
	 */
	@Bean
	@ConditionalOnMissingBean(ZookeeperHealthIndicator.class)
	@ConditionalOnBean(CuratorFramework.class)
	@ConditionalOnEnabledHealthIndicator("zookeeper")
	public ZookeeperHealthIndicator zookeeperHealthIndicator(CuratorFramework curator) {
		return new ZookeeperHealthIndicator(curator);
	}
}
