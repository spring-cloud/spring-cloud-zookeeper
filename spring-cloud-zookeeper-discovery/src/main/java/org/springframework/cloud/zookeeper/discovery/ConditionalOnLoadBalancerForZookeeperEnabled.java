package org.springframework.cloud.zookeeper.discovery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Wrapper annotation to enable Spring Cloud LoadBalancer for Zookeeper.
 *
 * @author Olga Maciaszek-Sharma
 * @since 3.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnProperty(value = "spring.cloud.zookeeper.loadbalancer.enabled", matchIfMissing = true)
public @interface ConditionalOnLoadBalancerForZookeeperEnabled {
}
