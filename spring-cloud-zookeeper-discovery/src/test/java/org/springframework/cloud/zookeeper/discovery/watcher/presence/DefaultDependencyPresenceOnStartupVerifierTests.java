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

	@Test public void should_throw_exception_if_obligatory_dependencies_are_missing() {
		//given:
		DefaultDependencyPresenceOnStartupVerifier dependencyVerifier = new DefaultDependencyPresenceOnStartupVerifier();
		ServiceCache serviceCache = mock(ServiceCache.class);
		given(serviceCache.getInstances()).willReturn(Collections.emptyList());
		//when:
		try {
			dependencyVerifier.verifyDependencyPresence(SERVICE_NAME, serviceCache, true);
			Assert.fail("Should throw no instances running exception");
		} catch (Exception e) {
			//then:
			then(e).isInstanceOf(NoInstancesRunningException.class);
		}
	}
}
