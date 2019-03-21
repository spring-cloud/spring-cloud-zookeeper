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

import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
public class StubsConfigurationTests {

	@Test
	public void should_return_empty_colon_separated_dependency_notation_if_empty_path_has_been_provided() {
		// given:
		String path = "";
		// when:
		StubsConfiguration stubsConfiguration = new StubsConfiguration(path);
		// then:
		then(stubsConfiguration.toColonSeparatedDependencyNotation()).isEqualTo("");
	}

	@Test
	public void should_return_empty_colon_separated_dependency_notation_if_invalid_path_has_been_provided() {
		// given:
		String path = "pl/";
		// when:
		StubsConfiguration stubsConfiguration = new StubsConfiguration(path);
		// then:
		then(stubsConfiguration.toColonSeparatedDependencyNotation()).isEqualTo("");
	}

	@Test
	public void should_properly_parse_invalid_colon_separated_path_into_empty_notation() {
		// given:
		String path = "pl/a";
		// when:
		StubsConfiguration stubsConfiguration = new StubsConfiguration(path);
		// then:
		then(stubsConfiguration.toColonSeparatedDependencyNotation()).isEqualTo("");
		then(stubsConfiguration.getStubsGroupId()).isEqualTo("");
		then(stubsConfiguration.getStubsArtifactId()).isEqualTo("");
		then(stubsConfiguration.getStubsClassifier()).isEqualTo("");
	}

	@Test
	public void should_parse_the_path_into_group_artifact_and_classifier() {
		// given:
		String path = "pl/a";
		// when:
		StubsConfiguration stubsConfiguration = new StubsConfiguration(
				new StubsConfiguration.DependencyPath(path));
		// then:
		then(stubsConfiguration.toColonSeparatedDependencyNotation())
				.isEqualTo("pl:a:stubs");
		then(stubsConfiguration.getStubsGroupId()).isEqualTo("pl");
		then(stubsConfiguration.getStubsArtifactId()).isEqualTo("a");
		then(stubsConfiguration.getStubsClassifier()).isEqualTo("stubs");
	}

	@Test
	public void should_properly_set_group_artifact_and_classifier() {
		// when:
		StubsConfiguration stubsConfiguration = new StubsConfiguration("pl", "a",
				"superstubs");
		// then:
		then(stubsConfiguration.toColonSeparatedDependencyNotation())
				.isEqualTo("pl:a:superstubs");
		then(stubsConfiguration.getStubsGroupId()).isEqualTo("pl");
		then(stubsConfiguration.getStubsArtifactId()).isEqualTo("a");
		then(stubsConfiguration.getStubsClassifier()).isEqualTo("superstubs");
	}

}
