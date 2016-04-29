package org.springframework.cloud.zookeeper.discovery.watcher.presence;

import java.util.Collections;

import org.apache.curator.x.discovery.ServiceCache;
import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * @author Marcin Grzejszczak
 */
public class DependencyPresenceOnStartupVerifierTests {

	private static final String SERVICE_NAME = "service01";

	@Test public void should_check_optional_dependency_using_optional_dependency_checker() {
		//given:
		PresenceChecker optionalDependencyChecker = mock(PresenceChecker.class);
		DependencyPresenceOnStartupVerifier dependencyVerifier = new DependencyPresenceOnStartupVerifier(optionalDependencyChecker) {
		};
		ServiceCache serviceCache = mock(ServiceCache.class);
		given(serviceCache.getInstances()).willReturn(Collections.emptyList());
		//when:
		dependencyVerifier.verifyDependencyPresence(SERVICE_NAME, serviceCache, false);
		//then:
		then(optionalDependencyChecker).should().checkPresence(SERVICE_NAME, serviceCache.getInstances());
	}
	
}
