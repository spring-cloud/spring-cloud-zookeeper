package org.springframework.cloud.zookeeper.discovery.dependency;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to turn on a feature if Zookeeper dependencies have been passed
 *
 * @author Marcin Grzejszczak, 4financeIT
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(DependenciesPassedCondition.class)
@ConditionalOnProperty(value = "spring.cloud.zookeeper.dependencies.enabled", matchIfMissing = true)
public @interface ConditionalOnDependenciesPassed {
}
