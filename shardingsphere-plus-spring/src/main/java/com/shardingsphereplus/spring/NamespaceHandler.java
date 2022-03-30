package com.shardingsphereplus.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class NamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("datasource", new DataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("partition", new DataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("debug", new DataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("sharding-algorithm", new DataSourceBeanDefinitionParser());
    }

}
