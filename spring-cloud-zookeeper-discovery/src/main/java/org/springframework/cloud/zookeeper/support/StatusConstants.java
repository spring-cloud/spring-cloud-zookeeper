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

package org.springframework.cloud.zookeeper.support;

import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;

/**
 * @author Spencer Gibb
 */
public final class StatusConstants {

	private StatusConstants() {
	}

	/**
	 * Key to the
	 * {@link ZookeeperInstance#getMetadata()}
	 * map.
	 */
	public static final String INSTANCE_STATUS_KEY = "instance_status";

	/**
	 * UP value for {@link StatusConstants#INSTANCE_STATUS_KEY} key.
	 */
	public static final String STATUS_UP = "UP";

	/**
	 * OUT_OF_SERVICE value for {@link StatusConstants#INSTANCE_STATUS_KEY} key.
	 */
	public static final String STATUS_OUT_OF_SERVICE = "OUT_OF_SERVICE";

}
