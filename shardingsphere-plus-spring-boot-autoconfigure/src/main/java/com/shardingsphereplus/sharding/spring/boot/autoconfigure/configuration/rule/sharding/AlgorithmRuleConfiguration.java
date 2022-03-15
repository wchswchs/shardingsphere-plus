package com.shardingsphereplus.sharding.spring.boot.autoconfigure.configuration.rule.sharding;

import com.shardingsphereplus.sharding.spring.boot.autoconfigure.Configuration;
import com.shardingsphereplus.sharding.spring.boot.autoconfigure.configuration.rule.sharding.algorithm.AlgorithmProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;

import java.util.HashMap;
import java.util.Map;

public class AlgorithmRuleConfiguration implements Configuration<Map<String, ShardingSphereAlgorithmConfiguration>> {

    private final String[] shardingAlgorithms;
    private final String partitionJoinDelimiter;

    public AlgorithmRuleConfiguration(String[] shardingAlgorithms, String partitionJoinDelimiter) {
        this.shardingAlgorithms = shardingAlgorithms;
        this.partitionJoinDelimiter = partitionJoinDelimiter;
    }

    @Override
    public Map<String, ShardingSphereAlgorithmConfiguration> build() {
        Map<String, String> usedShardingAlgorithm = new HashMap<>();
        if (shardingAlgorithms.length > 1) {
            for (String item : shardingAlgorithms) {
                String[] algorithmItem = item.split("->");
                String algorithmName = StringUtils.substringBefore(algorithmItem[1], "[");
                if (!usedShardingAlgorithm.containsKey(algorithmName)) {
                    String algorithmProperties = StringUtils.substringBetween(algorithmItem[1], "[", "]");
                    usedShardingAlgorithm.put(algorithmName, algorithmProperties);
                }
            }
        }
        if (shardingAlgorithms.length == 1) {
            String algorithmProperties = StringUtils.substringBetween(shardingAlgorithms[0], "[", "]");
            usedShardingAlgorithm.put(StringUtils.substringBefore(shardingAlgorithms[0], "["), algorithmProperties);
        }
        Map<String, ShardingSphereAlgorithmConfiguration> algorithmConfigurationMap = new HashMap<>();
        for (final Map.Entry<String, String> shardingAlgorithm : usedShardingAlgorithm.entrySet()) {
            algorithmConfigurationMap.put(shardingAlgorithm.getKey(), new ShardingSphereAlgorithmConfiguration(
                    shardingAlgorithm.getKey(),
                    new AlgorithmProperties(shardingAlgorithm.getValue(), partitionJoinDelimiter).build()
                    ));
        }
        return algorithmConfigurationMap;
    }



}
