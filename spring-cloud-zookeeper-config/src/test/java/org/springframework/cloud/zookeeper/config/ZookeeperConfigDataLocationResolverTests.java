/*
 * Copyright 2015-2020 the original author or authors.
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

package org.springframework.cloud.zookeeper.config;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.BootstrapRegistry;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZookeeperConfigDataLocationResolverTests {

	@Test
	public void testParseLocation() {
		ZookeeperConfigDataLocationResolver resolver = new ZookeeperConfigDataLocationResolver();
		UriComponents uriComponents = resolver.parseLocation(null,
				"zookeeper:myhost:2182/mypath1;/mypath2;/mypath3");
		assertThat(uriComponents.toUri()).hasScheme("zookeeper").hasHost("myhost")
				.hasPort(2182).hasPath("/mypath1;/mypath2;/mypath3");

		uriComponents = resolver.parseLocation(null, "zookeeper:myhost:2182");
		assertThat(uriComponents.toUri()).hasScheme("zookeeper").hasHost("myhost")
				.hasPort(2182).hasPath("");
	}

	@Test
	public void testResolveProfileSpecificWithCustomPaths() {
		String location = "zookeeper:myhost:2182/mypath1;/mypath2;/mypath3";
		List<ZookeeperConfigDataLocation> locations = testResolveProfileSpecific(location);
		assertThat(locations).hasSize(3);
		assertThat(toContexts(locations)).containsExactly("/mypath1", "/mypath2",
				"/mypath3");
	}

	@Test
	public void testResolveProfileSpecificWithAutomaticPaths() {
		String location = "zookeeper:myhost";
		List<ZookeeperConfigDataLocation> locations = testResolveProfileSpecific(location);
		assertThat(locations).hasSize(4);
		assertThat(toContexts(locations)).containsExactly("config/testapp,dev",
				"config/testapp", "config/application,dev", "config/application");
	}

	@Test
	public void testLoadProperties() {
		ZookeeperProperties properties = createResolver().loadProperties(
				Binder.get(new MockEnvironment()),
				UriComponentsBuilder.fromUriString("zookeeper://myhost:8502").build());
		assertThat(properties.getConnectString()).isEqualTo("myhost:8502");
	}

	private List<String> toContexts(List<ZookeeperConfigDataLocation> locations) {
		return locations.stream().map(ZookeeperConfigDataLocation::getContext)
				.collect(Collectors.toList());
	}

	private List<ZookeeperConfigDataLocation> testResolveProfileSpecific(String location) {
		ZookeeperConfigDataLocationResolver resolver = createResolver();

		MockEnvironment env = new MockEnvironment();
		env.setProperty("spring.application.name", "testapp");

		BootstrapRegistry registry = mock(BootstrapRegistry.class);
		when(registry.register(any(), any())).thenReturn(mock(BootstrapRegistry.Registration.class));

		ConfigDataLocationResolverContext context = mock(
				ConfigDataLocationResolverContext.class);

		when(context.getBootstrapRegistry()).thenReturn(registry);
		when(context.getBinder()).thenReturn(Binder.get(env));

		Profiles profiles = mock(Profiles.class);
		when(profiles.getAccepted()).thenReturn(Collections.singletonList("dev"));

		return resolver.resolveProfileSpecific(context, location, false, profiles);
	}

	private ZookeeperConfigDataLocationResolver createResolver() {
		return new ZookeeperConfigDataLocationResolver();
	}

}
