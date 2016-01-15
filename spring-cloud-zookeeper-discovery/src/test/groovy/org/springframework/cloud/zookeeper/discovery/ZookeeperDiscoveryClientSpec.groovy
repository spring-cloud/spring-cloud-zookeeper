package org.springframework.cloud.zookeeper.discovery

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class ZookeeperDiscoveryClientSpec extends Specification {

	def "should return an empty list of services if service discovery is null"() {
		given:
			ZookeeperServiceDiscovery serviceDiscovery = Stub()
			ZookeeperDiscoveryClient zookeeperDiscoveryClient = new ZookeeperDiscoveryClient(serviceDiscovery, null)
		when:
			List<String> services = zookeeperDiscoveryClient.getServices()
		then:
			assert services.empty
	}
}

