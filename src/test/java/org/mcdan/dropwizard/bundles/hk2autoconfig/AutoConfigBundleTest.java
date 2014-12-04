package org.mcdan.dropwizard.bundles.hk2autoconfig;

import static org.junit.Assert.assertNotNull;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AutoConfigBundleTest {

    private Configuration config;
    private Bootstrap<Configuration> bootstrap;
    private Environment env;
    private ObjectMapper objMapper;
    
    @Before
    public void setup() {
        config = Mockito.mock(Configuration.class);
        bootstrap = Mockito.mock(Bootstrap.class);
        env = Mockito.mock(Environment.class);
        objMapper = Mockito.mock(ObjectMapper.class);
        Mockito.when(env.getObjectMapper()).thenReturn(objMapper);
    }

    @Test
    public void testConstructor() {
        AutoConfigBundle<Configuration> bundle = new AutoConfigBundle<Configuration>(Configuration.class, "org.mcdan.dropwizard.bundles.tests");
        assertNotNull("Constructing the object should not return null", bundle);
    }

    @Test
    public void testInitialize() {
        AutoConfigBundle<Configuration> bundle = new AutoConfigBundle<Configuration>(Configuration.class, "org.mcdan.dropwizard.bundles.tests");
        bundle.initialize(bootstrap);
    }
    
    @Test
    public void testRun() throws Exception {
        AutoConfigBundle<Configuration> bundle = new AutoConfigBundle<Configuration>(Configuration.class, "org.mcdan.dropwizard.bundles.tests");
//        bundle.run(config, env);
    }

}
