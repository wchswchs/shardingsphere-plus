package com.shardingsphereplus.config;

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

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DataSourceFactory {

    public static DataSource build(
            DatasourceConfiguration datasourceConfiguration,
            ActualNodesConfiguration actualNodesConfiguration,
            ShardingColumnConfiguration shardingColumnConfiguration,
            ShardingAlgorithmConfiguration shardingAlgorithmConfiguration,
            ReadWriteRuleConfiguration readWriteRuleConfiguration,
            AlgorithmRuleConfiguration algorithmRuleConfiguration,
            Condition condition,
            CommonConfig commonConfig,
            String logicDbName
            ) throws Exception {
        Map<String, DataSource> dataSourceMap = datasourceConfiguration.build();

        List<RuleConfiguration> ruleConfigurations = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(condition.getShardingColumns()) && ArrayUtils.isNotEmpty(condition.getLogicTables())) {
            List<ShardingTableRuleConfiguration> tableRuleConfigurations = new TableRuleConfiguration(
                    actualNodesConfiguration.build(),
                    shardingColumnConfiguration.build(),
                    shardingAlgorithmConfiguration.build()
            ).build();
            Map<String, ShardingSphereAlgorithmConfiguration> shardingAlgorithmConfigurations = algorithmRuleConfiguration.build();
            ShardingRuleConfiguration shardingRuleConfiguration = new ShardingConfiguration(tableRuleConfigurations, shardingAlgorithmConfigurations).build();
            ruleConfigurations.add(shardingRuleConfiguration);
        }

        if (StringUtils.isNotEmpty(condition.getReadDataSource())
                && StringUtils.isNotEmpty(condition.getReadDataSource())) {
            ruleConfigurations.add(readWriteRuleConfiguration.build());
        }

        //build common properties
        Properties commonProperties = new Properties();
        commonProperties.setProperty("sql-show", commonConfig.getSqlShow());

        return ShardingSphereDataSourceFactory.createDataSource(logicDbName, dataSourceMap, ruleConfigurations, commonProperties);
    }

    public static class Condition {
        String[] shardingColumns;
        String[] logicTables;

        String readDataSource;

        public Condition(String[] shardingColumns, String[] logicTables, String readDataSource) {
            this.shardingColumns = shardingColumns;
            this.logicTables = logicTables;
            this.readDataSource = readDataSource;
        }

        public String[] getShardingColumns() {
            return shardingColumns;
        }

        public String[] getLogicTables() {
            return logicTables;
        }

        public String getReadDataSource() {
            return readDataSource;
        }
    }

    public static class CommonConfig {
        String sqlShow;

        public CommonConfig(String sqlShow) {
            this.sqlShow = sqlShow;
        }

        public String getSqlShow() {
            return sqlShow;
        }
    }

}
