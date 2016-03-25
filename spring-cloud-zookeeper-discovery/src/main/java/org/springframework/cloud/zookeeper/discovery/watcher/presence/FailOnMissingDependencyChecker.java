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

import java.util.List;

import org.apache.curator.x.discovery.ServiceInstance;

/**
 * Will result in throwing an exception if there are no running instances of the dependency
 *
 * @author Marcin Grzejszczak
 * @author Adam Chudzik, 4financeIT
 * @since 1.0.0
 */
public class FailOnMissingDependencyChecker implements PresenceChecker {
	@Override
	public void checkPresence(String dependencyName, List<ServiceInstance<?>> serviceInstances) {
		if (serviceInstances.isEmpty()) {
			throw new NoInstancesRunningException(dependencyName);
		}
	}

}
