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
		String url = "http://" + alias +"/" + endpoint;
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
