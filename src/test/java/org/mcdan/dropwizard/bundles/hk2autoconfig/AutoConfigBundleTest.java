package org.mcdan.dropwizard.bundles.hk2autoconfig;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mcdan.dropwizard.bundles.hk2autoconfig.test.service.ServiceConfig;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.Configuration;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class AutoConfigBundleTest {

    private ServiceConfiguration               config;
    private ServiceConfig serviceConfig;
    private Bootstrap<Configuration>    bootstrap;
    private Environment                 env;
    private ObjectMapper                objMapper;
    private JerseyEnvironment jersey;
    
    @Before
    public void setup() {
        config = new ServiceConfiguration();
        serviceConfig = new ServiceConfig();
        serviceConfig.setItValue("hello dw ioc");
        config.setServiceConfig(serviceConfig);
        bootstrap = Mockito.mock(Bootstrap.class);
        env = Mockito.mock(Environment.class);
        objMapper = Mockito.mock(ObjectMapper.class);
        Mockito.when(env.getObjectMapper()).thenReturn(objMapper);
        // Setup some mocks so there's no need to load stuff in HK2 at all.
        jersey = Mockito.mock(JerseyEnvironment.class);
        Mockito.when(env.jersey()).thenReturn(jersey);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                AbstractBinder a = (AbstractBinder) invocation.getArguments()[0];
                if (a != null) {
                    a.bind(Mockito.mock(DynamicConfiguration.class));                    
                }
                return null;
            }
        
        }).when(jersey).register(Mockito.any());

    }
    
    @After
    public void teardown() {
        Mockito.reset(objMapper);
    }

    
    
    

    @Test
    public void testServiceBinder() throws Exception {
        AutoConfigBundle<ServiceConfiguration> bundle = new AutoConfigBundle<ServiceConfiguration>(ServiceConfiguration.class,
                "org.mcdan.dropwizard.bundles.hk2autoconfig.test.service");
        bundle.initialize(bootstrap);
        bundle.run(config, env);
        

        Mockito.verify(jersey, Mockito.times(3)).register(Mockito.any());
    }

}
