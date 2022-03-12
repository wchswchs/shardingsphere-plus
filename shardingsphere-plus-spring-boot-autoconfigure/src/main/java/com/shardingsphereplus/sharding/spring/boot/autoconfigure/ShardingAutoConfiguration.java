package com.shardingsphereplus.sharding.spring.boot.autoconfigure;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Configuration
@EnableConfigurationProperties(ShardingProperties.class)
public class ShardingAutoConfiguration {

    private final ShardingProperties properties;

    public ShardingAutoConfiguration(ShardingProperties properties) {
        this.properties = properties;
    }

    @Bean
    public DataSource dataSource() throws Exception {
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

        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();

        //build database
        String jdbcUrl = properties.getDatasource().getJdbcUrl();
        List<String> jdbcUrlUnits = Splitter.on("//").omitEmptyStrings().splitToList(jdbcUrl);
        String logicSchemaName = jdbcUrlUnits.get(1).split("/")[1].split("\\?")[0];
        String actualDatabases;
        if (properties.getDatasource().getDbPartitionNum() > 1) {
            actualDatabases = String.format("%s%s${0..%d}.%s%s${0..%d}",
                    logicSchemaName,
                    properties.getPartitionJoinDelimiter(),
                    properties.getDatasource().getDbPartitionNum() - 1,
                    properties.getDatasource().getLogicTable(),
                    properties.getPartitionJoinDelimiter(),
                    properties.getDatasource().getTablePartitionNum() - 1);
        } else {
            actualDatabases = String.format("%s.%s%s${0..%d}",
                    logicSchemaName,
                    properties.getDatasource().getLogicTable(),
                    properties.getPartitionJoinDelimiter(),
                    properties.getDatasource().getTablePartitionNum() - 1);
        }
        ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration(
                properties.getDatasource().getLogicTable(),
                actualDatabases
        );

        Map<String, DataSource> dataSourceMap = buildDatasource(logicSchemaName, jdbcUrl);
        //sharding rule config
        ShardingSphereAlgorithmConfiguration shardingSphereAlgorithmConfiguration = new ShardingSphereAlgorithmConfiguration(
                        properties.getAlgorithm().getAlgorithmName(),
                        getShardingProperties(properties.getAlgorithm().getAlgorithmName())
        );
        shardingRuleConfiguration.getShardingAlgorithms().put(properties.getAlgorithm().getAlgorithmName(), shardingSphereAlgorithmConfiguration);
        shardingTableRuleConfiguration.setTableShardingStrategy(new StandardShardingStrategyConfiguration(
                properties.getAlgorithm().getShardingColumn(),
                properties.getAlgorithm().getAlgorithmName()));
        shardingRuleConfiguration.getTables().add(shardingTableRuleConfiguration);

        //build common properties
        Properties commonProperties = new Properties();
        commonProperties.setProperty("sql-show", properties.getSqlShow());

        return ShardingSphereDataSourceFactory.createDataSource(logicSchemaName, dataSourceMap, Collections.singleton(shardingRuleConfiguration), commonProperties);
    }

    private Map<String, DataSource> buildDatasource(String logicSchemaName, String jdbcUrl) {
        Map<String, DataSource> dataSourceMap = new HashMap<>();

        for (int i = 0; i < properties.getDatasource().getDbPartitionNum(); i ++) {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            String schemaName;
            if (properties.getDatasource().getDbPartitionNum() > 1) {
                schemaName = logicSchemaName + properties.getPartitionJoinDelimiter() + i;
                dataSource.setUrl(jdbcUrl.replace(logicSchemaName, schemaName));
            } else {
                schemaName = logicSchemaName;
                dataSource.setUrl(jdbcUrl);
            }
            dataSource.setUsername(properties.getDatasource().getUsername());
            dataSource.setPassword(properties.getDatasource().getPassword());
            dataSourceMap.put(schemaName, dataSource);
        }
        return dataSourceMap;
    }

    private Properties getShardingProperties(String shardingAlgorithmName) throws Exception {
        Class<?> algorithmConfigClazz = Class.forName(properties.getClass().getName() + "$" + shardingAlgorithmName);
        Method algorithmConfigMethod = properties.getAlgorithm().getClass().getMethod("get" + shardingAlgorithmName);
        Object algorithmConfigObj = algorithmConfigMethod.invoke(properties.getAlgorithm());
        Properties shardingAlgorithmProperties = new Properties();
        for (Field field : algorithmConfigClazz.getDeclaredFields()) {
            field.setAccessible(true);
            shardingAlgorithmProperties.setProperty(field.getName(), field.get(algorithmConfigObj).toString());
            shardingAlgorithmProperties.setProperty("partitionJoinDelimiter", properties.getPartitionJoinDelimiter());
        }
        return shardingAlgorithmProperties;
    }

}
