package org.springframework.cloud.zookeeper.discovery.dependency;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Arrays;


/**
 * Representation of a stubs location
 *
 * @author Marcin Grzejszczak
 */
@Data
public class StubsConfiguration {
	private static final String DEFAULT_STUBS_CLASSIFIER = "stubs";
	private static final String STUB_COLON_DELIMITER = ":";
	private static final String PATH_SLASH_DELIMITER = "/";

	private final String stubsGroupId;
	private final String stubsArtifactId;
	private final String stubsClassifier;

	public StubsConfiguration(String stubsGroupId, String stubsArtifactId, String stubsClassifier) {
		this.stubsGroupId = stubsGroupId;
		this.stubsArtifactId = stubsArtifactId;
		this.stubsClassifier = StringUtils.hasText(stubsClassifier) ? stubsClassifier : DEFAULT_STUBS_CLASSIFIER;
	}

	public StubsConfiguration(String stubPath) {
		String[] parsedPath = parsedPathEmptyByDefault(stubPath, STUB_COLON_DELIMITER);
		this.stubsGroupId = parsedPath[0];
		this.stubsArtifactId = parsedPath[1];
		this.stubsClassifier = parsedPath[2];
	}

	public StubsConfiguration(DependencyPath path) {
		String[] parsedPath = parsedPathEmptyByDefault(path.getPath(), PATH_SLASH_DELIMITER);
		this.stubsGroupId = parsedPath[0];
		this.stubsArtifactId = parsedPath[1];
		this.stubsClassifier = parsedPath[2];
	}

	private String[] parsedPathEmptyByDefault(String path, String delimiter) {
		String[] splitPath = path.split(delimiter);
		String stubsGroupId = "";
		String stubsArtifactId = "";
		String stubsClassifier = "";
		if (splitPath.length >= 2) {
			stubsGroupId = splitPath[0];
			stubsArtifactId = splitPath[1];
			stubsClassifier = splitPath.length == 3 ? splitPath[2] : DEFAULT_STUBS_CLASSIFIER;
		}
		return new String[]{stubsGroupId, stubsArtifactId, stubsClassifier};
	}

	private boolean isDefined() {
		return StringUtils.hasText(stubsGroupId) && StringUtils.hasText(stubsArtifactId);
	}

	public String toColonSeparatedDependencyNotation() {
		if(!isDefined()) {
			return "";
		}
		return StringUtils.collectionToDelimitedString(Arrays.asList(getStubsGroupId(), getStubsArtifactId(), getStubsClassifier()), STUB_COLON_DELIMITER);
	}

	/**
	 * Marker class to discern between the stubs location and dependency registration path
	 */
	@Data
	static class DependencyPath {
		private final String path;
	}
}
