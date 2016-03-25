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
package org.springframework.cloud.zookeeper.discovery.watcher.presence;

import org.apache.curator.x.discovery.ServiceCache;

/**
 * Verifier that checks for presence of mandatory dependencies and delegates to an optional
 * presence checker verification of presence of optional dependencies.
 *
 * The default implementation of required dependencies will result in shutting down of the application
 * if the dependency is missing.
 *
 * @author Marcin Grzejszczak
 * @author Tomasz Szymanski, 4financeIT
 * @version 1.0.0
 *
 * @see FailOnMissingDependencyChecker
 */
public abstract class DependencyPresenceOnStartupVerifier {
	private static final PresenceChecker MANDATORY_DEPENDENCY_CHECKER = new FailOnMissingDependencyChecker();
	private final PresenceChecker optionalDependencyChecker;

	public DependencyPresenceOnStartupVerifier(PresenceChecker optionalDependencyChecker) {
		this.optionalDependencyChecker = optionalDependencyChecker;
	}

	@SuppressWarnings("unchecked")
	public void verifyDependencyPresence(String dependencyName, @SuppressWarnings("rawtypes") ServiceCache serviceCache, boolean required) {
		if (required) {
			MANDATORY_DEPENDENCY_CHECKER.checkPresence(dependencyName, serviceCache.getInstances());
		} else {
			this.optionalDependencyChecker.checkPresence(dependencyName, serviceCache.getInstances());
		}
	}
}
