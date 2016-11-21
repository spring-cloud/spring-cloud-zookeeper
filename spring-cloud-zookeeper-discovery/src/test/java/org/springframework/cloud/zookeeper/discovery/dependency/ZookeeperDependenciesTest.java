package org.springframework.cloud.zookeeper.discovery.dependency;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;

public class ZookeeperDependenciesTest {

    @Test
    public void should_properly_sanitize_dependency_path() {
        // given:
        Map<String, ZookeeperDependency> dependencies = new LinkedHashMap<>();
        ZookeeperDependency cat = new ZookeeperDependency();
        cat.setPath("/cats/cat");
        dependencies.put("cat", cat);
        ZookeeperDependency dog = new ZookeeperDependency();
        dog.setPath("dogs/dog");
        dependencies.put("dog", dog);
        ZookeeperDependencies zookeeperDependencies = new ZookeeperDependencies();
        zookeeperDependencies.setDependencies(dependencies);
        // when:
        zookeeperDependencies.init();
        // then:
        then(zookeeperDependencies.getDependencies().get("cat").getPath()).isEqualTo("/cats/cat");
        then(zookeeperDependencies.getDependencies().get("dog").getPath()).isEqualTo("/dogs/dog");
    }

    @Test
    public void should_properly_sanitize_dependency_path_with_prefix() {
        // given:
        Map<String, ZookeeperDependency> dependencies = new LinkedHashMap<>();
        ZookeeperDependency cat = new ZookeeperDependency();
        cat.setPath("/cats/cat");
        dependencies.put("cat", cat);
        ZookeeperDependency dog = new ZookeeperDependency();
        dog.setPath("dogs/dog");
        dependencies.put("dog", dog);
        ZookeeperDependencies zookeeperDependencies = new ZookeeperDependencies();
        zookeeperDependencies.setPrefix("animals/");
        zookeeperDependencies.setDependencies(dependencies);
        // when:
        zookeeperDependencies.init();
        // then:
        then(zookeeperDependencies.getDependencies().get("cat").getPath()).isEqualTo("/animals/cats/cat");
        then(zookeeperDependencies.getDependencies().get("dog").getPath()).isEqualTo("/animals/dogs/dog");
    }

}