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

package org.springframework.cloud.zookeeper.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.junit.Test;

import org.springframework.mock.env.MockEnvironment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Spencer Gibb
 */
public class ZookeeperPropertySourceLocatorNoApplicationNameTests {

	@Test
	public void defaultSpringApplicationNameWorks() {
		CuratorFramework curator = mock(CuratorFramework.class);
		when(curator.getChildren()).thenReturn(mock(GetChildrenBuilder.class));
		ZookeeperConfigProperties properties = new ZookeeperConfigProperties();
		properties.setName("notempty");
		ZookeeperPropertySourceLocator locator = new ZookeeperPropertySourceLocator(
				curator, properties);
		locator.locate(new MockEnvironment());
	}

}
