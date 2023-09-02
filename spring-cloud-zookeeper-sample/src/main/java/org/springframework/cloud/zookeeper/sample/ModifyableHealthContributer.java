/*
 * Copyright 2015-2021 the original author or authors.
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

package org.springframework.cloud.zookeeper.sample;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

public class ModifyableHealthContributer extends AbstractHealthIndicator {

	private boolean isHealthy = true;

	@Override
	protected void doHealthCheck(Builder builder) throws Exception {
		if (isHealthy) {
			builder.up().build();
		}
		else {
			builder.down().build();
		}
	}

	public void setHealthy(boolean healthy) {
		isHealthy = healthy;
	}
}
