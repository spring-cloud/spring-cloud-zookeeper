/*
 * Copyright 2012-2015 the original author or authors.
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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configuration that registers a bean related to microservice's address and port providing.
 *
 * @see MicroserviceAddressProvider
 *
 * @author Marcin Grzejszczak, 4financeIT
 * @author Tomasz Dziurko, 4financeIT
 */
@Configuration
public class AddressProviderConfiguration {

	@Autowired
	private Environment environment;

	@Autowired
	private ZookeeperDiscoveryProperties properties;

	public AddressProviderConfiguration(Environment environment,
			ZookeeperDiscoveryProperties properties) {
		this.environment = environment;
		this.properties = properties;
	}

	@Bean
	public MicroserviceAddressProvider microserviceAddressProvider() {
		String host = properties.getInstanceHost() == null? getIpAddress() : properties.getInstanceHost();
		Integer port = Integer.valueOf(environment.getProperty("server.port", "8080"));
		return new MicroserviceAddressProvider(host, port);
	}

	/**
	 * Return a non loopback IPv4 address for the machine running this process.
	 * If the machine has multiple network interfaces, the IP address for the
	 * first interface returned by {@link java.net.NetworkInterface#getNetworkInterfaces}
	 * is returned.
	 *
	 * @return non loopback IPv4 address for the machine running this process
	 * @see java.net.NetworkInterface#getNetworkInterfaces
	 * @see java.net.NetworkInterface#getInetAddresses
	 */
	public static String getIpAddress() {
		try {
			for (Enumeration<NetworkInterface> enumNic = NetworkInterface.getNetworkInterfaces();
				 enumNic.hasMoreElements(); ) {
				NetworkInterface ifc = enumNic.nextElement();
				if (ifc.isUp()) {
					for (Enumeration<InetAddress> enumAddr = ifc.getInetAddresses();
						 enumAddr.hasMoreElements(); ) {
						InetAddress address = enumAddr.nextElement();
						if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
							return address.getHostAddress();
						}
					}
				}
			}
		} catch (IOException e) {
			// ignore
		}
		return "unknown";
	}

}
