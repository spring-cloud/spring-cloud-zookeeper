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

package org.springframework.cloud.zookeeper.discovery.dependency;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class ZookeeperDependenciesTest {

	@Test
	public void should_properly_sanitize_dependency_path() {
		// given:
		Map<String, ZookeeperDependency> dependencies = new LinkedHashMap<>();
		ZookeeperDependency cat = new ZookeeperDependency();
		cat.setPath("/cats/cat");
		dependencies.put("cat", cat);
		ZookeeperDependency dog = new ZookeeperDependency();
		dog.setPath("dogs/dog");
		dependencies.put("dog", dog);
		ZookeeperDependencies zookeeperDependencies = new ZookeeperDependencies();
		zookeeperDependencies.setDependencies(dependencies);
		// when:
		zookeeperDependencies.init();
		// then:
		then(zookeeperDependencies.getDependencies().get("cat").getPath())
				.isEqualTo("/cats/cat");
		then(zookeeperDependencies.getDependencies().get("dog").getPath())
				.isEqualTo("/dogs/dog");
	}

	@Test
	public void should_properly_sanitize_dependency_path_with_prefix() {
		// given:
		Map<String, ZookeeperDependency> dependencies = new LinkedHashMap<>();
		ZookeeperDependency cat = new ZookeeperDependency();
		cat.setPath("/cats/cat");
		dependencies.put("cat", cat);
		ZookeeperDependency dog = new ZookeeperDependency();
		dog.setPath("dogs/dog");
		dependencies.put("dog", dog);
		ZookeeperDependencies zookeeperDependencies = new ZookeeperDependencies();
		zookeeperDependencies.setPrefix("animals/");
		zookeeperDependencies.setDependencies(dependencies);
		// when:
		zookeeperDependencies.init();
		// then:
		then(zookeeperDependencies.getDependencies().get("cat").getPath())
				.isEqualTo("/animals/cats/cat");
		then(zookeeperDependencies.getDependencies().get("dog").getPath())
				.isEqualTo("/animals/dogs/dog");
	}

}
