package org.springframework.cloud.zookeeper.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;

/**
 * An {@link Iterable} representing registered Zookeeper instances. If using
 * {@link ZookeeperDependencies} it will return a list of registered Zookeeper instances
 * corresponding to the ones defined in the dependencies.
 *
 * @since 1.0.0
 */
public class ZookeeperServiceInstances implements Iterable<ServiceInstance<ZookeeperInstance>> {

	private static final Log log = LogFactory.getLog(ZookeeperServiceInstances.class);

	private final ZookeeperServiceDiscovery serviceDiscovery;
	private final ZookeeperDependencies zookeeperDependencies;
	private final ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;
	private final List<ServiceInstance<ZookeeperInstance>> allInstances;

	public ZookeeperServiceInstances(ZookeeperServiceDiscovery serviceDiscovery, ZookeeperDependencies zookeeperDependencies,
			ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
		this.serviceDiscovery = serviceDiscovery;
		this.zookeeperDependencies = zookeeperDependencies;
		this.zookeeperDiscoveryProperties = zookeeperDiscoveryProperties;
		this.allInstances = getZookeeperInstances();
	}

	private List<ServiceInstance<ZookeeperInstance>> getZookeeperInstances() {
		ArrayList<ServiceInstance<ZookeeperInstance>> allInstances = new ArrayList<>();
		try {
			for (String name : getNamesToQuery()) {
				allInstances.addAll(nestedInstances(allInstances, sanitizePath(name)));
			}
			return allInstances;
		}
		catch (Exception e) {
			log.debug("Exception occurred while trying to build the list of instances", e);
			return allInstances;
		}
	}

	private List<ServiceInstance<ZookeeperInstance>> nestedInstances(
			List<ServiceInstance<ZookeeperInstance>> accumulator, String name) throws Exception {
		String parentPath = prepareQueryName(name);
		Collection<ServiceInstance<ZookeeperInstance>> childrenInstances = tryToGetInstances(parentPath);
		if (childrenInstances != null) {
			return convertCollectionToList(childrenInstances);
		}
		try {
			List<String> children = this.serviceDiscovery.getCurator().getChildren().forPath(parentPath);
			return iterateOverChildren(accumulator, parentPath, children);
		} catch (Exception e) {
			log.trace("Exception occurred while trying to retrieve children of [" + parentPath + "]", e);
			return injectZookeeperServiceInstances(accumulator, parentPath);
		}
	}

	private String prepareQueryName(String name) {
		return name.startsWith(getDiscoveryRoot()) ? name : getDiscoveryRoot() + name;
	}

	private String getDiscoveryRoot() {
		return sanitizePath(this.zookeeperDiscoveryProperties.getRoot());
	}

	private Collection<ServiceInstance<ZookeeperInstance>> tryToGetInstances(String path) {
		try {
			return this.serviceDiscovery.getServiceDiscovery().queryForInstances(getPathWithoutRoot(path));
		} catch (Exception e) {
			log.trace("Exception occurred while trying to retrieve instances of [" + path + "]", e);
			return null;
		}
	}

	private String getPathWithoutRoot(String path) {
		return path.substring(getDiscoveryRoot().length());
	}

	private List<ServiceInstance<ZookeeperInstance>> injectZookeeperServiceInstances(
			List<ServiceInstance<ZookeeperInstance>> accumulator, String name) throws Exception {
		Collection<ServiceInstance<ZookeeperInstance>> instances = this.serviceDiscovery
				.getServiceDiscovery().queryForInstances(name);
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
			List<ServiceInstance<ZookeeperInstance>> accumulator,
			String parentPath, List<String> children) throws Exception {
		List<ServiceInstance<ZookeeperInstance>> lists = new ArrayList<>();
		for (String child : children) {
			lists.addAll(nestedInstances(accumulator, parentPath + "/" + child));
		}
		return lists;
	}

	private Collection<String> getNamesToQuery() throws Exception {
		if (this.zookeeperDependencies == null) {
			return this.serviceDiscovery.getServiceDiscovery().queryForNames();
		}
		return this.zookeeperDependencies.getDependencyNames();
	}

	@Override
	public Iterator<ServiceInstance<ZookeeperInstance>> iterator() {
		return this.allInstances.iterator();
	}

	private String sanitizePath(String value) {
		return withLeadingSlash(withoutSlashAtEnd(value));
	}

	private String withLeadingSlash(String value) {
		return value.startsWith("/") ? value : "/" + value;
	}

	private String withoutSlashAtEnd(String value) {
		return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}
}
