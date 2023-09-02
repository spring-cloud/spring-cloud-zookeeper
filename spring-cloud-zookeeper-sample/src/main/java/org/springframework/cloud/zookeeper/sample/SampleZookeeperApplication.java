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

package org.springframework.cloud.zookeeper.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author Spencer Gibb
 */
@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@RestController
@EnableScheduling
@EnableFeignClients
public class SampleZookeeperApplication {

	@Value("${spring.application.name:testZookeeperApp}")
	private String appName;

	@Autowired
	private LoadBalancerClient loadBalancer;

	@Autowired
	private DiscoveryClient discovery;

	@Autowired
	private Environment env;

	@Autowired
	private AppClient appClient;

	@Autowired
	private ModifyableHealthContributer modifyableHealthContributer;

	@Autowired(required = false)
	private Registration registration;

	private RestTemplate rest;

	@RequestMapping("/")
	public ServiceInstance lb() {
		return this.loadBalancer.choose(this.appName);
	}

	@RequestMapping("/hi")
	public String hi() {
		return "Hello World! from " + this.registration;
	}

	@RequestMapping("/self")
	public String self() {
		return this.appClient.hi();
	}

	@RequestMapping("/myenv")
	public String env(@RequestParam("prop") String prop) {
		return this.env.getProperty(prop, "Not Found");
	}

	@RequestMapping("/up")
	public String up() {
		modifyableHealthContributer.setHealthy(true);
		return "Instance is now marked as healthy.";
	}

	@RequestMapping("/down")
	public String down() {
		modifyableHealthContributer.setHealthy(false);
		return "Instance is now marked as unhealthy.";
	}

	public String rt() {
		return this.rest.getForObject("http://" + this.appName + "/hi", String.class);
	}

	@Bean
	@LoadBalanced
	RestTemplate loadBalancedRestTemplate() {
		this.rest = new RestTemplateBuilder().build();
		return this.rest;
	}

	@Bean
	ModifyableHealthContributer modifyableHealthContributer() {
		return new ModifyableHealthContributer();
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleZookeeperApplication.class, args);
	}

	@FeignClient("testZookeeperApp")
	interface AppClient {

		@RequestMapping(path = "/hi", method = RequestMethod.GET)
		String hi();

	}

}
