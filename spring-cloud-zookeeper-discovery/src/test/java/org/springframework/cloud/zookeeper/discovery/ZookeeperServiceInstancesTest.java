package org.springframework.cloud.zookeeper.discovery;

import org.apache.curator.x.discovery.ServiceInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.cloud.zookeeper.discovery.dependency.ZookeeperDependencies;

import java.util.Arrays;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class ZookeeperServiceInstancesTest {

    private final ZookeeperServiceDiscovery serviceDiscovery = mock(ZookeeperServiceDiscovery.class, RETURNS_DEEP_STUBS);
    private final ZookeeperDependencies zookeeperDependencies = mock(ZookeeperDependencies.class);
    private final ZookeeperDiscoveryProperties zookeeperDiscoveryProperties = mock(ZookeeperDiscoveryProperties.class);

    ServiceInstance<ZookeeperInstance> paymentsInstance = mock(ServiceInstance.class);
    ServiceInstance<ZookeeperInstance> reportingInstance = mock(ServiceInstance.class);
    ServiceInstance<ZookeeperInstance> childInstance = mock(ServiceInstance.class);

    private String root;

    public ZookeeperServiceInstancesTest(String root) {
        this.root = root;
    }

    @Parameterized.Parameters(name = "With root {0}")
    public static Iterable<String> rootVariations() {
        return Arrays.asList("es", "es/","/es");
    }

    @Test
    public void should_generate_correct_query_name() throws Exception {
        //given:
        when(zookeeperDependencies.getDependencyNames()).thenReturn(Arrays.asList("/payments", "reporting", "combined-with-children"));
        when(serviceDiscovery.getServiceDiscovery().queryForInstances("/combined-with-children")).thenReturn(null);
        when(serviceDiscovery.getServiceDiscovery().queryForInstances("/payments")).thenReturn(singleton(paymentsInstance));
        when(serviceDiscovery.getServiceDiscovery().queryForInstances("/reporting")).thenReturn(singleton(reportingInstance));
        when(serviceDiscovery.getServiceDiscovery().queryForInstances("/combined-with-children/child")).thenReturn(singleton(childInstance));
        when(serviceDiscovery.getCurator().getChildren().forPath("/es/combined-with-children")).thenReturn(singletonList("child"));
        when(zookeeperDiscoveryProperties.getRoot()).thenReturn(root);

        //when:
        ZookeeperServiceInstances zookeeperServiceInstances =
                new ZookeeperServiceInstances(serviceDiscovery, zookeeperDependencies, zookeeperDiscoveryProperties);

        //then:
        then(zookeeperServiceInstances.iterator()).containsExactly(paymentsInstance, reportingInstance, childInstance);
    }

}