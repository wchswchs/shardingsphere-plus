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

        String jdbcUrl = properties.getDatasource().getJdbcUrl();
        List<String> jdbcUrlUnits = Splitter.on("//").omitEmptyStrings().splitToList(jdbcUrl);
        String logicSchemaName = jdbcUrlUnits.get(1).split("/")[1].split("\\?")[0];
        String[] logicTables = properties.getDatasource().getLogicTable().split(",");

        Map<String, DataSource> dataSourceMap = buildDatasource(logicSchemaName, jdbcUrl);
        Map<String, String> usedShardingAlgorithm = new HashMap<>();
        buildUsedShardingAlgorithms(logicTables, usedShardingAlgorithm);
        registerTableRule(
                getShardingAlgorithmMapping(logicTables),
                getShardingColumnMapping(logicTables),
                getShardingActualNodeMapping(logicTables, logicSchemaName),
                logicTables, shardingRuleConfiguration);
        registerShardingAlgorithms(usedShardingAlgorithm, shardingRuleConfiguration);

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

    private void registerTableRule(Map<String, String> shardingAlgorithmMap,
                                   Map<String, String> shardingColumnMap,
                                   Map<String, String> shardingActualNodeMap,
                                   String[] logicTables,
                                   ShardingRuleConfiguration shardingRuleConfiguration) {
        for (String logicTable : logicTables) {
            shardingRuleConfiguration.getTables().add(
                    buildTableRuleConfiguration(logicTable, shardingActualNodeMap.get(logicTable),
                            shardingAlgorithmMap.get(logicTable),
                            shardingColumnMap.get(logicTable))
            );
        }
    }

    private ShardingTableRuleConfiguration buildTableRuleConfiguration(String logicTable, String actualNodes,
                                                                       String shardingAlgorithm, String shardingColumn) {
        ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration(
                logicTable,
                actualNodes
        );
        shardingTableRuleConfiguration.setTableShardingStrategy(new StandardShardingStrategyConfiguration(
                shardingColumn,
                shardingAlgorithm)
        );
        return shardingTableRuleConfiguration;
    }

    private void registerShardingAlgorithms(Map<String, String> usedShardingAlgorithm,
                                            ShardingRuleConfiguration shardingRuleConfiguration) throws Exception {
        for (final Map.Entry<String, String> shardingAlgorithm : usedShardingAlgorithm.entrySet()) {
            ShardingSphereAlgorithmConfiguration shardingSphereAlgorithmConfiguration =
                    new ShardingSphereAlgorithmConfiguration(
                            shardingAlgorithm.getKey(),
                            getShardingProperties(shardingAlgorithm.getValue())
                    );
            shardingRuleConfiguration.getShardingAlgorithms().put(shardingAlgorithm.getKey(), shardingSphereAlgorithmConfiguration);
        }
    }

    private Properties getShardingProperties(String shardingAlgorithmPropertiesStr) {
        Properties shardingAlgorithmProperties = new Properties();
        if (StringUtils.isNotEmpty(shardingAlgorithmPropertiesStr)) {
            for (final Map.Entry<String, String> property : Splitter.on("|").withKeyValueSeparator(":").split(shardingAlgorithmPropertiesStr).entrySet()) {
                shardingAlgorithmProperties.setProperty(property.getKey(), property.getValue());
                shardingAlgorithmProperties.setProperty("partitionJoinDelimiter", properties.getPartitionJoinDelimiter());
            }
        }
        return shardingAlgorithmProperties;
    }

    private Map<String, String> getShardingAlgorithmMapping(String[] logicTables) {
        Map<String, String> shardingAlgorithmMap = new HashMap<>();
        if (logicTables.length > 1) {
            Map<String, String> shardingAlgorithmMapping = Splitter.on(",").omitEmptyStrings().withKeyValueSeparator("->")
                    .split(properties.getAlgorithm().getAlgorithmName());
            for (final Map.Entry<String, String> shardingAlgorithm : shardingAlgorithmMapping.entrySet()) {
                shardingAlgorithmMap.put(shardingAlgorithm.getKey(), shardingAlgorithm.getValue().split("\\[")[0]);
            }
        } else {
            shardingAlgorithmMap.put(properties.getDatasource().getLogicTable(),
                    StringUtils.substringBefore(properties.getAlgorithm().getAlgorithmName(), "["));
        }
        return shardingAlgorithmMap;
    }

    private void buildUsedShardingAlgorithms(String[] logicTables, Map<String, String> usedShardingAlgorithm) {
        if (logicTables.length > 1) {
            Map<String, String> shardingAlgorithmMap = Splitter.on(",").omitEmptyStrings().withKeyValueSeparator("->")
                    .split(properties.getAlgorithm().getAlgorithmName());
            for (String logicTable : logicTables) {
                String algorithmName = StringUtils.substringBefore(shardingAlgorithmMap.get(logicTable), "[");
                if (!usedShardingAlgorithm.containsKey(algorithmName)) {
                    String algorithmProperties = StringUtils.substringBetween(shardingAlgorithmMap.get(logicTable), "[", "]");
                    usedShardingAlgorithm.put(algorithmName, algorithmProperties);
                }
            }
        } else {
            String algorithmProperties = StringUtils.substringBetween(properties.getAlgorithm().getAlgorithmName(), "[", "]");
            usedShardingAlgorithm.put(StringUtils.substringBefore(properties.getAlgorithm().getAlgorithmName(), "["), algorithmProperties);
        }
    }

    private Map<String, String> getShardingColumnMapping(String[] logicTables) {
        Map<String, String> shardingColumnMap = new HashMap<>();
        if (logicTables.length > 1) {
            shardingColumnMap = Splitter.on(",").omitEmptyStrings().withKeyValueSeparator("->")
                    .split(properties.getAlgorithm().getShardingColumn());
        } else {
            shardingColumnMap.put(properties.getDatasource().getLogicTable(),
                    properties.getAlgorithm().getShardingColumn());
        }
        return shardingColumnMap;
    }

    private Map<String, String> getShardingActualNodeMapping(String[] logicTables, String logicSchemaName) {
        Map<String, String> actualShardingNodeMap = new HashMap<>();
        for (String logicTable : logicTables) {
            actualShardingNodeMap.put(logicTable, getActualNodeConfiguration(logicTable, logicSchemaName));
        }
        return actualShardingNodeMap;
    }

    private String getActualNodeConfiguration(String logicTable, String logicSchemaName) {
        String actualDatabases;
        if (properties.getDatasource().getDbPartitionNum() > 1) {
            actualDatabases = String.format("%s%s${0..%d}.%s%s${0..%d}",
                    logicSchemaName,
                    properties.getPartitionJoinDelimiter(),
                    properties.getDatasource().getDbPartitionNum() - 1,
                    logicTable,
                    properties.getPartitionJoinDelimiter(),
                    properties.getDatasource().getTablePartitionNum() - 1);
        } else {
            actualDatabases = String.format("%s.%s%s${0..%d}",
                    logicSchemaName,
                    logicTable,
                    properties.getPartitionJoinDelimiter(),
                    properties.getDatasource().getTablePartitionNum() - 1);
        }
        return actualDatabases;
    }

}
