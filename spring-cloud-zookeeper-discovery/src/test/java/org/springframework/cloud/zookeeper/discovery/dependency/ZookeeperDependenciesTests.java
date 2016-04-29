package org.springframework.cloud.zookeeper.discovery.dependency;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
public class ZookeeperDependenciesTests {

	private static final ZookeeperDependency EXPECTED_DEPENDENCY = new ZookeeperDependency(
			"path",
			LoadBalancerType.RANDOM,
			"contentTypeTemplate",
			"version",
			defaultHeader(),
			false,
			""
			);
	private static final Map<String, ZookeeperDependency> DEPENDENCIES = defaultDependencies();

	private static Map<String, Collection<String>> defaultHeader() {
		return Collections.singletonMap("header", (Collection<String>) Collections.singletonList("value"));
	}

	private static Map<String, ZookeeperDependency> defaultDependencies() {
		Map<String, ZookeeperDependency> map = new HashMap<>();
		map.put("alias", EXPECTED_DEPENDENCY);
		return map;
	}

	ZookeeperDependencies zookeeperDependencies = new ZookeeperDependencies();
	
	@Before
	public void setup() {
		this.zookeeperDependencies.setDependencies(DEPENDENCIES);
	}

	@Test public void should_retrieve_dependency_dependency_for_good_path() {
		// expect:
		then(this.zookeeperDependencies.getDependencyForPath("path")).isEqualTo(EXPECTED_DEPENDENCY);
	}

	@Test public void should_retrieve_null_dependency_dependency_for_bad_path() {
		// expect:
		then(this.zookeeperDependencies.getDependencyForPath("unknownPath")).isNull();
	}

	@Test public void should_retrieve_dependency_for_good_alias() {
		// expect:
		then(this.zookeeperDependencies.getDependencyForAlias("alias")).isEqualTo(EXPECTED_DEPENDENCY);
	}

	@Test public void should_retrieve_null_dependency_for_bad_alias() {
		// expect:
		then(this.zookeeperDependencies.getDependencyForAlias("unknownAlias")).isNull();
	}

	@Test public void should_retrieve_alias_for_good_path() {
		// expect:
		then(this.zookeeperDependencies.getAliasForPath("path")).isEqualTo("alias");
	}

	@Test public void should_retrieve_empty_alias_for_bad_path() {
		// expect:
		then(this.zookeeperDependencies.getAliasForPath("unkownPath")).isEmpty();
	}

	@Test public void should_retrieve_path_for_good_alias() {
		// expect:
		then(this.zookeeperDependencies.getPathForAlias("alias")).isEqualTo("path");
	}

	@Test public void should_retrieve_empty_path_for_bad_alias() {
		// expect:
		then(this.zookeeperDependencies.getPathForAlias("unknownAlias")).isEmpty();
	}

	@Test public void should_successfully_replace_version_in_content_type_template() {
		// given:
		ZookeeperDependency zookeeperDependency = new ZookeeperDependency();
		zookeeperDependency.setContentTypeTemplate("application/vnd.some-service.$version+json");
		zookeeperDependency.setVersion("v1");
		// expect:
		then(zookeeperDependency.getContentTypeWithVersion()).isEqualTo("application/vnd.some-service.v1+json");
	}
	
}
