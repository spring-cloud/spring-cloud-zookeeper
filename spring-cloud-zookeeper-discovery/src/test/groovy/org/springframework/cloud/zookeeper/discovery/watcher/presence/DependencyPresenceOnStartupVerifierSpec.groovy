/*
 * Copyright 2012-2015 the original author or authors.
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
package org.springframework.cloud.zookeeper.discovery.watcher.presence

import org.apache.curator.x.discovery.ServiceCache
import spock.lang.Specification

class DependencyPresenceOnStartupVerifierSpec extends Specification {

	private static final String SERVICE_NAME = 'service01'

	def 'should check optional dependency using optional dependency checker'() {
		given:
			PresenceChecker optionalDependencyChecker = Mock()
			DependencyPresenceOnStartupVerifier dependencyVerifier = new DependencyPresenceOnStartupVerifier(optionalDependencyChecker) {
			}
			ServiceCache serviceCache = Mock()
			serviceCache.instances >> []
		when:
			dependencyVerifier.verifyDependencyPresence(SERVICE_NAME, serviceCache, false)
		then:
			1 * optionalDependencyChecker.checkPresence(SERVICE_NAME, serviceCache.instances)
	}

}
