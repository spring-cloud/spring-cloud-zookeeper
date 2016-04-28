package org.springframework.cloud.zookeeper.discovery.dependency;

import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
public class StubsConfigurationTests {

	@Test
	public void should_return_empty_colon_separated_dependency_notation_if_empty_path_has_been_provided() {
		//given:
		String path = "";
		//when:
		StubsConfiguration stubsConfiguration = new StubsConfiguration(path);
		//then:
		then(stubsConfiguration.toColonSeparatedDependencyNotation()).isEqualTo("");
	}
	@Test
	public void should_return_empty_colon_separated_dependency_notation_if_invalid_path_has_been_provided() {
		//given:
		String path = "pl/";
		//when:
		StubsConfiguration stubsConfiguration = new StubsConfiguration(path);
		//then:
		then(stubsConfiguration.toColonSeparatedDependencyNotation()).isEqualTo("");
	}

	@Test
	public void should_properly_parse_invalid_colon_separated_path_into_empty_notation() {
		//given:
		String path = "pl/a";
		//when:
		StubsConfiguration stubsConfiguration = new StubsConfiguration(path);
		//then:
		then(stubsConfiguration.toColonSeparatedDependencyNotation()).isEqualTo("");
		then(stubsConfiguration.getStubsGroupId()).isEqualTo("");
		then(stubsConfiguration.getStubsArtifactId()).isEqualTo("");
		then(stubsConfiguration.getStubsClassifier()).isEqualTo("");
	}

	@Test
	public void should_parse_the_path_into_group_artifact_and_classifier() {
		//given:
		String path = "pl/a";
		//when:
		StubsConfiguration stubsConfiguration = new StubsConfiguration(new StubsConfiguration.DependencyPath(path));
		//then:
		then(stubsConfiguration.toColonSeparatedDependencyNotation()).isEqualTo("pl:a:stubs");
		then(stubsConfiguration.getStubsGroupId()).isEqualTo("pl");
		then(stubsConfiguration.getStubsArtifactId()).isEqualTo("a");
		then(stubsConfiguration.getStubsClassifier()).isEqualTo("stubs");
	}

	@Test
	public void should_properly_set_group_artifact_and_classifier() {
		//when:
		StubsConfiguration stubsConfiguration = new StubsConfiguration("pl", "a", "superstubs");
		//then:
		then(stubsConfiguration.toColonSeparatedDependencyNotation()).isEqualTo("pl:a:superstubs");
		then(stubsConfiguration.getStubsGroupId()).isEqualTo("pl");
		then(stubsConfiguration.getStubsArtifactId()).isEqualTo("a");
		then(stubsConfiguration.getStubsClassifier()).isEqualTo("superstubs");
	}
	
}
