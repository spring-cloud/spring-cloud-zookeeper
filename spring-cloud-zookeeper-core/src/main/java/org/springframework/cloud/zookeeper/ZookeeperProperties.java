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

package org.springframework.cloud.zookeeper;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

/**
 * Properties related to connecting to Zookeeper.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
@Validated
@ConfigurationProperties("spring.cloud.zookeeper")
public class ZookeeperProperties {

	/**
	 * Connection string to the Zookeeper cluster.
	 */
	@NotNull
	private String connectString = "localhost:2181";

	/**
	 * Is Zookeeper enabled.
	 */
	private boolean enabled = true;

	/**
	 * Initial amount of time to wait between retries.
	 */
	private Integer baseSleepTimeMs = 50;

	/**
	 * Max number of times to retry.
	 */
	private Integer maxRetries = 10;

	/**
	 * Max time in ms to sleep on each retry.
	 */
	private Integer maxSleepMs = 500;

	/**
	 * Wait time to block on connection to Zookeeper.
	 */
	private Integer blockUntilConnectedWait = 10;

	/**
	 * The unit of time related to blocking on connection to Zookeeper.
	 */
	private TimeUnit blockUntilConnectedUnit = TimeUnit.SECONDS;

	/**
	 * The configured/negotiated session timeout in milliseconds. Please refer to
	 * <a href='https://cwiki.apache.org/confluence/display/CURATOR/TN14'>Curator's Tech
	 * Note 14</a> to understand how Curator implements connection sessions.
	 *
	 * @see <a href='https://cwiki.apache.org/confluence/display/CURATOR/TN14'>Curator's
	 * Tech Note 14</a>
	 */
	@DurationUnit(ChronoUnit.MILLIS)
	private Duration sessionTimeout = Duration.of(60 * 1000, ChronoUnit.MILLIS);

	/**
	 * The configured connection timeout in milliseconds.
	 */
	@DurationUnit(ChronoUnit.MILLIS)
	private Duration connectionTimeout = Duration.of(15 * 1000, ChronoUnit.MILLIS);

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

	public Duration getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(Duration sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public Duration getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(Duration connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

}
