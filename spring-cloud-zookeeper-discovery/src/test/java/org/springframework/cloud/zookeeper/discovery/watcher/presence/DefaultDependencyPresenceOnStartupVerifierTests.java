/*
 * Copyright 2015-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.zookeeper.discovery.watcher.presence;

import java.util.Collections;

import org.apache.curator.x.discovery.ServiceCache;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * @author Marcin Grzejszczak
 */
public class DefaultDependencyPresenceOnStartupVerifierTests {

	private static final String SERVICE_NAME = "service01";

	@Test
	public void should_throw_exception_if_obligatory_dependencies_are_missing() {
		// given:
		DefaultDependencyPresenceOnStartupVerifier dependencyVerifier = new DefaultDependencyPresenceOnStartupVerifier();
		ServiceCache serviceCache = mock(ServiceCache.class);
		given(serviceCache.getInstances()).willReturn(Collections.emptyList());
		// when:
		try {
			dependencyVerifier.verifyDependencyPresence(SERVICE_NAME, serviceCache, true);
			Assert.fail("Should throw no instances running exception");
		}
		catch (Exception e) {
			// then:
			then(e).isInstanceOf(NoInstancesRunningException.class);
		}
	}

}
