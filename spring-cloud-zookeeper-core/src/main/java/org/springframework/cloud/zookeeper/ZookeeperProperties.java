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

import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.zookeeper")
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
	private Integer maxRetries = 10;

	/**
	 * @param maxSleepMs max time in ms to sleep on each retry
	 */
	private Integer maxSleepMs = 500;

	private Integer blockUntilConnectedWait = 10;

	private TimeUnit blockUntilConnectedUnit = TimeUnit.SECONDS;

	public String getConnectString() {
		return this.connectString;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public Integer getBaseSleepTimeMs() {
		return this.baseSleepTimeMs;
	}

	public Integer getMaxRetries() {
		return this.maxRetries;
	}

	public Integer getMaxSleepMs() {
		return this.maxSleepMs;
	}

	public Integer getBlockUntilConnectedWait() {
		return this.blockUntilConnectedWait;
	}

	public TimeUnit getBlockUntilConnectedUnit() {
		return this.blockUntilConnectedUnit;
	}

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setBaseSleepTimeMs(Integer baseSleepTimeMs) {
		this.baseSleepTimeMs = baseSleepTimeMs;
	}

	public void setMaxRetries(Integer maxRetries) {
		this.maxRetries = maxRetries;
	}

	public void setMaxSleepMs(Integer maxSleepMs) {
		this.maxSleepMs = maxSleepMs;
	}

	public void setBlockUntilConnectedWait(Integer blockUntilConnectedWait) {
		this.blockUntilConnectedWait = blockUntilConnectedWait;
	}

	public void setBlockUntilConnectedUnit(TimeUnit blockUntilConnectedUnit) {
		this.blockUntilConnectedUnit = blockUntilConnectedUnit;
	}
}
