package com.shardingsphereplus.sharding.spring.boot.autoconfigure;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
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
        if (StringUtils.isEmpty(properties.getDatasource().getDbAddress())) {
            throw new IllegalArgumentException("configuration item spring.sharding.datasource.dbAddress can not be empty");
        }
        if (StringUtils.isEmpty(properties.getDatasource().getLogicDbName())) {
            throw new IllegalArgumentException("configuration item spring.sharding.datasource.logicDbName can not be empty");
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

        String[] dbAddresses = properties.getDatasource().getDbAddress().split(",");
        String logicDbName = properties.getDatasource().getLogicDbName();
        String[] logicTables = properties.getDatasource().getLogicTable().split(",");
        String[] shardingTableAlgorithmItems = properties.getAlgorithm().getShardingTableAlgorithmName().split(",");
        String[] shardingColumnItems = properties.getAlgorithm().getShardingColumn().split(",");
        String[] tablePartitionNumItems = properties.getDatasource().getTablePartitionNum().split(",");

        Map<String, DataSource> dataSourceMap = buildDatasource(
                logicDbName, dbAddresses,
                properties.getDatasource().getCharacterEncoding(),
                properties.getDatasource().isRewriteBatchedStatements());

        Map<String, String> usedShardingAlgorithm = new HashMap<>();
        buildUsedShardingAlgorithms(shardingTableAlgorithmItems, usedShardingAlgorithm);
        registerTableRule(
                getShardingAlgorithmMapping(logicTables, shardingTableAlgorithmItems),
                getShardingColumnMapping(shardingColumnItems),
                getShardingActualNodeMapping(logicTables, logicDbName, tablePartitionNumItems, dbAddresses),
                logicTables, shardingRuleConfiguration);
        registerShardingAlgorithms(usedShardingAlgorithm, shardingRuleConfiguration);

        List<RuleConfiguration> ruleConfigurations = new ArrayList<>();

        ruleConfigurations.add(shardingRuleConfiguration);
        if (StringUtils.isNotEmpty(properties.getDatasource().getReadDatasource())
                && StringUtils.isNotEmpty(properties.getDatasource().getReadDatasource())) {
            ruleConfigurations.add(
                    buildReadWriteRuleConfiguration(properties.getDatasource().getReadDatasource(),
                    properties.getDatasource().getWriteDatasource())
            );
        }

        //build common properties
        Properties commonProperties = new Properties();
        commonProperties.setProperty("sql-show", properties.getSqlShow());

        return ShardingSphereDataSourceFactory.createDataSource(logicDbName, dataSourceMap, ruleConfigurations, commonProperties);
    }

    private Map<String, DataSource> buildDatasource(String logicDbName, String[] dbAddresses, String characterEncoding,
                                                    boolean rewriteBatchedStatements) {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        int i = 0;
        for (String address : dbAddresses) {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            String schemaName = logicDbName + properties.getPartitionJoinDelimiter() + i;
            String jdbcAddress = StringUtils.joinWith("//", "jdbc:mysql:", address);
            String jdbcUrl = StringUtils.joinWith("/", jdbcAddress, logicDbName);
            String characterEncodingParam = StringUtils.joinWith("=", "characterEncoding", characterEncoding);
            String rewriteBatchedStatementsParam = StringUtils.joinWith("=", "rewriteBatchedStatements", rewriteBatchedStatements);
            String jdbcParams = StringUtils.joinWith("&", characterEncodingParam, rewriteBatchedStatementsParam);
            dataSource.setUrl(StringUtils.joinWith("?", jdbcUrl, jdbcParams));
            dataSource.setUsername(properties.getDatasource().getUsername());
            dataSource.setPassword(properties.getDatasource().getPassword());
            dataSourceMap.put(schemaName, dataSource);

            i ++;
        }
        return dataSourceMap;
    }

    private ReadwriteSplittingRuleConfiguration buildReadWriteRuleConfiguration(String readDatasource, String writeDatasource) {
        Properties readWriteProperties = new Properties();
        readWriteProperties.setProperty("read-data-source-names", readDatasource);
        readWriteProperties.setProperty("write-data-source-name", writeDatasource);
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("default", "Static",
                        readWriteProperties, "ROUND_ROBIN");
        ShardingSphereAlgorithmConfiguration algorithmConfiguration =
                new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties());
        Map<String, ShardingSphereAlgorithmConfiguration> sphereAlgorithmConfigurationMap = new HashMap<>(1);
        sphereAlgorithmConfigurationMap.put("round_robin", algorithmConfiguration);
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceConfig), sphereAlgorithmConfigurationMap);
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
        }
        if (shardingAlgorithmItems.length == 1) {
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
        }
        if (shardingAlgorithmItems.length == 1) {
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
        }
        if (shardingColumns.length == 1) {
            shardingColumnMap.put(properties.getDatasource().getLogicTable(),
                    properties.getAlgorithm().getShardingColumn());
        }
        return shardingColumnMap;
    }

    private Map<String, String> getShardingActualNodeMapping(String[] logicTables, String logicDbName, String[] tablePartitionNumItems, String[] dbAddresses) {
        Map<String, String> actualShardingNodeMap = new HashMap<>();
        for (String logicTable : logicTables) {
            if (tablePartitionNumItems.length > 1) {
                for (String partitionNum : tablePartitionNumItems) {
                    String[] partitionPair = partitionNum.split("->");
                    if (partitionPair[0].equals(logicTable)) {
                        actualShardingNodeMap.put(logicTable,
                                getActualNodeConfiguration(logicTable, logicDbName, Integer.parseInt(partitionPair[1]), dbAddresses));
                    }
                }
            }
            if (tablePartitionNumItems.length == 1) {
                actualShardingNodeMap.put(logicTable,
                        getActualNodeConfiguration(logicTable, logicDbName, Integer.parseInt(tablePartitionNumItems[0]), dbAddresses));
            }
        }
        return actualShardingNodeMap;
    }

    private String getActualNodeConfiguration(String logicTable, String logicSchemaName, int tablePartitionNum, String[] dbAddresses) {
        String actualDatabases = "";
        if (dbAddresses.length > 1) {
            actualDatabases = String.format("%s%s${0..%d}.%s%s${0..%d}",
                    logicSchemaName,
                    properties.getPartitionJoinDelimiter(),
                    dbAddresses.length - 1,
                    logicTable,
                    properties.getPartitionJoinDelimiter(),
                    tablePartitionNum - 1);
        }
        if (dbAddresses.length == 1) {
            actualDatabases = String.format("%s%s0.%s%s${0..%d}",
                    logicSchemaName,
                    properties.getPartitionJoinDelimiter(),
                    logicTable,
                    properties.getPartitionJoinDelimiter(),
                    tablePartitionNum - 1);
        }
        return actualDatabases;
    }

}
