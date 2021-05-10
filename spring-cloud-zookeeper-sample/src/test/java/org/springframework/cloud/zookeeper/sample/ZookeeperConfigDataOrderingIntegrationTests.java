/*
 * Copyright 2018-2021 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.KeeperException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.cloud.zookeeper.config.ZookeeperConfigProperties;
import org.springframework.cloud.zookeeper.test.ZookeeperTestingServer;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = SampleZookeeperApplication.class,
		properties = { "spring.application.name=" + ZookeeperConfigDataOrderingIntegrationTests.APP_NAME,
				"spring.config.name=orderingtest", "spring.profiles.active=dev",
				"management.endpoints.web.exposure.include=*" },
		webEnvironment = RANDOM_PORT)
public class ZookeeperConfigDataOrderingIntegrationTests {

	private static final String BASE_PATH = new WebEndpointProperties().getBasePath();

	static final String APP_NAME = "testZkConfigDataOrderingIntegration";

	private static final String PREFIX = "_configDataOrderingIntegrationTests_config__";

	private static final String ROOT = "/" + PREFIX + UUID.randomUUID();

	private static final String VALUE = "my value from zk default profile";

	private static final String TEST_PROP = "my.prop";

	private static final String KEY = ROOT + "/" + APP_NAME + "/" + TEST_PROP;

	private static final String VALUE_PROFILE = "my value from zk dev profile";

	private static final String KEY_PROFILE = ROOT + "/" + APP_NAME + ",dev/" + TEST_PROP;
	private static ZookeeperTestingServer testingServer;
	private static CuratorFramework curator;

	@Autowired
	private Environment env;

	@BeforeAll
	public static void initialize() throws Exception {
		testingServer = new ZookeeperTestingServer();
		testingServer.start();
		System.setProperty(ZookeeperProperties.PREFIX + ".connect-string", "localhost:" + testingServer.getPort());
		System.setProperty(ZookeeperConfigProperties.PREFIX + ".root", ROOT);
		String connectString = "localhost:" + testingServer.getPort();
		curator = CuratorFrameworkFactory.builder()
				.retryPolicy(new RetryOneTime(500)).connectString(connectString).build();
		curator.start();
		List<String> children = curator.getChildren().forPath("/");
		for (String child : children) {
			if (child.startsWith(PREFIX) && child.length() > PREFIX.length()) {
				delete("/" + child);
			}
		}

		StringBuilder create = new StringBuilder(1024);
		create.append(curator.create().creatingParentsIfNeeded()
				.forPath(KEY, VALUE.getBytes())).append('\n');
		create.append(curator.create().creatingParentsIfNeeded()
				.forPath(KEY_PROFILE, VALUE_PROFILE.getBytes())).append('\n');
		curator.close();
		System.out.println(create);
	}

	public static void delete(String path) throws Exception {
		try {
			if (curator.getState() == CuratorFrameworkState.STARTED) {
				curator.delete().deletingChildrenIfNeeded().forPath(path);
			}
		}
		catch (KeeperException e) {
			if (e.code() != KeeperException.Code.NONODE) {
				throw e;
			}
		}
	}
	@AfterAll
	public static void close() throws Exception {
		try {
			delete(ROOT);
		}
		finally {
			testingServer.close();
		}
		System.clearProperty(ZookeeperProperties.PREFIX + ".connect-string");
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void contextLoads() {
		Integer port = env.getProperty("local.server.port", Integer.class);
		ResponseEntity<Map> response = new TestRestTemplate()
				.getForEntity("http://localhost:" + port + BASE_PATH + "/env/my.prop", Map.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Map res = response.getBody();
		assertThat(res).containsKey("propertySources");
		Map<String, Object> property = (Map<String, Object>) res.get("property");
		assertThat(property).containsEntry("value", VALUE_PROFILE);
	}

}
