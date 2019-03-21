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

package org.springframework.cloud.zookeeper.discovery;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the default payload of a registered service in Zookeeper.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
public class ZookeeperInstance {

	private String id;

	private String name;

	private Map<String, String> metadata = new HashMap<>();

	@SuppressWarnings("unused")
	private ZookeeperInstance() {
	}

	public ZookeeperInstance(String id, String name, Map<String, String> metadata) {
		this.id = id;
		this.name = name;
		this.metadata = metadata;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getMetadata() {
		return this.metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "ZookeeperInstance{" + "id='" + this.id + '\'' + ", name='" + this.name
				+ '\'' + ", metadata=" + this.metadata + '}';
	}

}
