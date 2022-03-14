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
        if (StringUtils.isEmpty(properties.getDatasource().getTablePartitionNum())) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.datasource.dbPartitionNum can not be empty"
            );
        }
        if (StringUtils.isEmpty(properties.getAlgorithm().getShardingColumn())) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.algorithm.shardingColumn can not be empty"
            );
        }

        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();

        String jdbcUrl = properties.getDatasource().getJdbcUrl();
        List<String> jdbcUrlUnits = Splitter.on("//").omitEmptyStrings().splitToList(jdbcUrl);
        String logicSchemaName = jdbcUrlUnits.get(1).split("/")[1].split("\\?")[0];
        String[] logicTables = properties.getDatasource().getLogicTable().split(",");
        String[] shardingAlgorithmItems = properties.getAlgorithm().getShardingAlgorithmName().split(",");
        String[] shardingColumnItems = properties.getAlgorithm().getShardingColumn().split(",");
        String[] tablePartitionNumItems = properties.getDatasource().getTablePartitionNum().split(",");

        Map<String, DataSource> dataSourceMap = buildDatasource(logicSchemaName, jdbcUrl);
        Map<String, String> usedShardingAlgorithm = new HashMap<>();
        buildUsedShardingAlgorithms(shardingAlgorithmItems, usedShardingAlgorithm);
        registerTableRule(
                getShardingAlgorithmMapping(logicTables, shardingAlgorithmItems),
                getShardingColumnMapping(shardingColumnItems),
                getShardingActualNodeMapping(logicTables, logicSchemaName, tablePartitionNumItems),
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
                                            ShardingRuleConfiguration shardingRuleConfiguration) {
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

    private Map<String, String> getShardingAlgorithmMapping(String[] logicTables, String[] shardingAlgorithmItems) {
        Map<String, String> shardingAlgorithmMap = new HashMap<>();
        if (shardingAlgorithmItems.length > 1) {
            for (String item: shardingAlgorithmItems) {
                String[] algorithmItem = item.split("->");
                shardingAlgorithmMap.put(algorithmItem[0], StringUtils.substringBefore(algorithmItem[1], "["));
            }
        } else {
            for (String logicTable : logicTables) {
                shardingAlgorithmMap.put(logicTable,
                        StringUtils.substringBefore(shardingAlgorithmItems[0], "["));
            }
        }
        return shardingAlgorithmMap;
    }

    private void buildUsedShardingAlgorithms(String[] shardingAlgorithmItems, Map<String, String> usedShardingAlgorithm) {
        if (shardingAlgorithmItems.length > 1) {
            for (String item : shardingAlgorithmItems) {
                String[] algorithmItem = item.split("->");
                String algorithmName = StringUtils.substringBefore(algorithmItem[1], "[");
                if (!usedShardingAlgorithm.containsKey(algorithmName)) {
                    String algorithmProperties = StringUtils.substringBetween(algorithmItem[1], "[", "]");
                    usedShardingAlgorithm.put(algorithmName, algorithmProperties);
                }
            }
        } else {
            String algorithmProperties = StringUtils.substringBetween(shardingAlgorithmItems[0], "[", "]");
            usedShardingAlgorithm.put(StringUtils.substringBefore(shardingAlgorithmItems[0], "["), algorithmProperties);
        }
    }

    private Map<String, String> getShardingColumnMapping(String[] shardingColumns) {
        Map<String, String> shardingColumnMap = new HashMap<>();
        if (shardingColumns.length > 1) {
            for (String column : shardingColumns) {
                String[] columnMap = column.split("->");
                shardingColumnMap.put(columnMap[0], columnMap[1]);
            }
        } else {
            shardingColumnMap.put(properties.getDatasource().getLogicTable(),
                    properties.getAlgorithm().getShardingColumn());
        }
        return shardingColumnMap;
    }

    private Map<String, String> getShardingActualNodeMapping(String[] logicTables, String logicSchemaName, String[] tablePartitionNumItems) {
        Map<String, String> actualShardingNodeMap = new HashMap<>();
        for (String logicTable : logicTables) {
            if (tablePartitionNumItems.length > 1) {
                for (String partitionNum : tablePartitionNumItems) {
                    String[] partitionPair = partitionNum.split("->");
                    if (partitionPair[0].equals(logicTable)) {
                        actualShardingNodeMap.put(logicTable,
                                getActualNodeConfiguration(logicTable, logicSchemaName, Integer.parseInt(partitionPair[1])));
                    }
                }
            } else {
                actualShardingNodeMap.put(logicTable,
                        getActualNodeConfiguration(logicTable, logicSchemaName, Integer.parseInt(tablePartitionNumItems[0])));
            }
        }
        return actualShardingNodeMap;
    }

    private String getActualNodeConfiguration(String logicTable, String logicSchemaName, int tablePartitionNum) {
        String actualDatabases;
        if (properties.getDatasource().getDbPartitionNum() > 1) {
            actualDatabases = String.format("%s%s${0..%d}.%s%s${0..%d}",
                    logicSchemaName,
                    properties.getPartitionJoinDelimiter(),
                    properties.getDatasource().getDbPartitionNum() - 1,
                    logicTable,
                    properties.getPartitionJoinDelimiter(),
                    tablePartitionNum - 1);
        } else {
            actualDatabases = String.format("%s.%s%s${0..%d}",
                    logicSchemaName,
                    logicTable,
                    properties.getPartitionJoinDelimiter(),
                    tablePartitionNum - 1);
        }
        return actualDatabases;
    }

}
