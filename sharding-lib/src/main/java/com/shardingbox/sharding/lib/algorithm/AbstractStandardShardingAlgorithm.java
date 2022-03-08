package com.shardingbox.sharding.lib.algorithm;

import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

public abstract class AbstractStandardShardingAlgorithm<T extends Comparable<?>> implements StandardShardingAlgorithm<T> {

    protected String partitionJoinDelimiter = "_";

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<T> rangeShardingValue) {
        return null;
    }

    public String getPartitionJoinDelimiter() {
        return partitionJoinDelimiter;
    }

    public void setPartitionJoinDelimiter(String partitionJoinDelimiter) {
        this.partitionJoinDelimiter = partitionJoinDelimiter;
    }

}
