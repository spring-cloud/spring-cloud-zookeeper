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
