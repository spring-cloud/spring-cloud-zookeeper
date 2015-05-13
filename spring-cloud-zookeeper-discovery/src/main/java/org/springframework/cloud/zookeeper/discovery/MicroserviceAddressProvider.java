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

/**
 * Holder for microservice's host and port
 *
 * @author Marcin Grzejszczak, 4financeIT
 * @author Adam Chudzik, 4financeIT
 *
 */
public class MicroserviceAddressProvider {
	private final String host;
	private final int port;

	public MicroserviceAddressProvider(String microserviceHost, int microservicePort) {
		this.host = microserviceHost;
		this.port = microservicePort;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}