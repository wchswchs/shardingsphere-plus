package com.shardingsphereplus.spring.boot.autoconfigure;

import com.shardingsphereplus.config.DataSourceFactory;
import com.shardingsphereplus.config.configuration.DatasourceConfiguration;
import com.shardingsphereplus.config.configuration.rule.ReadWriteRuleConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.AlgorithmRuleConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.table.ActualNodesConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.table.ShardingAlgorithmConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.table.ShardingColumnConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(ShardingProperties.class)
public class ShardingAutoConfiguration {

    private final ShardingProperties properties;

    public ShardingAutoConfiguration(ShardingProperties properties) {
        this.properties = properties;
    }

    @Bean
    public DataSource dataSource() throws Exception {
        if (StringUtils.isEmpty(properties.getDatasource().getDbServer())) {
            throw new IllegalArgumentException("configuration item spring.sharding.datasource.dbServer can not be empty");
        }
        if (StringUtils.isEmpty(properties.getDatasource().getLogicDbName())) {
            throw new IllegalArgumentException("configuration item spring.sharding.datasource.logicDbName can not be empty");
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
                    "configuration item spring.sharding.datasource.tablePartitionNum can not be empty"
            );
        }

        String[] dbServers = properties.getDatasource().getDbServer().split(",");
        String logicDbName = properties.getDatasource().getLogicDbName();
        String[] logicTables = properties.getDatasource().getLogicTable().split(",");
        String[] shardingTableAlgorithms = properties.getAlgorithm().getShardingTableAlgorithmName().split(",");
        String[] shardingColumns = properties.getAlgorithm().getShardingColumn().split(",");
        String[] tablePartitionNumList = properties.getDatasource().getTablePartitionNum().split(",");

        DatasourceConfiguration datasourceConfiguration = new DatasourceConfiguration(
                logicDbName, dbServers,
                properties.getDatasource().getUsername(),
                properties.getDatasource().getPassword(),
                properties.getDatasource().getServerTimeZone(),
                properties.getDatasource().getCharacterEncoding(),
                properties.getDatasource().isRewriteBatchedStatements(),
                properties.getPartitionJoinDelimiter());

        ActualNodesConfiguration actualNodesConfiguration = new ActualNodesConfiguration(
                logicTables, logicDbName, tablePartitionNumList,
                properties.getDatasource().getShardingDatasource(),
                properties.getPartitionJoinDelimiter());
        ShardingAlgorithmConfiguration shardingAlgorithmConfiguration = new ShardingAlgorithmConfiguration(logicTables, shardingTableAlgorithms);
        ShardingColumnConfiguration shardingColumnConfiguration = new ShardingColumnConfiguration(logicTables, shardingColumns);
        AlgorithmRuleConfiguration shardingAlgorithmConfigurations =
                new AlgorithmRuleConfiguration(shardingTableAlgorithms, properties.getPartitionJoinDelimiter());
        ReadWriteRuleConfiguration readWriteRuleConfiguration = new ReadWriteRuleConfiguration(
                properties.getDatasource().getReadDatasource(),
                properties.getDatasource().getWriteDatasource(),
                properties.getPartitionJoinDelimiter(),
                logicDbName);

        //build common properties
        DataSourceFactory.CommonConfig commonConfig = new DataSourceFactory.CommonConfig(properties.getSqlShow());
        DataSourceFactory.Condition condition = new DataSourceFactory.Condition(shardingColumns, logicTables, properties.getDatasource().getReadDatasource());

        return DataSourceFactory.build(
                datasourceConfiguration,
                actualNodesConfiguration,
                shardingColumnConfiguration,
                shardingAlgorithmConfiguration,
                readWriteRuleConfiguration,
                shardingAlgorithmConfigurations,
                condition, commonConfig, logicDbName
        );
    }

}
