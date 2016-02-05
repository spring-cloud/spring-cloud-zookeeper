/*
 * Copyright 2013-2016 the original author or authors.
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

import java.nio.charset.Charset;

import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.springframework.context.ApplicationEvent;

/**
 * @author Spencer Gibb
 */
public class ZookeeperConfigRefreshEvent extends ApplicationEvent {

	private TreeCacheEvent event;

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param source the object on which the event initially occurred (never {@code null})
	 */
	public ZookeeperConfigRefreshEvent(Object source, TreeCacheEvent event) {
		super(source);
		this.event = event;
	}

	public TreeCacheEvent getEvent() {
		return event;
	}

	public String getEventDesc() {
		StringBuffer out = new StringBuffer();
		out.append("type="+event.getType());
		out.append(", path="+event.getData().getPath());
		byte[] data = event.getData().getData();
		if (data != null && data.length > 0) {
			out.append(", data="+new String(data, Charset.forName("UTF-8")));
		}
		return out.toString();
	}
}
