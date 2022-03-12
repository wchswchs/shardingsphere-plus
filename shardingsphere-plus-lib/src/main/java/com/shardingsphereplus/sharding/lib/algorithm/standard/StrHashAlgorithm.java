package com.shardingsphereplus.sharding.lib.algorithm.standard;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.shardingsphereplus.sharding.lib.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Properties;

@Slf4j
public class StrHashAlgorithm implements StandardShardingAlgorithm<String> {

    private Properties props = new Properties();
    private int startIndex = -1;
    private int endIndex = -1;
    private String partitionJoinDelimiter;


    public StrHashAlgorithm() {
    }

    @Override
    public void init() {
        Preconditions.checkArgument(props.containsKey("startIndex") || props.contains("endIndex"),
                "startIndex and endIndex cannot be both null.");
        this.startIndex = Integer.parseInt(props.getProperty("startIndex"));
        this.endIndex = Integer.parseInt(props.getProperty("endIndex"));
        this.partitionJoinDelimiter = props.getProperty("partitionJoinDelimiter");
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
        int hash = Math.abs(Hashing.murmur3_128(0x1234ABCD).newHasher()
                .putString(shardingColumnValue, StandardCharsets.UTF_8)
                .hash().asInt());
        String routedPartition = StringUtils.joinWith(partitionJoinDelimiter, shardingValue.getLogicTableName(), hash % partitions.size());
        if (log.isDebugEnabled()) {
            log.debug("compute hashcode and routed partition, [hashcode={}, index={}, partition={}]",
                    hash, hash % partitions.size(), routedPartition);
        }
        if (!partitions.contains(routedPartition)) {
            throw new ShardingSphereException("unavailable routed partition: " + routedPartition);
        }
        return routedPartition;
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<String> rangeShardingValue) {
        return null;
    }

    @Override
    public String getType() {
        return "StrHash";
    }

    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public Properties getProps() {
        return props;
    }

    @Override
    public void setProps(Properties props) {
        this.props = props;
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
