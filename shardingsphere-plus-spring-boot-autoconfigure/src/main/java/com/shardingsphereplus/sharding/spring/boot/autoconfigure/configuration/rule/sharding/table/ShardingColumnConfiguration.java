package com.shardingsphereplus.sharding.spring.boot.autoconfigure.configuration.rule.sharding.table;

import com.shardingsphereplus.sharding.spring.boot.autoconfigure.Configuration;

import java.util.HashMap;
import java.util.Map;

public class ShardingColumnConfiguration implements Configuration<Map<String, String>> {

    private final String[] logicTables;
    private final String[] shardingColumns;

    public ShardingColumnConfiguration(String[] logicTables, String[] shardingColumns) {
        this.logicTables = logicTables;
        this.shardingColumns = shardingColumns;
    }

    @Override
    public Map<String, String> build() {
        Map<String, String> shardingColumnMap = new HashMap<>();
        if (shardingColumns.length > 1) {
            for (String column : shardingColumns) {
                String[] columnMap = column.split("->");
                shardingColumnMap.put(columnMap[0], columnMap[1]);
            }
            return shardingColumnMap;
        }
        shardingColumnMap.put(logicTables[0], shardingColumns[0]);
        return shardingColumnMap;
    }

}
