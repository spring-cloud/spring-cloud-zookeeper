/*
 * Copyright 2015-present the original author or authors.
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

package org.springframework.cloud.zookeeper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.drivers.TracerDriver;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.zookeeper.ClientCnxnSocketNIO;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} that sets up Zookeeper discovery.
 *
 * @author Spencer Gibb
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnZookeeperEnabled
@EnableConfigurationProperties
public class ZookeeperAutoConfiguration {

	private static final Log log = LogFactory.getLog(ZookeeperAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean
	public ZookeeperProperties zookeeperProperties() {
		return new ZookeeperProperties();
	}

	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean
	public CuratorFramework curatorFramework(ZookeeperProperties properties, RetryPolicy retryPolicy,
			ObjectProvider<CuratorFrameworkCustomizer> optionalCuratorFrameworkCustomizerProvider,
			ObjectProvider<EnsembleProvider> optionalEnsembleProvider,
			ObjectProvider<TracerDriver> optionalTracerDriverProvider) throws Exception {
		return CuratorFactory.curatorFramework(properties, retryPolicy,
				optionalCuratorFrameworkCustomizerProvider::orderedStream, optionalEnsembleProvider::getIfAvailable,
				optionalTracerDriverProvider::getIfAvailable);
	}

	@Bean
	@ConditionalOnMissingBean
	public RetryPolicy exponentialBackoffRetry(ZookeeperProperties properties) {
		return CuratorFactory.retryPolicy(properties);
	}

}

// TODO: remove after GraalVM metadata PR merged
class ZookeeperCoreHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		if (!ClassUtils.isPresent("org.apache.zookeeper.ZooKeeper", classLoader)) {
			return;
		}
		hints.reflection().registerType(TypeReference.of(ClientCnxnSocketNIO.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
		hints.reflection()
				.registerType(TypeReference.of("org.apache.curator.x.discovery.details.OldServiceInstance"),
						hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS,
								MemberCategory.INVOKE_DECLARED_METHODS));
		hints.reflection()
				.registerType(TypeReference.of("org.apache.curator.x.discovery.UriSpec"),
						hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS,
								MemberCategory.INVOKE_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(UriSpec.Part.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS,
						MemberCategory.INVOKE_DECLARED_METHODS));
		hints.reflection()
				.registerType(TypeReference.of("org.apache.curator.x.discovery.ServiceInstance"),
						hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS,
								MemberCategory.INVOKE_DECLARED_METHODS));
	}
}
