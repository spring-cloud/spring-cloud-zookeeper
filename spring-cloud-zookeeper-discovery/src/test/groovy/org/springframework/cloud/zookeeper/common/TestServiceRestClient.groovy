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
package org.springframework.cloud.zookeeper.common

import groovy.transform.CompileStatic
import org.springframework.web.client.RestTemplate

@CompileStatic
class TestServiceRestClient {

	final RestTemplate restTemplate;

	TestServiceRestClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate
	}

	String callService(String alias, String endpoint) {
		return restTemplate.getForObject("http://$alias/$endpoint", String)
	}

	String callOnUrl(String url, String endpoint) {
		return new RestTemplate().getForObject("http://$url/$endpoint", String)
	}
}
