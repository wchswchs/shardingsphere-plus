package com.shardingsphereplus.sharding.spring.boot.autoconfigure.configuration.rule.sharding.algorithm;

import com.google.common.base.Splitter;
import com.shardingsphereplus.sharding.spring.boot.autoconfigure.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Properties;

public class AlgorithmProperties implements Configuration<Properties> {

    private final String propertiesString;
    private final String partitionJoinDelimiter;

    public AlgorithmProperties(String propertiesString, String partitionJoinDelimiter) {
        this.propertiesString = propertiesString;
        this.partitionJoinDelimiter = partitionJoinDelimiter;
    }

    @Override
    public Properties build() {
        Properties shardingAlgorithmProperties = new Properties();
        if (StringUtils.isNotEmpty(propertiesString)) {
            for (final Map.Entry<String, String> property : Splitter.on("|").withKeyValueSeparator(":").split(propertiesString).entrySet()) {
                shardingAlgorithmProperties.setProperty(property.getKey(), property.getValue());
                shardingAlgorithmProperties.setProperty("partitionJoinDelimiter", partitionJoinDelimiter);
            }
        }
        return shardingAlgorithmProperties;
    }

}
