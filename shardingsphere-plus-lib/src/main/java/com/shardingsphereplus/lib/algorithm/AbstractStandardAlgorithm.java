package com.shardingsphereplus.lib.algorithm;

import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

public abstract class AbstractStandardAlgorithm<T extends Comparable<?>> implements StandardShardingAlgorithm<T> {

    protected String partitionJoinDelimiter = "_";
    protected final static String PARTITION_JOIN_DELIMITER = "partitionJoinDelimiter";

    public String getPartitionJoinDelimiter() {
        return partitionJoinDelimiter;
    }

    public void setPartitionJoinDelimiter(String partitionJoinDelimiter) {
        this.partitionJoinDelimiter = partitionJoinDelimiter;
    }

}
