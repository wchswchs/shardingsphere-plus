package com.shardingsphereplus.sharding.lib.algorithm.standard;

import com.google.common.hash.Hashing;
import com.shardingsphereplus.sharding.lib.algorithm.AbstractStandardShardingAlgorithm;
import com.shardingsphereplus.sharding.lib.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Slf4j
public class StrHashAlgorithm extends AbstractStandardShardingAlgorithm<String> {

    private int startIndex = -1;
    private int endIndex = -1;

    public StrHashAlgorithm() {
    }

    public StrHashAlgorithm(int startIndex, int endIndex, String partitionJoinDelimiter) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        if (StringUtils.isNotEmpty(partitionJoinDelimiter)) {
            this.partitionJoinDelimiter = partitionJoinDelimiter;
        }
    }

    @Override
    public String doSharding(Collection<String> partitions, PreciseShardingValue<String> shardingValue) {
        if (CollectionUtils.isEmpty(partitions)
                || shardingValue == null
                || StringUtils.isEmpty(shardingValue.getLogicTableName())
                || shardingValue.getValue() == null) {
            throw new IllegalArgumentException("sharding partitions or key can not be empty");
        }
        if (log.isDebugEnabled()) {
            log.debug("sharding params [partitions={}, logicTable={}, columnName={}, columnValue={}]",
                    ArrayUtils.toString(partitions),
                    shardingValue.getLogicTableName(),
                    shardingValue.getColumnName(),
                    shardingValue.getValue()
                    );
        }
        String shardingColumnValue = StringUtil.subString(shardingValue.getValue(), startIndex, endIndex);
        //use murmurhash3 because it's collision is lower than hashCode()
        int hash = Hashing.murmur3_128(0x1234ABCD).newHasher()
                .putString(shardingColumnValue, StandardCharsets.UTF_8)
                .hash().asInt() & Integer.MAX_VALUE;
        int index = hash % partitions.size();
        String routedPartition = StringUtils.leftPad(String.valueOf(index), 3, '0');
        if (log.isDebugEnabled()) {
            log.debug("compute hashcode and routed partition, [hashcode={}, index={}, partition={}]",
                    hash, index, routedPartition);
        }
        if (!partitions.contains(routedPartition)) {
            throw new ShardingSphereException("unavailable routed partition: " + routedPartition);
        }
        return StringUtils.joinWith(partitionJoinDelimiter, shardingValue.getLogicTableName(), routedPartition);
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

}
