package org.springframework.cloud.zookeeper.discovery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.Arrays;

import static org.assertj.core.api.BDDAssertions.then;

@RunWith(Parameterized.class)
public class ZookeeperDiscoveryPropertiesTest {

    private String root;

    public ZookeeperDiscoveryPropertiesTest(String root) {
        this.root = root;
    }

    @Parameterized.Parameters(name = "With root {0}")
    public static Iterable<String> rootVariations() {
        return Arrays.asList("es", "es/","/es");
    }

    @Test
    public void should_escape_root() {
        // given:
        ZookeeperDiscoveryProperties zookeeperDiscoveryProperties = new ZookeeperDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
        // when:
        zookeeperDiscoveryProperties.setRoot(root);
        // then:
        then(zookeeperDiscoveryProperties.getRoot()).isEqualTo("/es");
    }

}
