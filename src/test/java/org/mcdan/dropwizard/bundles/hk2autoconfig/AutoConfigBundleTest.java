package org.mcdan.dropwizard.bundles.hk2autoconfig;

import static org.junit.Assert.assertNotNull;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mcdan.dropwizard.bundles.hk2autoconfig.test.resource.TestResource;
import org.mcdan.dropwizard.bundles.hk2autoconfig.test.service.ServiceObject;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AutoConfigBundleTest {

    private Configuration               config;
    private Bootstrap<Configuration>    bootstrap;
    private Environment                 env;
    private ObjectMapper                objMapper;
    private ResourceConfig              resourceConfig;
    private ServiceObject so = new ServiceObject();

    @Before
    public void setup() {
        config = Mockito.mock(Configuration.class);
        bootstrap = Mockito.mock(Bootstrap.class);
        env = Mockito.mock(Environment.class);
        objMapper = Mockito.mock(ObjectMapper.class);
        Mockito.when(env.getObjectMapper()).thenReturn(objMapper);
        // Setup some mocks so there's no need to load stuff in HK2 at all.
        final JerseyEnvironment jersey = Mockito.mock(JerseyEnvironment.class);
        resourceConfig = Mockito.mock(ResourceConfig.class);
        Mockito.when(env.jersey()).thenReturn(jersey);
        Mockito.when(jersey.getResourceConfig()).thenReturn(resourceConfig);
    }
    
    @After
    public void teardown() {
        Mockito.reset(objMapper);
    }

    @Test
    public void testConstructor() {
        AutoConfigBundle<Configuration> bundle = new AutoConfigBundle<Configuration>(Configuration.class,
                "o.m.d.b.test");
        assertNotNull("Constructing the object should not return null", bundle);
    }

    @Test
    public void testInitialize() {
        AutoConfigBundle<Configuration> bundle = new AutoConfigBundle<Configuration>(Configuration.class,
                "o.m.d.b.test");
        bundle.initialize(bootstrap);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRegisterConfig() throws Exception {
        // Create a fake map to return for the mocked config class.
        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put("username", "foo");
        valueMap.put("password", "bar");
        valueMap.put("logging", "logger");
        Mockito.when(objMapper.convertValue(Mockito.any(Configuration.class), Mockito.eq(Map.class))).thenReturn(
                valueMap);

        AutoConfigBundle<Configuration> bundle = new AutoConfigBundle<Configuration>(Configuration.class,
                "o.m.d.b.test");
        bundle.initialize(bootstrap);
        bundle.run(config, env);
        // Minus one because the logging getter should be removed via the blacklist.

    }

    @Test
    public void testResourceRegistration() throws Exception {
        AutoConfigBundle<Configuration> bundle = new AutoConfigBundle<Configuration>(Configuration.class,
                "org.mcdan.dropwizard.bundles.hk2autoconfig.test.resource");
        final JerseyEnvironment jersey = Mockito.mock(JerseyEnvironment.class);
        Mockito.when(env.jersey()).thenReturn(jersey);
        bundle.initialize(bootstrap);
        bundle.run(config, env);
        InOrder ordered = Mockito.inOrder(jersey, jersey);
        ordered.verify(jersey).register(Mockito.any());
        ordered.verify(jersey).register(Mockito.eq(TestResource.class));
    }

    @Test
    public void testHealthCheckRegistration() throws Exception {
        AutoConfigBundle<Configuration> bundle = new AutoConfigBundle<Configuration>(Configuration.class,
                "org.mcdan.dropwizard.bundles.hk2autoconfig.test.healthcheck");
        final HealthCheckRegistry hcr = Mockito.mock(HealthCheckRegistry.class);
        Mockito.when(env.healthChecks()).thenReturn(hcr);
        bundle.initialize(bootstrap);
        bundle.run(config, env);
        
        Mockito.verify(hcr, Mockito.times(1)).register(Mockito.anyString(), Mockito.any(HealthCheck.class));
    }

    @Test
    public void testServiceBinder() throws Exception {
        AutoConfigBundle<Configuration> bundle = new AutoConfigBundle<Configuration>(Configuration.class,
                "org.mcdan.dropwizard.bundles.hk2autoconfig.test.service");
        final JerseyEnvironment jersey = Mockito.mock(JerseyEnvironment.class);
        Mockito.when(env.jersey()).thenReturn(jersey);
        bundle.initialize(bootstrap);
        bundle.run(config, env);
        
        Mockito.verify(jersey, Mockito.times(2)).register(Mockito.any());
    }

}
