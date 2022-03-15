package com.shardingsphereplus.sharding.spring.boot.autoconfigure.configuration.rule.sharding.table;

import com.shardingsphereplus.sharding.spring.boot.autoconfigure.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ShardingAlgorithmConfiguration implements Configuration<Map<String, String>> {

    private final String[] logicTables;
    private final String[] shardingAlgorithms;

    public ShardingAlgorithmConfiguration(String[] logicTables, String[] shardingAlgorithms) {
        this.logicTables = logicTables;
        this.shardingAlgorithms = shardingAlgorithms;
    }

    @Override
    public Map<String, String> build() {
        Map<String, String> shardingAlgorithmMap = new HashMap<>();
        if (shardingAlgorithms.length > 1) {
            for (String item: shardingAlgorithms) {
                String[] algorithmItem = item.split("->");
                shardingAlgorithmMap.put(algorithmItem[0], StringUtils.substringBefore(algorithmItem[1], "["));
            }
        }
        if (shardingAlgorithms.length == 1) {
            for (String logicTable : logicTables) {
                shardingAlgorithmMap.put(logicTable,
                        StringUtils.substringBefore(shardingAlgorithms[0], "["));
            }
        }
        return shardingAlgorithmMap;
    }

}
