package com.shardingsphereplus.sharding.spring.boot.autoconfigure.configuration.rule;

import com.google.common.base.Joiner;
import com.shardingsphereplus.sharding.spring.boot.autoconfigure.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import java.util.*;

public class ReadWriteRuleConfiguration implements Configuration<ReadwriteSplittingRuleConfiguration> {

    private final String readDatasources;
    private final String writeDatasource;
    private final String partitionJoinDelimiter;
    private final String logicDbName;

    public ReadWriteRuleConfiguration(String readDatasources, String writeDatasource, String partitionJoinDelimiter,
                                      String logicDbName) {
        this.readDatasources = readDatasources;
        this.writeDatasource = writeDatasource;
        this.partitionJoinDelimiter = partitionJoinDelimiter;
        this.logicDbName = logicDbName;
    }

    @Override
    public ReadwriteSplittingRuleConfiguration build() {
        Properties readWriteProperties = new Properties();
        List<String> readDatasouces = new ArrayList<>();
        for (String readDatasourceItem : readDatasources.split(",")) {
            readDatasouces.add(StringUtils.joinWith(partitionJoinDelimiter, logicDbName, readDatasourceItem));
        }
        readWriteProperties.setProperty("read-data-source-names", Joiner.on(",").join(readDatasouces));
        readWriteProperties.setProperty("write-data-source-name", writeDatasource);
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration(
                        "default", "Static", readWriteProperties, "ROUND_ROBIN");
        ShardingSphereAlgorithmConfiguration algorithmConfiguration =
                new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties());
        Map<String, ShardingSphereAlgorithmConfiguration> sphereAlgorithmConfigurationMap = new HashMap<>(1);
        sphereAlgorithmConfigurationMap.put("round_robin", algorithmConfiguration);
        return new ReadwriteSplittingRuleConfiguration(
                Collections.singleton(dataSourceConfig),
                sphereAlgorithmConfigurationMap);
    }

}
