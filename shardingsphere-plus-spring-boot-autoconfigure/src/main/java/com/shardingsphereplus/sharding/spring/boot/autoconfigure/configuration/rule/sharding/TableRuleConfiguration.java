package com.shardingsphereplus.sharding.spring.boot.autoconfigure.configuration.rule.sharding;

import com.shardingsphereplus.sharding.spring.boot.autoconfigure.Configuration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableRuleConfiguration implements Configuration<List<ShardingTableRuleConfiguration>> {

    private final Map<String, String> actualNodes;
    private final Map<String, String> shardingColumns;
    private final Map<String, String> shardingAlgorithms;

    public TableRuleConfiguration(Map<String, String> actualNodes, Map<String, String> shardingColumns,
                                  Map<String, String> shardingAlgorithms) {
        this.actualNodes = actualNodes;
        this.shardingColumns = shardingColumns;
        this.shardingAlgorithms = shardingAlgorithms;
    }

    @Override
    public List<ShardingTableRuleConfiguration> build() {
        List<ShardingTableRuleConfiguration> tableRuleConfigurations = new ArrayList<>();
        for (final Map.Entry<String, String> actualNode : actualNodes.entrySet()) {
            ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration(
                    actualNode.getKey(),
                    actualNode.getValue()
            );
            shardingTableRuleConfiguration.setTableShardingStrategy(new StandardShardingStrategyConfiguration(
                    shardingColumns.get(actualNode.getKey()),
                    shardingAlgorithms.get(actualNode.getKey()))
            );
            tableRuleConfigurations.add(shardingTableRuleConfiguration);
        }
        return tableRuleConfigurations;
    }

}
