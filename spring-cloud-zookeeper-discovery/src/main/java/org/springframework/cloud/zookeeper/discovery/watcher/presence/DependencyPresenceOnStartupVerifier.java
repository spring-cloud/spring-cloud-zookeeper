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
package org.springframework.cloud.zookeeper.discovery.watcher.presence;

import org.apache.curator.x.discovery.ServiceCache;

/**
 * @author Marcin Grzejszczak, 4financeIT
 * @author Tomasz Szymanski, 4financeIT
 */
@SuppressWarnings("unchecked")
public abstract class DependencyPresenceOnStartupVerifier {
	private static final PresenceChecker MANDATORY_DEPENDENCY_CHECKER = new FailOnMissingDependencyChecker();
	private final PresenceChecker optionalDependencyChecker;

	public DependencyPresenceOnStartupVerifier(PresenceChecker optionalDependencyChecker) {
		this.optionalDependencyChecker = optionalDependencyChecker;
	}

	public void verifyDependencyPresence(String dependencyName, ServiceCache serviceCache, boolean required) {
		if (required) {
			MANDATORY_DEPENDENCY_CHECKER.checkPresence(dependencyName, serviceCache.getInstances());
		} else {
			optionalDependencyChecker.checkPresence(dependencyName, serviceCache.getInstances());
		}
	}
}
