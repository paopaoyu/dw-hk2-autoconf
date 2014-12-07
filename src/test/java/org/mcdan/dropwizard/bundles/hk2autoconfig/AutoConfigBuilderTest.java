package org.mcdan.dropwizard.bundles.hk2autoconfig;

import io.dropwizard.Configuration;

import org.junit.Assert;
import org.junit.Test;

public class AutoConfigBuilderTest {

    @Test
    public void testBuilder() {
        AutoConfigBundle<Configuration> bundle = AutoConfigBundle.<Configuration> newBuilder().addPackageName("foobar")
                .setConfigurationClass(Configuration.class).build();
        Assert.assertNotNull(bundle);
    }

}
