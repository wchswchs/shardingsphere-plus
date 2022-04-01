package com.shardingsphereplus.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class DataSourceNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("datasource", new DataSourceBeanDefinitionParser());
    }

}
