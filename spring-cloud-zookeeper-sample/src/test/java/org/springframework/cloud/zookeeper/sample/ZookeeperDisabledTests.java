/*
 * Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.zookeeper.sample;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.cloud.zookeeper.config.ZookeeperConfigAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.RibbonZookeeperAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.dependency.DependencyFeignClientAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.dependency.DependencyRestTemplateAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.dependency.DependencyRibbonAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependenciesAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.watcher.DependencyWatcherAutoConfiguration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperAutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistryAutoConfiguration;
import org.springframework.cloud.zookeeper.support.CuratorServiceDiscoveryAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleZookeeperApplication.class, webEnvironment = RANDOM_PORT,
		properties = "spring.cloud.zookeeper.enabled=false")
public class ZookeeperDisabledTests {

	@Autowired(required = false)
	private ZookeeperAutoConfiguration zookeeperAutoConfiguration;

	@Autowired(required = false)
	private ZookeeperConfigAutoConfiguration zookeeperConfigAutoConfiguration;

	@Autowired(required = false)
	private RibbonZookeeperAutoConfiguration ribbonZookeeperAutoConfiguration;

	@Autowired(required = false)
	private ZookeeperDiscoveryAutoConfiguration zookeeperDiscoveryAutoConfiguration;

	@Autowired(required = false)
	private DependencyFeignClientAutoConfiguration dependencyFeignClientAutoConfiguration;

	@Autowired(required = false)
	private DependencyRibbonAutoConfiguration dependencyRibbonAutoConfiguration;

	@Autowired(required = false)
	private DependencyRestTemplateAutoConfiguration dependencyRestTemplateAutoConfiguration;

	@Autowired(required = false)
	private ZookeeperDependenciesAutoConfiguration zookeeperDependenciesAutoConfiguration;

	@Autowired(required = false)
	private DependencyWatcherAutoConfiguration dependencyWatcherAutoConfiguration;

	@Autowired(required = false)
	private ZookeeperAutoServiceRegistrationAutoConfiguration zookeeperAutoServiceRegistrationAutoConfiguration;

	@Autowired(required = false)
	private ZookeeperServiceRegistryAutoConfiguration zookeeperServiceRegistryAutoConfiguration;

	@Autowired(required = false)
	private CuratorServiceDiscoveryAutoConfiguration curatorServiceDiscoveryAutoConfiguration;


	@Test
	public void allPartsOfZookeeperDisabled() throws Exception {
		assertThat(this.zookeeperAutoConfiguration).as("ZookeeperAutoConfiguration was not disabled").isNull();
		assertThat(this.zookeeperConfigAutoConfiguration).as("ZookeeperConfigAutoConfiguration was not disabled").isNull();
		assertThat(this.ribbonZookeeperAutoConfiguration).as("RibbonZookeeperAutoConfiguration was not disabled").isNull();
		assertThat(this.zookeeperDiscoveryAutoConfiguration).as("ZookeeperDiscoveryAutoConfiguration was not disabled").isNull();
		assertThat(this.dependencyFeignClientAutoConfiguration).as("DependencyFeignClientAutoConfiguration was not disabled").isNull();
		assertThat(this.dependencyRibbonAutoConfiguration).as("DependencyRibbonAutoConfiguration was not disabled").isNull();
		assertThat(this.dependencyRestTemplateAutoConfiguration).as("DependencyRestTemplateAutoConfiguration was not disabled").isNull();
		assertThat(this.zookeeperDependenciesAutoConfiguration).as("ZookeeperDependenciesAutoConfiguration was not disabled").isNull();
		assertThat(this.dependencyWatcherAutoConfiguration).as("DependencyWatcherAutoConfiguration was not disabled").isNull();
		assertThat(this.zookeeperAutoServiceRegistrationAutoConfiguration).as("ZookeeperAutoServiceRegistrationAutoConfiguration was not disabled").isNull();
		assertThat(this.zookeeperServiceRegistryAutoConfiguration).as("ZookeeperServiceRegistryAutoConfiguration was not disabled").isNull();
		assertThat(this.curatorServiceDiscoveryAutoConfiguration).as("CuratorServiceDiscoveryAutoConfiguration was not disabled").isNull();

	}
}
