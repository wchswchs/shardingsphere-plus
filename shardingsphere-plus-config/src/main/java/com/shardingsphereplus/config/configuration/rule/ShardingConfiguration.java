package com.shardingsphereplus.config.configuration.rule;

import com.shardingsphereplus.config.Configuration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;

import java.util.List;
import java.util.Map;

public class ShardingConfiguration implements Configuration<ShardingRuleConfiguration> {

    private final Map<String, ShardingSphereAlgorithmConfiguration> algorithmConfigurations;
    private final List<ShardingTableRuleConfiguration> tableRuleConfigurations;

    public ShardingConfiguration(List<ShardingTableRuleConfiguration> tableRuleConfigurations,
                                 Map<String, ShardingSphereAlgorithmConfiguration> algorithmConfigurations) {
        this.algorithmConfigurations = algorithmConfigurations;
        this.tableRuleConfigurations = tableRuleConfigurations;
    }

    @Override
    public ShardingRuleConfiguration build() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        for (ShardingTableRuleConfiguration tableRuleConfiguration : tableRuleConfigurations) {
            shardingRuleConfiguration.getTables().add(tableRuleConfiguration);
        }
        for (Map.Entry<String, ShardingSphereAlgorithmConfiguration> algorithmConfiguration
                : algorithmConfigurations.entrySet()) {
            shardingRuleConfiguration.getShardingAlgorithms().put(algorithmConfiguration.getKey(),
                    algorithmConfiguration.getValue());
        }
        return shardingRuleConfiguration;
    }

}
