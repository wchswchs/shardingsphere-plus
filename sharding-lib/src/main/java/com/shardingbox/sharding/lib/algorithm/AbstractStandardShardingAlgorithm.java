package com.shardingbox.sharding.lib.algorithm;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;

public abstract class AbstractStandardShardingAlgorithm<T extends Comparable<?>> implements PreciseShardingAlgorithm<T> {

    protected String partitionJoinDelimiter = "_";

    public String getPartitionJoinDelimiter() {
        return partitionJoinDelimiter;
    }

    public void setPartitionJoinDelimiter(String partitionJoinDelimiter) {
        this.partitionJoinDelimiter = partitionJoinDelimiter;
    }

}
