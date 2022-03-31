package com.shardingsphereplus.spring.boot.autoconfigure;

import com.shardingsphereplus.config.configuration.DatasourceConfiguration;
import com.shardingsphereplus.config.configuration.rule.ReadWriteRuleConfiguration;
import com.shardingsphereplus.config.configuration.rule.ShardingConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.AlgorithmRuleConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.TableRuleConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.table.ActualNodesConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.table.ShardingAlgorithmConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.table.ShardingColumnConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
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
                    "configuration item spring.sharding.datasource.dbPartitionNum can not be empty"
            );
        }


        String[] dbServers = properties.getDatasource().getDbServer().split(",");
        String logicDbName = properties.getDatasource().getLogicDbName();
        String[] logicTables = properties.getDatasource().getLogicTable().split(",");
        String[] shardingTableAlgorithms = properties.getAlgorithm().getShardingTableAlgorithmName().split(",");
        String[] shardingColumns = properties.getAlgorithm().getShardingColumn().split(",");
        String[] tablePartitionNumList = properties.getDatasource().getTablePartitionNum().split(",");

        List<RuleConfiguration> ruleConfigurations = new ArrayList<>();

        Map<String, DataSource> dataSourceMap = new DatasourceConfiguration(
                logicDbName, dbServers,
                properties.getDatasource().getUsername(),
                properties.getDatasource().getPassword(),
                properties.getDatasource().getCharacterEncoding(),
                properties.getDatasource().isRewriteBatchedStatements(),
                properties.getPartitionJoinDelimiter()).build();

        if (ArrayUtils.isNotEmpty(shardingColumns) && ArrayUtils.isNotEmpty(logicTables)) {
            List<ShardingTableRuleConfiguration> tableRuleConfigurations = new TableRuleConfiguration(
                    new ActualNodesConfiguration(
                            logicTables, logicDbName, tablePartitionNumList,
                            properties.getDatasource().getShardingDatasource(),
                            properties.getPartitionJoinDelimiter()).build(),
                    new ShardingColumnConfiguration(logicTables, shardingColumns).build(),
                    new ShardingAlgorithmConfiguration(logicTables, shardingTableAlgorithms).build()
            ).build();
            Map<String, ShardingSphereAlgorithmConfiguration> shardingAlgorithmConfigurations =
                    new AlgorithmRuleConfiguration(shardingTableAlgorithms, properties.getPartitionJoinDelimiter()).build();
            ShardingRuleConfiguration shardingRuleConfiguration = new ShardingConfiguration(tableRuleConfigurations, shardingAlgorithmConfigurations).build();
            ruleConfigurations.add(shardingRuleConfiguration);
        }

        if (StringUtils.isNotEmpty(properties.getDatasource().getReadDatasource())
                && StringUtils.isNotEmpty(properties.getDatasource().getReadDatasource())) {
            ruleConfigurations.add(new ReadWriteRuleConfiguration(
                    properties.getDatasource().getReadDatasource(),
                    properties.getDatasource().getWriteDatasource(),
                    properties.getPartitionJoinDelimiter(),
                    logicDbName).build());
        }

        //build common properties
        Properties commonProperties = new Properties();
        commonProperties.setProperty("sql-show", properties.getSqlShow());

        return ShardingSphereDataSourceFactory.createDataSource(logicDbName, dataSourceMap, ruleConfigurations, commonProperties);
    }

}
