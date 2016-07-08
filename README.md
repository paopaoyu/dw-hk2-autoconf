##如何更好的在dropWizard使用IOC容器

###dropwizard介绍
>dropWizard是由Yammer开发团队贡献的一个后台服务开发框架，其集成了Java生态系统中各个问题域中最优秀的组件，帮助开发者快速的打造一个Rest风格的后台服务。 
    
>对开发者来说，使用DropWizard有如下好处： 

- 和Maven集成良好，也就是说和Gradle集成也很良好； 
- 开发迅速，部署简单； 
- 代码结构好，可读性高； 
- 自动为服务提供OM框架； 
- 让开发者自然的把一个应用拆分为一个个的小服务 
####dropwizard的缺点
> 在实际的使用过程中，也遇到了一些使用上的不便，特别是在现在由微服务所构成的系统中，往往需要使用到各种中间件，如：mq，分布式缓存和分布式数据库等，但是各个中间件客户端的初始化过程却存在着很大的差别，如初始化activemq的（参考[https://github.com/mbknor/dropwizard-activemq-bundle](https://github.com/mbknor/dropwizard-activemq-bundle)）

    private ActiveMQBundle activeMQBundle;

    @Override
    public void initialize(Bootstrap<Config> configBootstrap) {

        // Create the bundle and store reference to it
        this.activeMQBundle = new ActiveMQBundle();
        // Add the bundle
        configBootstrap.addBundle(activeMQBundle);
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {

        final String queueName = config.getQueueName();

        // Set up the sender for our queue
        ActiveMQSender sender = activeMQBundle.createSender( queueName, false);

        // Set up a receiver that just logs the messages we receive on our queue
        activeMQBundle.registerReceiver(
                queueName,
                (animal) -> log.info("\n*****\nWe received an animal from activeMq: \n{}\n*****", animal),
                Animal.class,
                true);

        // Create our resource
        environment.jersey().register( new AnimalResource(sender) );
    }
>如果只是一个中间件的初始化，可能还会觉得没有太大的问题，但是如果要使用多个中间件，如果依赖的中间件客户端的初始化过程发现改变，虽然其使用接口并没有改变，则相关的微服务模块也都需要进行修改。而理想情况下，我们所开发的微服务，应该只会受其接口变化的影响。

###将dropwizard与IOC容器结合

目前dropwizard已经有了可以和spring和guice这两个著名的ioc容器相关结合的开源实现，但是由于dropwizard使用到了Jersey，而Jersey中就已经含有了一个HK2的IOC容器，[dw-hk2-autoconf](https://github.com/mcdan/dw-hk2-autoconf)就是这样的一个项目，但是直接使用这个项目的实现，并不能完全达到实现一种“微内核+插件”的架构模式，其根本原因就是没有将dropwizard的Configuration对象进行相关的拆解，注入到HK2的IOC容器中，已经将dw-hk2-autoconf fork出来，增加了将Configuration进行分析的过程，基本就能实现“微内核+插件”的架构模式，主要的修改是在[AutoConfigBundle.java](https://github.com/paopaoyu/dw-hk2-autoconf/blob/master/src/main/java/org/mcdan/dropwizard/bundles/hk2autoconfig/AutoConfigBundle.java)中增加了registerSubConfigurationProvider方法