package org.springframework.cloud.zookeeper.discovery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wrapper annotation to enable Ribbon for Zookeeper
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnProperty(value = "ribbon.zookeeper.enabled", matchIfMissing = true)
public @interface ConditionalOnRibbonZookeeper {
}
