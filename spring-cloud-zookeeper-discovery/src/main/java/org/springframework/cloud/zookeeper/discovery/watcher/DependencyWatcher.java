package org.springframework.cloud.zookeeper.discovery.watcher;

import java.io.IOException;

public interface DependencyWatcher {

    /**
     * Register hooks upon dependencies registration
     *
     * @throws Exception
     */
    void registerDependencyRegistrationHooks() throws Exception;

    /**
     * Unregister hooks upon dependencies registration
     *
     * @throws IOException
     */
    void clearDependencyRegistrationHooks() throws IOException;

    /**
     * Register a listener for a dependency
     *
     * @param listener
     */
    void registerDependencyStateChangeListener(DependencyWatcherListener listener);

    /**
     * Unregister a listener for a dependency
     *
     * @param listener
     */
    void clearDependencyStateChangeListener(DependencyWatcherListener listener);
}
