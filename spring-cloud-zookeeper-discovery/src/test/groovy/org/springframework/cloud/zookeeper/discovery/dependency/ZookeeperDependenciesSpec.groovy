package org.springframework.cloud.zookeeper.discovery.dependency

import spock.lang.Specification
import spock.lang.Unroll

class ZookeeperDependenciesSpec extends Specification {

	private static final ZookeeperDependencies.ZookeeperDependency EXPECTED_DEPENDENCY = new ZookeeperDependencies.ZookeeperDependency(
			'id',
			'path',
			LoadBalancerType.RANDOM,
			'contentTypeTemplate',
			'version',
			[header: 'value'],
			false
	)
	private static final Map<String, ZookeeperDependencies.ZookeeperDependency> DEPENDENCIES = [
			alias: EXPECTED_DEPENDENCY
	]

	ZookeeperDependencies zookeeperDependencies = new ZookeeperDependencies(dependencies: DEPENDENCIES)

	@Unroll
	def "should retrieve dependency [#expectedDependency] for path [#path]"() {
		expect:
			expectedDependency == zookeeperDependencies.getDependencyForPath(path)
		where:
			path          || expectedDependency
			'unknownPath' || null
			'path'        || EXPECTED_DEPENDENCY
	}

	@Unroll
	def "should retrieve dependency [#expectedDependency] for alias [#alias]"() {
		expect:
			expectedDependency == zookeeperDependencies.getDependencyForAlias(alias)
		where:
			alias          || expectedDependency
			'unknownAlias' || null
			'alias'        || EXPECTED_DEPENDENCY
	}

	@Unroll
	def "should retrieve alias [#expectedAlias] for path [#path]"() {
		expect:
			expectedAlias == zookeeperDependencies.getAliasForPath(path)
		where:
			path          || expectedAlias
			'unknownPath' || ''
			'path'        || 'alias'
	}

	@Unroll
	def "should retrieve path [#expectedPath] for alias [#alias]"() {
		expect:
			expectedPath == zookeeperDependencies.getPathForAlias(alias)
		where:
			alias          || expectedPath
			'unknownAlias' || ''
			'alias'        || 'path'
	}


}
