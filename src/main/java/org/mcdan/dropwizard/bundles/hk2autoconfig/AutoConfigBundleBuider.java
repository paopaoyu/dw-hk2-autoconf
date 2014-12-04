package org.mcdan.dropwizard.bundles.hk2autoconfig;

import io.dropwizard.Configuration;

public class AutoConfigBundleBuider<T extends Configuration> {
    private Class<T> klass;
    private String packageName;
    
    public AutoConfigBundleBuider<T> addPackageName(final String packageName) {
        this.packageName = packageName;
        return this;
    }

    public AutoConfigBundleBuider<T> setConfigurationClass(final Class<T> klass) {
        this.klass = klass;
        return this;
    }
    
    public AutoConfigBundle<T> build() {
        return new AutoConfigBundle<T>(klass, packageName);
    }
}
