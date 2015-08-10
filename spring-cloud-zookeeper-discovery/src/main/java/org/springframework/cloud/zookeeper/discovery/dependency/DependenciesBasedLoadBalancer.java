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

package org.springframework.cloud.zookeeper.discovery.dependency;

import com.netflix.loadbalancer.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LoadBalancer that delegates to other rules depending on the provided load balancing strategy
 * in the {@link ZookeeperDependencies.ZookeeperDependency#getLoadBalancerType()}
 *
 * @author Marcin Grzejszczak, 4financeIT
 */
@Slf4j
public class DependenciesBasedLoadBalancer extends BaseLoadBalancer {

	private final Map<String, IRule> ruleCache = new ConcurrentHashMap<>();

	private final ZookeeperDependencies zookeeperDependencies;

	public DependenciesBasedLoadBalancer(ZookeeperDependencies zookeeperDependencies, ServerList serverList) {
		this.zookeeperDependencies = zookeeperDependencies;
		setServersList(serverList.getInitialListOfServers());
	}

	@Override
	public Server chooseServer(Object key) {
		String keyAsString = (String) key;
		ZookeeperDependencies.ZookeeperDependency dependency = zookeeperDependencies.getDependencyForAlias(keyAsString);
		if (dependency == null) {
			log.debug("No dependency found for alias [{}] - will use the default rule which is [{}]", keyAsString, rule);
			return rule.choose(key);
		};
		cacheEntryIfMissing(keyAsString, dependency);
		log.debug("Will try to retrieve dependency for key [{}]. Current cache contents [{}]", keyAsString, ruleCache);
		return ruleCache.get(keyAsString).choose(key);
	}

	private void cacheEntryIfMissing(String keyAsString, ZookeeperDependencies.ZookeeperDependency dependency) {
		if (!ruleCache.containsKey(keyAsString)) {
			log.debug("Cache doesn't contain entry for [{}]", keyAsString);
			ruleCache.put(keyAsString, chooseRuleForLoadBalancerType(dependency.getLoadBalancerType()));
		}
	}

	private IRule chooseRuleForLoadBalancerType(LoadBalancerType type) {
		switch (type) {
			case ROUND_ROBIN:
				return getRoundRobinRule();
			case RANDOM:
				return getRandomRule();
			case STICKY:
				return getStickyRule();
			default:
				throw new IllegalArgumentException("Unknown load balancer type " + type);
		}
	}

	private RoundRobinRule getRoundRobinRule() {
		return new RoundRobinRule(this);
	}

	private IRule getRandomRule() {
		RandomRule randomRule = new RandomRule();
		randomRule.setLoadBalancer(this);
		return randomRule;
	}

	private IRule getStickyRule() {
		StickyRule stickyRule = new StickyRule(getRoundRobinRule());
		stickyRule.setLoadBalancer(this);
		return stickyRule;
	}
}
