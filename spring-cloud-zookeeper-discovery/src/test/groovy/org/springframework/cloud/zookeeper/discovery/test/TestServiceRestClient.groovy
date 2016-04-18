/*
 * Copyright 2013-2015 the original author or authors.
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
package org.springframework.cloud.zookeeper.discovery.test

import groovy.transform.CompileStatic
import groovy.util.logging.Commons
import org.springframework.web.client.RestTemplate

@CompileStatic
@Commons
class TestServiceRestClient {

	final RestTemplate restTemplate;

	TestServiceRestClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate
	}

	public <T> T callService(String alias, String endpoint, Class<T> clazz) {
		String url = "http://$alias/$endpoint"
		log.info("Calling [$url]")
		return restTemplate.getForObject(url, clazz)
	}

	String callService(String alias, String endpoint) {
		return callService(alias, endpoint, String)
	}

	String callOnUrl(String url, String endpoint) {
		return new RestTemplate().getForObject("http://$url/$endpoint", String)
	}
}
