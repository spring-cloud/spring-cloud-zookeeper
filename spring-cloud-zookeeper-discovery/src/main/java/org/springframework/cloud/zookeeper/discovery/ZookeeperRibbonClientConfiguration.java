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

package org.springframework.cloud.zookeeper.discovery;

import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.ServerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.zookeeper.discovery.dependency.ConditionalOnDependenciesPassed;
import org.springframework.cloud.zookeeper.discovery.dependency.DependenciesBasedLoadBalancer;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static com.netflix.client.config.CommonClientConfigKey.DeploymentContextBasedVipAddresses;
import static com.netflix.client.config.CommonClientConfigKey.EnableZoneAffinity;

/**
 * Preprocessor that configures defaults for zookeeper-discovered ribbon clients. Such as:
 * <code>@zone</code>, NIWSServerListClassName, DeploymentContextBasedVipAddresses,
 * NFLoadBalancerRuleClassName, NIWSServerListFilterClassName and more
 *
 * @author Spencer Gibb
 * @author Dave Syer
 * @author Marcin Grzejszczak, 4financeIT
 */
@Configuration
public class ZookeeperRibbonClientConfiguration {
	protected static final String VALUE_NOT_SET = "__not__set__";
	protected static final String DEFAULT_NAMESPACE = "ribbon";

	@Autowired
	private ZookeeperServiceDiscovery serviceDiscovery;

	@Value("${ribbon.client.name}")
	private String serviceId = "client";

	public ZookeeperRibbonClientConfiguration() {
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnDependenciesPassed
	public ServerList<?> ribbonServerListFromDependencies(IClientConfig config, ZookeeperDependencies zookeeperDependencies) {
		ZookeeperServerList serverList = new ZookeeperServerList(serviceDiscovery.getServiceDiscovery());
		serverList.initFromDependencies(config, zookeeperDependencies);
		return serverList;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnDependenciesPassed
	@ConditionalOnProperty(value = "spring.cloud.zookeeper.dependencies.ribbon.loadbalancer", matchIfMissing = true)
	public ILoadBalancer dependenciesBasedLoadBalancer(ZookeeperDependencies zookeeperDependencies, ServerList serverList) {
		return new DependenciesBasedLoadBalancer(zookeeperDependencies, serverList);
	}

	@Bean
	@ConditionalOnMissingBean
	public ServerList<?> ribbonServerList(IClientConfig config) {
		ZookeeperServerList serverList = new ZookeeperServerList(serviceDiscovery.getServiceDiscovery());
		serverList.initWithNiwsConfig(config);
		return serverList;
	}

	@PostConstruct
	public void preprocess() {
		setProp(this.serviceId, DeploymentContextBasedVipAddresses.key(), this.serviceId);
		setProp(this.serviceId, EnableZoneAffinity.key(), "true");
	}

	protected void setProp(String serviceId, String suffix, String value) {
		// how to set the namespace properly?
		String key = getKey(serviceId, suffix);
		DynamicStringProperty property = getProperty(key);
		if (property.get().equals(VALUE_NOT_SET)) {
			ConfigurationManager.getConfigInstance().setProperty(key, value);
		}
	}

	protected DynamicStringProperty getProperty(String key) {
		return DynamicPropertyFactory.getInstance().getStringProperty(key, VALUE_NOT_SET);
	}

	protected String getKey(String serviceId, String suffix) {
		return serviceId + "." + DEFAULT_NAMESPACE + "." + suffix;
	}

}
