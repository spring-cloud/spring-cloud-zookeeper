/*
 * Copyright 2015-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.zookeeper.discovery.test;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.web.client.RestTemplate;

/**
 * @author Marcin Grzejszczak
 */
public class TestLoadBalancedClient extends TestServiceRestClient {

	public static final String BASE_PATH = new WebEndpointProperties().getBasePath();

	private final String thisAppName;

	public TestLoadBalancedClient(RestTemplate restTemplate) {
		super(restTemplate);
		this.thisAppName = "someName";
	}

	public TestLoadBalancedClient(RestTemplate restTemplate, String thisAppName) {
		super(restTemplate);
		this.thisAppName = thisAppName;
	}

	public String thisHealthCheck() {
		return this.restTemplate.getForObject(
				"http://" + this.thisAppName + BASE_PATH + "/health", String.class);
	}

	public Integer thisPort() {
		return this.restTemplate.getForObject("http://" + this.thisAppName + "/port",
				Integer.class);
	}

}
