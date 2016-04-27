package org.springframework.cloud.zookeeper.discovery.test;

import org.springframework.web.client.RestTemplate;

/**
 * @author Marcin Grzejszczak
 */
public class TestRibbonClient extends TestServiceRestClient {

	private final String thisAppName;

	TestRibbonClient(RestTemplate restTemplate) {
		super(restTemplate);
		this.thisAppName = "someName";
	}

	TestRibbonClient(RestTemplate restTemplate, String thisAppName) {
		super(restTemplate);
		this.thisAppName = thisAppName;
	}

	String thisHealthCheck() {
		return this.restTemplate
				.getForObject("http://" + this.thisAppName + "/health", String.class);
	}

	Integer thisPort() {
		return this.restTemplate
				.getForObject("http://" + this.thisAppName + "/port", Integer.class);
	}
}
