package org.springframework.cloud.zookeeper.discovery.dependency;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ported from {@link org.apache.curator.x.discovery.strategies.StickyStrategy}
 *
 * author: Marcin Grzejszczak, 4financeIT
 */
public class StickyRule extends AbstractLoadBalancerRule {
	private final IRule masterStrategy;
	private final AtomicReference<Server> ourInstance = new AtomicReference<>(null);
	private final AtomicInteger instanceNumber = new AtomicInteger(-1);

	public StickyRule(IRule masterStrategy) {
		this.masterStrategy = masterStrategy;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig iClientConfig) {

	}

	@Override
	public Server choose(Object key) {
		final List<Server> instances = getLoadBalancer().getServerList(true);
		Server localOurInstance = ourInstance.get();
		if (!instances.contains(localOurInstance)) {
			ourInstance.compareAndSet(localOurInstance, null);
		}
		if (ourInstance.get() == null) {
			Server instance = masterStrategy.choose(key);
			if (ourInstance.compareAndSet(null, instance)) {
				instanceNumber.incrementAndGet();
			}
		}
		return ourInstance.get();
	}

	/**
	 * Each time a new instance is picked, an internal counter is incremented. This way you
	 * can track when/if the instance changes. The instance can change when the selected instance
	 * is not in the current list of instances returned by the instance provider
	 *
	 * @return instance number
	 */
	public int getInstanceNumber() {
		return instanceNumber.get();
	}

}
