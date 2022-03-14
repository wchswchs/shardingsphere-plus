package com.shardingsphereplus.sharding.spring.boot.autoconfigure;

import com.shardingsphereplus.sharding.lib.algorithm.standard.StrHashAlgorithm;
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
        TestPropertyValues.of("spring.sharding.datasource.dbAddress:localhost:3306").applyTo(this.context);
        TestPropertyValues.of("spring.sharding.datasource.logicDbName:test").applyTo(this.context);
        TestPropertyValues.of("spring.sharding.datasource.logicTable:user").applyTo(this.context);
        TestPropertyValues.of("spring.sharding.datasource.username:root").applyTo(this.context);
        TestPropertyValues.of("spring.sharding.datasource.password:123456").applyTo(this.context);
        TestPropertyValues.of("spring.sharding.algorithm.shardingColumn:user_name").applyTo(context);
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
        TestPropertyValues.of("spring.sharding.algorithm.shardingTableAlgorithmName:StrHash[endIndex:2]").applyTo(context);
        context.register(ShardingAutoConfiguration.class);
        context.refresh();
        ContextManager contextManager = context.getBean(ShardingSphereDataSource.class).getContextManager();
        Collection<ShardingSphereRule> rules = contextManager.getMetaDataContexts().getMetaData("test")
                .getRuleMetaData().getRules();
        for (ShardingSphereRule rule : rules) {
            if (rule instanceof ShardingRule) {
                Assertions.assertThat(
                        ((StrHashAlgorithm)((ShardingRule) rule).getShardingAlgorithms()
                        .get("StrHash")).getStartIndex())
                        .isEqualTo(-1);
                Assertions.assertThat(
                        ((StrHashAlgorithm)((ShardingRule) rule).getShardingAlgorithms()
                        .get("StrHash")).getEndIndex())
                        .isEqualTo(2);
            }
        }
    }

    @Test
    @DisplayName("分库配置参数测试")
    public void testDbPartitionNum() {
        TestPropertyValues.of("spring.sharding.datasource.dbAddress:localhost:3306,localhost:3307").applyTo(this.context);
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
    @DisplayName("默认分片配置参数测试")
    public void testDefaultSharding() {
        context.register(ShardingAutoConfiguration.class);
        context.refresh();
        ContextManager contextManager = context.getBean(ShardingSphereDataSource.class).getContextManager();
        Collection<ShardingSphereRule> rules = contextManager.getMetaDataContexts().getMetaData("test")
                .getRuleMetaData().getRules();
        for (ShardingSphereRule rule : rules) {
            if (rule instanceof ShardingRule) {
                Assertions.assertThat(
                        ((StrHashAlgorithm)((ShardingRule) rule).getShardingAlgorithms()
                        .get("StrHash")).getStartIndex())
                        .isEqualTo(-1);
                Assertions.assertThat(
                        ((StrHashAlgorithm)((ShardingRule) rule).getShardingAlgorithms()
                        .get("StrHash")).getEndIndex())
                        .isEqualTo(-1);
            }
        }
    }

    @Test
    @DisplayName("多表不同分片测试")
    public void testMultiTableSharding() {
        TestPropertyValues.of("spring.sharding.datasource.logicTable:user,user_token").applyTo(context);
        TestPropertyValues.of("spring.sharding.algorithm.shardingTableAlgorithmName:user->StrHash[starIndex:1|endIndex:2],user_token->StrHash").applyTo(context);
        TestPropertyValues.of("spring.sharding.algorithm.shardingColumn:user->user_name,user_token->token").applyTo(context);
        context.register(ShardingAutoConfiguration.class);
        context.refresh();
        ContextManager contextManager = context.getBean(ShardingSphereDataSource.class).getContextManager();
        Collection<ShardingSphereRule> rules = contextManager.getMetaDataContexts().getMetaData("test")
                .getRuleMetaData().getRules();
        for (ShardingSphereRule rule : rules) {
            if (rule instanceof ShardingRule) {
                Assertions.assertThat(((ShardingRule) rule).getTableRules().size()).isEqualTo(2);
            }
        }
    }

    @Test
    @DisplayName("多表同一分片测试")
    public void testMultiTableSameSharding() {
        TestPropertyValues.of("spring.sharding.datasource.logicTable:user,user_token").applyTo(context);
        TestPropertyValues.of("spring.sharding.algorithm.shardingTableAlgorithmName:StrHash[starIndex:1|endIndex:2]").applyTo(context);
        TestPropertyValues.of("spring.sharding.algorithm.shardingColumn:user->user_name,user_token->token").applyTo(context);
        context.register(ShardingAutoConfiguration.class);
        context.refresh();
        ContextManager contextManager = context.getBean(ShardingSphereDataSource.class).getContextManager();
        Collection<ShardingSphereRule> rules = contextManager.getMetaDataContexts().getMetaData("test")
                .getRuleMetaData().getRules();
        for (ShardingSphereRule rule : rules) {
            if (rule instanceof ShardingRule) {
                Assertions.assertThat(((ShardingRule) rule).getTableRules().size()).isEqualTo(2);
            }
        }
    }

    @Test
    @DisplayName("多表不同分片数测试")
    public void testMultiTableDiffPartitionNum() {
        TestPropertyValues.of("spring.sharding.datasource.logicTable:user,user_token").applyTo(context);
        TestPropertyValues.of("spring.sharding.algorithm.shardingTableAlgorithmName:StrHash[starIndex:1|endIndex:2]").applyTo(context);
        TestPropertyValues.of("spring.sharding.algorithm.shardingColumn:user->user_name,user_token->token").applyTo(context);
        TestPropertyValues.of("spring.sharding.datasource.tablePartitionNum:user->16,user_token->32").applyTo(context);
        context.register(ShardingAutoConfiguration.class);
        context.refresh();
        ContextManager contextManager = context.getBean(ShardingSphereDataSource.class).getContextManager();
        Collection<ShardingSphereRule> rules = contextManager.getMetaDataContexts().getMetaData("test")
                .getRuleMetaData().getRules();
        for (ShardingSphereRule rule : rules) {
            if (rule instanceof ShardingRule) {
                Assertions.assertThat(((ShardingRule) rule).getTableRules().get("user").getActualDataNodes().size()).isEqualTo(16);
                Assertions.assertThat(((ShardingRule) rule).getTableRules().get("user_token").getActualDataNodes().size()).isEqualTo(32);
            }
        }
    }

    @Test
    @DisplayName("多表同一分片数测试")
    public void testMultiTableSamePartitionNum() {
        TestPropertyValues.of("spring.sharding.datasource.logicTable:user,user_token").applyTo(context);
        TestPropertyValues.of("spring.sharding.algorithm.shardingTableAlgorithmName:StrHash[starIndex:1|endIndex:2]").applyTo(context);
        TestPropertyValues.of("spring.sharding.algorithm.shardingColumn:user->user_name,user_token->token").applyTo(context);
        TestPropertyValues.of("spring.sharding.datasource.tablePartitionNum:32").applyTo(context);
        context.register(ShardingAutoConfiguration.class);
        context.refresh();
        ContextManager contextManager = context.getBean(ShardingSphereDataSource.class).getContextManager();
        Collection<ShardingSphereRule> rules = contextManager.getMetaDataContexts().getMetaData("test")
                .getRuleMetaData().getRules();
        for (ShardingSphereRule rule : rules) {
            if (rule instanceof ShardingRule) {
                Assertions.assertThat(((ShardingRule) rule).getTableRules().get("user").getActualDataNodes().size()).isEqualTo(32);
                Assertions.assertThat(((ShardingRule) rule).getTableRules().get("user_token").getActualDataNodes().size()).isEqualTo(32);
            }
        }
    }

}
