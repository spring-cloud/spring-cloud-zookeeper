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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.WatcherRemoveCuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetACLBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetConfigBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.api.ReconfigBuilder;
import org.apache.curator.framework.api.RemoveWatchesBuilder;
import org.apache.curator.framework.api.SetACLBuilder;
import org.apache.curator.framework.api.SetDataBuilder;
import org.apache.curator.framework.api.SyncBuilder;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.api.transaction.CuratorMultiTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.schema.SchemaSet;
import org.apache.curator.framework.state.ConnectionStateErrorPolicy;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.server.quorum.flexible.QuorumVerifier;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.cloud.zookeeper.ConditionalOnZookeeperEnabled;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

@Configuration(proxyBeanMethods = false)
@ConditionalOnZookeeperEnabled
@ConditionalOnProperty(value = "spring.cloud.zookeeper.config.enabled", matchIfMissing = true)
// this means its ConfigData, not bootstrap
@ConditionalOnMissingBean(ZookeeperPropertySourceLocator.class)
@AutoConfigureBefore(ZookeeperAutoConfiguration.class)
public class ZookeeperConfigDataAutoConfiguration {

	@Bean
	public CuratorFramework configDataCuratorFramework(ConfigurableEnvironment env) {
		List<PropertySource<?>> sources = env.getPropertySources().stream()
				.filter(propertySource -> propertySource instanceof ZookeeperPropertySource)
				.collect(Collectors.toList());
		List<String> contexts = sources.stream()
				.map(propertySource -> ((ZookeeperPropertySource) propertySource).getContext())
				.collect(Collectors.toList());
		CuratorFramework source = (CuratorFramework) sources.get(0).getSource();
		return new SpringCloudCuratorFramework(source, contexts);
	}

	static class SpringCloudCuratorFramework implements CuratorFramework {

		private final CuratorFramework delegate;
		private final List<String> contexts;

		public SpringCloudCuratorFramework(CuratorFramework delegate, List<String> contexts) {
			this.delegate = delegate;
			this.contexts = contexts;
		}

		public CuratorFramework getDelegate() {
			return this.delegate;
		}

		public List<String> getContexts() {
			return this.contexts;
		}

		@Override
		public void start() {
			delegate.start();
		}

		@Override
		public void close() {
			delegate.close();
		}

		@Override
		public CuratorFrameworkState getState() {
			return delegate.getState();
		}

		@Override
		@Deprecated
		public boolean isStarted() {
			return delegate.isStarted();
		}

		@Override
		public CreateBuilder create() {
			return delegate.create();
		}

		@Override
		public DeleteBuilder delete() {
			return delegate.delete();
		}

		@Override
		public ExistsBuilder checkExists() {
			return delegate.checkExists();
		}

		@Override
		public GetDataBuilder getData() {
			return delegate.getData();
		}

		@Override
		public SetDataBuilder setData() {
			return delegate.setData();
		}

		@Override
		public GetChildrenBuilder getChildren() {
			return delegate.getChildren();
		}

		@Override
		public GetACLBuilder getACL() {
			return delegate.getACL();
		}

		@Override
		public SetACLBuilder setACL() {
			return delegate.setACL();
		}

		@Override
		public ReconfigBuilder reconfig() {
			return delegate.reconfig();
		}

		@Override
		public GetConfigBuilder getConfig() {
			return delegate.getConfig();
		}

		@Override
		public CuratorTransaction inTransaction() {
			return delegate.inTransaction();
		}

		@Override
		public CuratorMultiTransaction transaction() {
			return delegate.transaction();
		}

		@Override
		public TransactionOp transactionOp() {
			return delegate.transactionOp();
		}

		@Override
		@Deprecated
		public void sync(String path, Object backgroundContextObject) {
			delegate.sync(path, backgroundContextObject);
		}

		@Override
		public void createContainers(String path) throws Exception {
			delegate.createContainers(path);
		}

		@Override
		public SyncBuilder sync() {
			return delegate.sync();
		}

		@Override
		public RemoveWatchesBuilder watches() {
			return delegate.watches();
		}

		@Override
		public Listenable<ConnectionStateListener> getConnectionStateListenable() {
			return delegate.getConnectionStateListenable();
		}

		@Override
		public Listenable<CuratorListener> getCuratorListenable() {
			return delegate.getCuratorListenable();
		}

		@Override
		public Listenable<UnhandledErrorListener> getUnhandledErrorListenable() {
			return delegate.getUnhandledErrorListenable();
		}

		@Override
		@Deprecated
		public CuratorFramework nonNamespaceView() {
			return delegate.nonNamespaceView();
		}

		@Override
		public CuratorFramework usingNamespace(String newNamespace) {
			return delegate.usingNamespace(newNamespace);
		}

		@Override
		public String getNamespace() {
			return delegate.getNamespace();
		}

		@Override
		public CuratorZookeeperClient getZookeeperClient() {
			return delegate.getZookeeperClient();
		}

		@Override
		@Deprecated
		public EnsurePath newNamespaceAwareEnsurePath(String path) {
			return delegate.newNamespaceAwareEnsurePath(path);
		}

		@Override
		@Deprecated
		public void clearWatcherReferences(Watcher watcher) {
			delegate.clearWatcherReferences(watcher);
		}

		@Override
		public boolean blockUntilConnected(int maxWaitTime, TimeUnit units) throws InterruptedException {
			return delegate.blockUntilConnected(maxWaitTime, units);
		}

		@Override
		public void blockUntilConnected() throws InterruptedException {
			delegate.blockUntilConnected();
		}

		@Override
		public WatcherRemoveCuratorFramework newWatcherRemoveCuratorFramework() {
			return delegate.newWatcherRemoveCuratorFramework();
		}

		@Override
		public ConnectionStateErrorPolicy getConnectionStateErrorPolicy() {
			return delegate.getConnectionStateErrorPolicy();
		}

		@Override
		public QuorumVerifier getCurrentConfig() {
			return delegate.getCurrentConfig();
		}

		@Override
		public SchemaSet getSchemaSet() {
			return delegate.getSchemaSet();
		}

		@Override
		public boolean isZk34CompatibilityMode() {
			return delegate.isZk34CompatibilityMode();
		}

		@Override
		public CompletableFuture<Void> postSafeNotify(Object monitorHolder) {
			return delegate.postSafeNotify(monitorHolder);
		}

		@Override
		public CompletableFuture<Void> runSafe(Runnable runnable) {
			return delegate.runSafe(runnable);
		}

	}

}
