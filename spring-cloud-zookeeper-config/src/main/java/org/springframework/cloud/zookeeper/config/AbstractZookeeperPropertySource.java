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

package org.springframework.cloud.zookeeper.config;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.core.env.EnumerablePropertySource;

/**
 * A {@link EnumerablePropertySource} that has a notion of a context which is
 * the root folder in Zookeeper.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
public abstract class AbstractZookeeperPropertySource extends EnumerablePropertySource<CuratorFramework> {

	private String context;

	public AbstractZookeeperPropertySource(String context, CuratorFramework source) {
		super(context, source);
		this.context = context;
		if (!this.context.startsWith("/")) {
			this.context = "/" + this.context;
		}
	}

	protected String sanitizeKey(String path) {
		return path.replace(this.context + "/", "").replace('/', '.');
	}

	public String getContext() {
		return this.context;
	}
}
