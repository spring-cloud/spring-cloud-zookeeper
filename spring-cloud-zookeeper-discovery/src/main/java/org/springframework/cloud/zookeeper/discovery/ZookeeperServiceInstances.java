package org.springframework.cloud.zookeeper.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;

import static org.springframework.cloud.zookeeper.discovery.DependencyPathUtils.sanitize;

/**
 * An {@link Iterable} representing registered Zookeeper instances. If using
 * {@link ZookeeperDependencies} it will return a list of registered Zookeeper instances
 * corresponding to the ones defined in the dependencies.
 *
 * @since 1.0.0
 */
public class ZookeeperServiceInstances
		implements Iterable<ServiceInstance<ZookeeperInstance>> {

	private static final Log log = LogFactory.getLog(ZookeeperServiceInstances.class);

	private ServiceDiscovery<ZookeeperInstance> serviceDiscovery;
	private final ZookeeperDependencies zookeeperDependencies;
	private final ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;
	private final List<ServiceInstance<ZookeeperInstance>> allInstances;
	private final CuratorFramework curator;

	public ZookeeperServiceInstances(CuratorFramework curator,
			ServiceDiscovery<ZookeeperInstance> serviceDiscovery,
			ZookeeperDependencies zookeeperDependencies,
			ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
		this.curator = curator;
		this.serviceDiscovery = serviceDiscovery;
		this.zookeeperDependencies = zookeeperDependencies;
		this.zookeeperDiscoveryProperties = zookeeperDiscoveryProperties;
		this.allInstances = getZookeeperInstances();
	}

	private List<ServiceInstance<ZookeeperInstance>> getZookeeperInstances() {
		ArrayList<ServiceInstance<ZookeeperInstance>> allInstances = new ArrayList<>();
		try {
			Collection<String> namesToQuery = getNamesToQuery();
			if (log.isDebugEnabled()) {
				log.debug("Querying the following names [" + namesToQuery + "]");
			}
			for (String name : namesToQuery) {
				allInstances.addAll(nestedInstances(allInstances, name));
			}
			return allInstances;
		}
		catch (Exception e) {
			log.debug("Exception occurred while trying to build the list of instances",
					e);
			return allInstances;
		}
	}

	private List<ServiceInstance<ZookeeperInstance>> nestedInstances(
			List<ServiceInstance<ZookeeperInstance>> accumulator, String name)
			throws Exception {
		String parentPath = prepareQueryName(name);
		Collection<ServiceInstance<ZookeeperInstance>> childrenInstances = tryToGetInstances(
				parentPath);
		if (childrenInstances != null) {
			return convertCollectionToList(childrenInstances);
		}
		try {
			List<String> children = this.curator.getChildren().forPath(parentPath);
			return iterateOverChildren(accumulator, parentPath, children);
		} catch (Exception e) {
			if (log.isTraceEnabled()) {
				log.trace("Exception occurred while trying to retrieve children of [" + parentPath + "]", e);
			}
			return injectZookeeperServiceInstances(accumulator, parentPath);
		}
	}

	private String prepareQueryName(String name) {
		String root = this.zookeeperDiscoveryProperties.getRoot();
		return name.startsWith(root) ? name : root + name;
	}

	private Collection<ServiceInstance<ZookeeperInstance>> tryToGetInstances(
			String path) {
		try {
			return getServiceDiscovery()
					.queryForInstances(getPathWithoutRoot(path));
		}
		catch (Exception e) {
			log.trace("Exception occurred while trying to retrieve instances of [" + path
					+ "]", e);
			return null;
		}
	}

	private ServiceDiscovery<ZookeeperInstance> getServiceDiscovery() {
		return this.serviceDiscovery;
	}

	private String getPathWithoutRoot(String path) {
		return path.substring(this.zookeeperDiscoveryProperties.getRoot().length());
	}

	private List<ServiceInstance<ZookeeperInstance>> injectZookeeperServiceInstances(
			List<ServiceInstance<ZookeeperInstance>> accumulator, String name)
			throws Exception {
		Collection<ServiceInstance<ZookeeperInstance>> instances = getServiceDiscovery().queryForInstances(name);
		accumulator.addAll(convertCollectionToList(instances));
		return accumulator;
	}

	private List<ServiceInstance<ZookeeperInstance>> convertCollectionToList(
			Collection<ServiceInstance<ZookeeperInstance>> instances) {
		List<ServiceInstance<ZookeeperInstance>> serviceInstances = new ArrayList<>();
		for (ServiceInstance<ZookeeperInstance> instance : instances) {
			serviceInstances.add(instance);
		}
		return serviceInstances;
	}

	private List<ServiceInstance<ZookeeperInstance>> iterateOverChildren(
			List<ServiceInstance<ZookeeperInstance>> accumulator, String parentPath,
			List<String> children) throws Exception {
		List<ServiceInstance<ZookeeperInstance>> lists = new ArrayList<>();
		for (String child : children) {
			lists.addAll(nestedInstances(accumulator, parentPath + "/" + child));
		}
		return lists;
	}

	private Collection<String> getNamesToQuery() throws Exception {
		if (this.zookeeperDependencies == null) {
			if (log.isDebugEnabled()) {
				log.debug("Using direct name resolution instead of dependency based one");
			}
			List<String> names = new ArrayList<>();
			for (String name : getServiceDiscovery().queryForNames()) {
				names.add(sanitize(name));
			}
			return names;
		}
		if (log.isDebugEnabled()) {
			log.debug("Using dependency based names to query");
		}
		return this.zookeeperDependencies.getDependencyNames();
	}

	@Override
	public Iterator<ServiceInstance<ZookeeperInstance>> iterator() {
		return this.allInstances.iterator();
	}

}
