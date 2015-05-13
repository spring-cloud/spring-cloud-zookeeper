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

import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.cloud.zookeeper.discovery.watcher.presence.NoInstancesRunningException;
import org.springframework.cloud.zookeeper.discovery.watcher.presence.PresenceChecker;

import java.util.List;

/**
 * @author Marcin Grzejszczak, 4financeIT
 * @author Adam Chudzik, 4financeIT
 */
public class FailOnMissingDependencyChecker implements PresenceChecker {
	@Override
	public void checkPresence(String dependencyName, List<ServiceInstance> serviceInstances) {
		if (serviceInstances.isEmpty()) {
			throw new NoInstancesRunningException(dependencyName);
		}
	}

}
