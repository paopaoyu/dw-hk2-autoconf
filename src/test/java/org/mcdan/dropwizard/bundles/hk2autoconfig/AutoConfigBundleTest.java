package org.mcdan.dropwizard.bundles.hk2autoconfig;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import io.dropwizard.Configuration;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.apache.commons.beanutils.PropertyUtils;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.mcdan.dropwizard.bundles.hk2autoconfig.test.resource.TestResource;
import org.mockito.Mockito;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AutoConfigBundleTest {

    private Configuration               config;
    private Bootstrap<Configuration>    bootstrap;
    private Environment                 env;
    private ObjectMapper                objMapper;
    private ServiceLocator              locator;
    private DynamicConfigurationService dcs;
    private DynamicConfiguration        dynConfig;

    @Before
    public void setup() {
        config = Mockito.mock(Configuration.class);
        bootstrap = Mockito.mock(Bootstrap.class);
        env = Mockito.mock(Environment.class);
        objMapper = Mockito.mock(ObjectMapper.class);
        Mockito.when(env.getObjectMapper()).thenReturn(objMapper);
        // Setup some mocks so there's no need to load stuff in HK2 at all.
        locator = Mockito.mock(ServiceLocator.class);
        dcs = Mockito.mock(DynamicConfigurationService.class);
        dynConfig = Mockito.mock(DynamicConfiguration.class);
        Mockito.when(locator.getService(Mockito.eq(DynamicConfigurationService.class))).thenReturn(dcs);
        Mockito.when(dcs.createDynamicConfiguration()).thenReturn(dynConfig);

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
    public void testResgisterConfig() throws Exception {
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
        bundle.setLocator(locator);
        bundle.run(config, env);
        // Minus one because the logging getter should be removed via the blacklist.
        Mockito.verify(dynConfig, Mockito.times(valueMap.size() - 1)).addActiveDescriptor(
                Mockito.any(AbstractActiveDescriptor.class));
        Mockito.verify(dynConfig, Mockito.times(1)).commit();

    }

    @Test
    public void testResourceRegistration() throws Exception {
        AutoConfigBundle<Configuration> bundle = new AutoConfigBundle<Configuration>(Configuration.class,
                "org.mcdan.dropwizard.bundles.hk2autoconfig.test.resource");
        final JerseyEnvironment jersey = Mockito.mock(JerseyEnvironment.class);
        Mockito.when(env.jersey()).thenReturn(jersey);
        final TestResource testResource = new TestResource();
        Mockito.when(locator.createAndInitialize(Mockito.eq(TestResource.class))).thenReturn(testResource);
        bundle.initialize(bootstrap);
        bundle.setLocator(locator);
        bundle.run(config, env);
        
        Mockito.verify(jersey, Mockito.times(1)).register(Mockito.eq(testResource));
    }

    @Test
    public void testHealthCheckRegistration() throws Exception {
        AutoConfigBundle<Configuration> bundle = new AutoConfigBundle<Configuration>(Configuration.class,
                "org.mcdan.dropwizard.bundles.hk2autoconfig.test.healthcheck");
        final HealthCheckRegistry hcr = Mockito.mock(HealthCheckRegistry.class);
        Mockito.when(env.healthChecks()).thenReturn(hcr);
        bundle.initialize(bootstrap);
        bundle.setLocator(locator);
        bundle.run(config, env);
        
        Mockito.verify(hcr, Mockito.times(1)).register(Mockito.anyString(), Mockito.any(HealthCheck.class));
    }
}
