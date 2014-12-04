package org.mcdan.dropwizard.bundles.hk2autoconfig;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.BindingBuilderFactory;
import org.glassfish.hk2.utilities.binding.NamedBindingBuilder;
import org.glassfish.hk2.utilities.binding.ServiceBindingBuilder;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AutoResourceConfigBundle<T extends Configuration> implements ConfiguredBundle<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AutoResourceConfigBundle.class);
    private ServiceLocator      locator;
    private Class<T>            klass;
    private Reflections         reflections;

    public AutoResourceConfigBundle(final Class<T> clazz, final String packageName) {
        this.klass = clazz;
        FilterBuilder filterBuilder = new FilterBuilder();
        filterBuilder.include(FilterBuilder.prefix(packageName));

        ConfigurationBuilder reflectionCfg = new ConfigurationBuilder();
        reflectionCfg.addUrls(ClasspathHelper.forPackage(packageName));
        reflectionCfg.filterInputsBy(filterBuilder).setScanners(new SubTypesScanner(), new TypeAnnotationsScanner());
        reflections = new Reflections(reflectionCfg);
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        LOG.warn("INIT of the bundle");
        locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
    }

    @Override
    public void run(final T configuration, final Environment environment) throws Exception {
        Map<String,Object> configMap = environment.getObjectMapper().convertValue(configuration, Map.class);
        registerConfigurationProvider(configuration, configMap);
        registerResources(environment);
        registerHealthChecks(environment);
    }

    private void registerConfigurationProvider(final T configuration, final Map<String, Object> configMap) {
        final Set<String> blackListedConfigAttribute = new HashSet<String>();
        blackListedConfigAttribute.addAll(Arrays.asList("logging", "server", "metrics"));
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration dynConfig = dcs.createDynamicConfiguration();
        ServiceBindingBuilder<T> binder = BindingBuilderFactory.newFactoryBinder(new FactoryWrapper(configuration)).to(
                this.klass);
        BindingBuilderFactory.addBinding(binder, dynConfig);
        
        final String configNamePrefix = "config.";
        for (String key : configMap.keySet()) {
            if (blackListedConfigAttribute.contains(key)) {
                continue;
            }
            Object o = configMap.get(key);
            AbstractActiveDescriptor<?> s = BuilderHelper.createConstantDescriptor(o, configNamePrefix + key, o.getClass());
            dynConfig.addActiveDescriptor(s);            
        }
//        AbstractActiveDescriptor<String> s = BuilderHelper.createConstantDescriptor("foobar?", "config.test", String.class);
        dynConfig.commit();
    }

    private void registerResources(final Environment environment) {
        Set<Class<? extends Object>> resourceClasses = reflections.getTypesAnnotatedWith(Path.class);
        Object resource;
        for (Class<?> resourceClass : resourceClasses) {
            resource = locator.createAndInitialize(resourceClass);
            environment.jersey().register(resource);
        }
    }

    private void registerHealthChecks(final Environment env) {
        Set<Class<? extends HealthCheck>> healthCheckClasses = reflections.getSubTypesOf(HealthCheck.class);
        for (Class<? extends HealthCheck> healthCheckKlass : healthCheckClasses) {
            env.healthChecks().register(healthCheckKlass.getName(), locator.createAndInitialize(healthCheckKlass));
        }
    }

    class FactoryWrapper implements Factory<T> {
        private final T instance;

        public FactoryWrapper(final T object) {
            this.instance = object;
        }

        @Override
        public void dispose(T arg0) {
        }

        @Override
        public T provide() {
            return this.instance;
        }
    }
}
