package com.shardingsphereplus.sharding.spring.boot.autoconfigure.configuration.rule.sharding.table;

import com.shardingsphereplus.sharding.spring.boot.autoconfigure.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ActualNodesConfiguration implements Configuration<Map<String, String>> {

    private final String[] logicTables;
    private final String[] tablePartitionNumList;
    private final String logicDbName;
    private final String shardingDatasources;
    private final String partitionJoinDelimiter;

    public ActualNodesConfiguration(String[] logicTables, String logicDbName, String[] tablePartitionNumList,
                                    String shardingDatasources, String partitionJoinDelimiter) {
        this.logicTables = logicTables;
        this.logicDbName = logicDbName;
        this.tablePartitionNumList = tablePartitionNumList;
        this.shardingDatasources = shardingDatasources;
        this.partitionJoinDelimiter = partitionJoinDelimiter;
    }

    @Override
    public Map<String, String> build() {
        Map<String, String> actualShardingNodeMap = new HashMap<>();
        for (String logicTable : logicTables) {
            if (tablePartitionNumList.length > 1) {
                for (String partitionNum : tablePartitionNumList) {
                    String[] partitionPair = partitionNum.split("->");
                    if (partitionPair[0].equals(logicTable)) {
                        actualShardingNodeMap.put(logicTable,
                                buildShardingActualNodes(
                                        logicTable,
                                        logicDbName,
                                        Integer.parseInt(partitionPair[1]),
                                        shardingDatasources
                                ));
                    }
                }
            }
            if (tablePartitionNumList.length == 1) {
                actualShardingNodeMap.put(logicTable,
                        buildShardingActualNodes(
                                logicTables[0],
                                logicDbName,
                                Integer.parseInt(tablePartitionNumList[0]),
                                shardingDatasources
                        ));
            }
        }
        return actualShardingNodeMap;
    }

    private String buildShardingActualNodes(String logicTable, String logicDbName, int tablePartitionNum, String shardingDatasources) {
        if (StringUtils.isNotEmpty(shardingDatasources)) {
            return String.format("%s%s${%s}.%s%s${0..%d}",
                    logicDbName,
                    partitionJoinDelimiter,
                    shardingDatasources,
                    logicTable,
                    partitionJoinDelimiter,
                    tablePartitionNum - 1);
        }
        return String.format("%s%s0.%s%s${0..%d}",
                logicDbName,
                partitionJoinDelimiter,
                logicTable,
                partitionJoinDelimiter,
                tablePartitionNum - 1);
    }

}
