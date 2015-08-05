package org.springframework.cloud.zookeeper.common

import org.springframework.web.client.RestTemplate

class TestRibbonClient extends TestServiceRestClient {

    private final String thisAppName

    TestRibbonClient(RestTemplate restTemplate) {
        super(restTemplate)
        this.thisAppName = 'someName'
    }

    TestRibbonClient(RestTemplate restTemplate, String thisAppName) {
        super(restTemplate)
        this.thisAppName = thisAppName
    }

    String thisHealthCheck() {
        return restTemplate.getForObject("http://$thisAppName/health", String)
    }

}