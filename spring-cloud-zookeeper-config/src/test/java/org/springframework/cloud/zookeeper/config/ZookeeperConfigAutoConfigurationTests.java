package org.springframework.cloud.zookeeper.config;


import org.junit.*;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.ConnectException;

/**
 * @author Cesar Aguilera
 */
public class ZookeeperConfigAutoConfigurationTests {

    @Rule
    public ExpectedException expectedException;

    @Before
    public void setUp() throws Exception {
        expectedException = ExpectedException.none();
        // makes Curator fail faster, otherwise it takes 15 seconds to trigger a retry
        System.setProperty("curator-default-connection-timeout", "0");
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty("curator-default-connection-timeout");
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testConfigEnabledFalseDoesNotLoadZookeeperConfigAutoConfiguration() throws Exception {
        ConfigurableApplicationContext context = new SpringApplicationBuilder()
                .sources(Config.class)
                .web(WebApplicationType.NONE)
                .run(
                        "--spring.application.name=testZookeeperConfigEnabledSetToFalse",
                        "--spring.jmx.default-domain=testZookeeperConfigEnabledSetToFalse",
                        "--spring.cloud.zookeeper.config.connectString=localhost:2188",
                        "--spring.cloud.zookeeper.baseSleepTimeMs=0",
                        "--spring.cloud.zookeeper.maxRetries=0",
                        "--spring.cloud.zookeeper.maxSleepMs=0",
                        "--spring.cloud.zookeeper.blockUntilConnectedWait=0",
                        "--spring.cloud.zookeeper.config.failFast=false",
                        "--spring.cloud.zookeeper.config.enabled=false"
                );


        context.getBean(ZookeeperConfigAutoConfiguration.class);
    }


    @Test
    public void testConfigEnabledTrueLoadsZookeeperConfigAutoConfiguration() throws Exception {
        expectedException.expect(ConnectException.class);

        new SpringApplicationBuilder()
                .sources(Config.class)
                .web(WebApplicationType.NONE)
                .run(
                        "--spring.application.name=testZookeeperConfigEnabledSetToTrue",
                        "--spring.jmx.default-domain=testZookeeperConfigEnabledSetToTrue",
                        "--spring.cloud.zookeeper.config.connectString=localhost:2188",
                        "--spring.cloud.zookeeper.baseSleepTimeMs=0",
                        "--spring.cloud.zookeeper.maxRetries=0",
                        "--spring.cloud.zookeeper.maxSleepMs=0",
                        "--spring.cloud.zookeeper.blockUntilConnectedWait=0",
                        "--spring.cloud.zookeeper.config.failFast=false",
                        "--spring.cloud.zookeeper.config.enabled=true"
                );
    }

    @SpringBootApplication
    static class Config {
    }
}
