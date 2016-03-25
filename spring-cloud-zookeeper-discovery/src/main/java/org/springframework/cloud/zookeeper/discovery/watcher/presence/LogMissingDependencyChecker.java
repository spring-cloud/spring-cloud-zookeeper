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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.x.discovery.ServiceInstance;

/**
 * Will log the missing microservice dependency
 *
 * @author Marcin Grzejszczak
 * @author Tomasz Dziurko, 4financeIT
 * @since 1.0.0
 */
public class LogMissingDependencyChecker implements PresenceChecker {

	private static final Log log = LogFactory.getLog(LogMissingDependencyChecker.class);

	@Override
	public void checkPresence(String dependencyName, List<ServiceInstance<?>> serviceInstances) {
		if (serviceInstances.isEmpty()) {
			log.warn("Microservice dependency with name [" + dependencyName
					+ "] is missing.");
		}
	}

}
