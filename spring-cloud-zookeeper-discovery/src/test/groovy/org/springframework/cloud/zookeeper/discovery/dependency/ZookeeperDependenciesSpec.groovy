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

package org.springframework.cloud.zookeeper.discovery.dependency

import spock.lang.Specification
import spock.lang.Unroll

class ZookeeperDependenciesSpec extends Specification {

	private static final ZookeeperDependency EXPECTED_DEPENDENCY = new ZookeeperDependency(
			'path',
			LoadBalancerType.RANDOM,
			'contentTypeTemplate',
			'version',
			[header: 'value'],
			false
	)
	private static final Map<String, ZookeeperDependency> DEPENDENCIES = [
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

	def "should successfully replace version in content type template"() {
		given:
			ZookeeperDependency zookeeperDependency = new ZookeeperDependency(
					contentTypeTemplate: 'application/vnd.some-service.$version+json',
					version: 'v1'
			)
		expect:
			'application/vnd.some-service.v1+json' == zookeeperDependency.contentTypeWithVersion
	}
}
