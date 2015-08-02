package org.springframework.cloud.zookeeper.discovery.dependency;

import com.netflix.loadbalancer.*;

/**
 * LoadBalancer that delegates to other rules depending on the provided load balancing strategy
 * in the {@link ZookeeperDependencies.ZookeeperDependency#getLoadBalancerType()}
 *
 * @author Marcin Grzejszczak, 4financeIT
 */
public class DependenciesBasedLoadBalancer extends BaseLoadBalancer {

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
			return rule.choose(key);
		} ;
		IRule loadBalancerRule = chooseRuleForLoadBalancerType(dependency.getLoadBalancerType());
		return loadBalancerRule.choose(key);
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
