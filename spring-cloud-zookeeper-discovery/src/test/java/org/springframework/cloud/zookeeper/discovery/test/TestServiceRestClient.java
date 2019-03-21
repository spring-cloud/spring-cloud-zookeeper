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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.client.RestTemplate;

/**
 * @author Marcin Grzejszczak
 */
public class TestServiceRestClient {

	private static final Log log = LogFactory.getLog(TestServiceRestClient.class);

	protected final RestTemplate restTemplate;

	public TestServiceRestClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public <T> T callService(String alias, String endpoint, Class<T> clazz) {
		String url = "http://" + alias + "/" + endpoint;
		log.info("Calling [" + url + "]");
		return this.restTemplate.getForObject(url, clazz);
	}

	public String callService(String alias, String endpoint) {
		return callService(alias, endpoint, String.class);
	}

	public String callOnUrl(String url, String endpoint) {
		if (!endpoint.startsWith("/")) {
			endpoint = "/" + endpoint;
		}

		return new RestTemplate().getForObject("http://" + url + endpoint, String.class);
	}

}
