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

package org.springframework.cloud.zookeeper.sample;

import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class SampleApplicationTests {

	@Test public void contextLoads() throws Exception {
		TestingServer server = new TestingServer(2181);

		ConfigurableApplicationContext context = new SpringApplicationBuilder(SampleZookeeperApplication.class).run("--server.port=0");

		context.close();
		server.close();
	}
}
