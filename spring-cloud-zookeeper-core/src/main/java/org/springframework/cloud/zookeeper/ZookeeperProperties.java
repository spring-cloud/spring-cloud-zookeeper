/*
 * Copyright 2013-2015 the original author or authors.
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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.zookeeper")
@Data
public class ZookeeperProperties {
	@NotNull
	private String connectString = "localhost:2181";

	private boolean enabled = true;

	/**
	 * @param baseSleepTimeMs initial amount of time to wait between retries
	 */
	private Integer baseSleepTimeMs = 50;

	/**
	 * @param maxRetries max number of times to retry
	 */
	private Integer maxRetries = 50;

	/**
	 * @param maxSleepMs max time in ms to sleep on each retry
	 */
	private Integer maxSleepMs = 500;

}
