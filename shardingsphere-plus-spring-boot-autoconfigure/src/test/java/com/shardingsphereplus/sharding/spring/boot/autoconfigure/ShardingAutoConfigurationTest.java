package com.shardingsphereplus.sharding.spring.boot.autoconfigure;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

public class ShardingAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void init() {
        this.context = new AnnotationConfigApplicationContext();
        TestPropertyValues.of("spring.sharding.datasource.jdbcUrl:jdbc:mysql://localhost:3306/test").applyTo(this.context);
        TestPropertyValues.of("spring.sharding.datasource.logicTable:user").applyTo(this.context);
        TestPropertyValues.of("spring.sharding.datasource.username:root").applyTo(this.context);
        TestPropertyValues.of("spring.sharding.datasource.password:123456").applyTo(this.context);
    }

    @AfterEach
    void closeContext() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    @DisplayName("分片算法配置参数测试")
    public void testAlgorithm() {
        TestPropertyValues.of("spring.sharding.algorithm.StrHash.endIndex:2").applyTo(context);
        context.register(ShardingAutoConfiguration.class);
        context.refresh();
        ContextManager contextManager = context.getBean(ShardingSphereDataSource.class).getContextManager();
        Collection<ShardingSphereRule> rules = contextManager.getMetaDataContexts().getMetaData("test")
                .getRuleMetaData().getRules();
        for (ShardingSphereRule rule : rules) {
            if (rule instanceof ShardingRule) {
                Assertions.assertThat(((ShardingRule) rule).getShardingAlgorithms()
                        .get("StrHash")
                        .getProps().getProperty("startIndex"))
                        .isEqualTo("-1");
                Assertions.assertThat(((ShardingRule) rule).getShardingAlgorithms()
                        .get("StrHash")
                        .getProps().getProperty("endIndex"))
                        .isEqualTo("2");
            }
        }
    }

    @Test
    @DisplayName("分库配置参数测试")
    public void testDbPartitionNum() {
        TestPropertyValues.of("spring.sharding.datasource.dbPartitionNum:2").applyTo(context);
        context.register(ShardingAutoConfiguration.class);
        context.refresh();
        String schemaName = context.getBean(ShardingSphereDataSource.class).getSchemaName();
        Map<String, DataSource> dataSourceMap = context.getBean(ShardingSphereDataSource.class).getContextManager().getDataSourceMap(schemaName);
        int i = 1;
        for (final Map.Entry<String, DataSource> dataSourceEntry : dataSourceMap.entrySet()) {
            Assertions.assertThat(dataSourceEntry.getKey()).isEqualTo("test_" + i);
            i --;
        }
    }

    @Test
    @DisplayName("无配置参数测试")
    public void testNon() {
        context.register(ShardingAutoConfiguration.class);
        context.refresh();
        ContextManager contextManager = context.getBean(ShardingSphereDataSource.class).getContextManager();
        Collection<ShardingSphereRule> rules = contextManager.getMetaDataContexts().getMetaData("test")
                .getRuleMetaData().getRules();
        for (ShardingSphereRule rule : rules) {
            if (rule instanceof ShardingRule) {
                Assertions.assertThat(((ShardingRule) rule).getShardingAlgorithms()
                        .get("StrHash")
                        .getProps().getProperty("startIndex"))
                        .isEqualTo("-1");
                Assertions.assertThat(((ShardingRule) rule).getShardingAlgorithms()
                        .get("StrHash")
                        .getProps().getProperty("endIndex"))
                        .isEqualTo("-1");
            }
        }
    }

}
