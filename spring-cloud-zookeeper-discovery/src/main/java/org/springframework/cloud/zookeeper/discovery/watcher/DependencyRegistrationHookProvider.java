/*
 * Copyright 2013-2018 the original author or authors.
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
package org.springframework.cloud.zookeeper.discovery.watcher;

import java.io.IOException;

/**
 * Implementations of this interface are required to register dependency registration hooks
 * on startup and their cleaning upon application context shutdown.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public interface DependencyRegistrationHookProvider {

	/**
	 * Register hooks upon dependencies registration.
	 *
	 * @throws Exception in case an error occurs while registering the dependency registration hooks
	 */
	void registerDependencyRegistrationHooks() throws Exception;

	/**
	 * Unregister hooks upon dependencies registration.
	 *
	 * @throws IOException in case an error occurs while removing the dependency registration hooks
	 */
	void clearDependencyRegistrationHooks() throws IOException;

}
