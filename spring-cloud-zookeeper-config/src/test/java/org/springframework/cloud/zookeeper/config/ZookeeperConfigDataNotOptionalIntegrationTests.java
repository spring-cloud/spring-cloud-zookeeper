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

package org.springframework.cloud.zookeeper.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.cloud.context.refresh.ConfigDataContextRefresher;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
public class ZookeeperConfigDataNotOptionalIntegrationTests {

	@Test
	public void configDataNotFoundThrowsException() {
		Assertions.assertThatThrownBy(() -> {
			ConfigurableApplicationContext context = null;
			try {
				context = new SpringApplicationBuilder(Config.class).web(WebApplicationType.NONE).run(
						"--spring.cloud.zookeeper.connect-string=notexistantdomain:5000", "--debug=true",
						"--spring.cloud.zookeeper.max-retries=0",
						"--spring.cloud.zookeeper.blockUntilConnectedWait=1",
						"--spring.cloud.zookeeper.blockUntilConnectedUnit=MILLISECONDS",
						"--spring.cloud.zookeeper.connection-timeout=1ms",
						"--spring.config.import=zookeeper:",
						"--spring.application.name=testZkConfigDataNotOptionalIntegration",
						"--logging.level.org.springframework.cloud.zookeeper=DEBUG",
						"--spring.cloud.zookeeper.config.root=/shouldfail");

			}
			finally {
				if (context != null) {
					context.close();
				}
			}
		}).isInstanceOf(ConfigDataResourceNotFoundException.class);
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config {

		@Bean
		public ContextRefresher contextRefresher(ConfigurableApplicationContext context, RefreshScope scope) {
			return new ConfigDataContextRefresher(context, scope);
		}

	}

}
