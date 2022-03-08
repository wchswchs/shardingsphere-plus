package com.shardingbox.sharding.spring.boot.autoconfigure;

import com.alibaba.druid.pool.DruidDataSource;
import com.shardingbox.sharding.lib.algorithm.standard.StrHashAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(ShardingProperties.class)
public class ShardingAutoConfiguration {

    private final ShardingProperties properties;
    private final ApplicationContext context;

    public ShardingAutoConfiguration(ShardingProperties properties, ApplicationContext context) {
        this.properties = properties;
        this.context = context;
    }

    @Bean("strHash")
    public StrHashAlgorithm strHashAlgorithm() {
        return new StrHashAlgorithm(
                properties.getAlgorithm().getStrHash().getStartIndex(),
                properties.getAlgorithm().getStrHash().getEndIndex(),
                properties.getTableJoinDelimiter()
        );
    }

    @Bean
    public DataSource dataSource() throws Exception {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        DruidDataSource dataSource = new DruidDataSource();

        if (StringUtils.isEmpty(properties.getDatasource().getJdbcUrl())) {
            throw new IllegalArgumentException("configuration item spring.sharding.datasource.jdbcUrl can not be empty");
        }
        if (StringUtils.isEmpty(properties.getDatasource().getLogicTable())) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.datasource.logicTable can not be empty"
            );
        }
        if (StringUtils.isEmpty(properties.getDatasource().getUsername())) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.datasource.username can not be empty"
            );
        }
        if (StringUtils.isEmpty(properties.getDatasource().getPassword())) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.datasource.password can not be empty"
            );
        }
        if (properties.getDatasource().getDbPartitionNum() < 1) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.datasource.tablePartitionNum can not lower than 1"
            );
        }
        if (properties.getDatasource().getTablePartitionNum() < 1) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.datasource.dbPartitionNum can not lower than 1"
            );
        }

        //basic datasource config
        for (int i = 0; i < properties.getDatasource().getDbPartitionNum(); i ++) {
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl(properties.getDatasource().getJdbcUrl());
            dataSource.setUsername(properties.getDatasource().getUsername());
            dataSource.setPassword(properties.getDatasource().getPassword());
            dataSourceMap.put("ds"+i, dataSource);
        }

        //sharding table strategy config
        ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration(
                properties.getDatasource().getLogicTable(),
                String.format("ds${0..%d}.%s${0..%d}",
                        properties.getDatasource().getDbPartitionNum() - 1,
                        properties.getDatasource().getLogicTable(),
                        properties.getDatasource().getTablePartitionNum() - 1)
        );
        try {
            Object algorithmBean = context.getBean(properties.getAlgorithm().getAlgorithmName());
            if (!(algorithmBean instanceof ShardingAlgorithm)) {
                throw new IllegalArgumentException(
                        "configuration item spring.sharding.algorithm.algorithmName not exist"
                );
            }
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.algorithm.algorithmName not exist"
            );
        }
        shardingTableRuleConfiguration.setTableShardingStrategy(new StandardShardingStrategyConfiguration(
                properties.getAlgorithm().getShardingColumn(),
                properties.getAlgorithm().getAlgorithmName()
        ));

        //sharding rule config
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getTables().add(shardingTableRuleConfiguration);

        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shardingRuleConfiguration), new Properties());
    }

}
