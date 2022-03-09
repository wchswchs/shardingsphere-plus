package com.shardingsphereplus.sharding.spring.boot.autoconfigure;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Splitter;
import com.shardingsphereplus.sharding.lib.algorithm.standard.StrHashAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.api.sharding.ShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(ShardingProperties.class)
public class ShardingAutoConfiguration {

    private final ShardingProperties properties;
    private final ApplicationContext context;

    public ShardingAutoConfiguration(ShardingProperties properties, ApplicationContext context) {
        this.properties = properties;
        this.context = context;
    }

    @Bean("strHash")
    public StrHashAlgorithm strHashAlgorithm() {
        return new StrHashAlgorithm(
                properties.getAlgorithm().getStrHash().getStartIndex(),
                properties.getAlgorithm().getStrHash().getEndIndex(),
                properties.getTableJoinDelimiter()
        );
    }

    @Bean
    public DataSource dataSource() throws Exception {
        Map<String, DataSource> dataSourceMap = new HashMap<>();

        if (StringUtils.isEmpty(properties.getDatasource().getJdbcUrl())) {
            throw new IllegalArgumentException("configuration item spring.sharding.datasource.jdbcUrl can not be empty");
        }
        if (StringUtils.isEmpty(properties.getDatasource().getLogicTable())) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.datasource.logicTable can not be empty"
            );
        }
        if (StringUtils.isEmpty(properties.getDatasource().getUsername())) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.datasource.username can not be empty"
            );
        }
        if (StringUtils.isEmpty(properties.getDatasource().getPassword())) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.datasource.password can not be empty"
            );
        }
        if (properties.getDatasource().getDbPartitionNum() < 1) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.datasource.tablePartitionNum can not lower than 1"
            );
        }
        if (properties.getDatasource().getTablePartitionNum() < 1) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.datasource.dbPartitionNum can not lower than 1"
            );
        }

        //basic datasource config
        String jdbcUrl = properties.getDatasource().getJdbcUrl();
        List<String> jdbcUrlUnits = Splitter.on("//").omitEmptyStrings().splitToList(jdbcUrl);
        String logicSchemaName = jdbcUrlUnits.get(1).split("/")[1];
        for (int i = 0; i < properties.getDatasource().getDbPartitionNum(); i ++) {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            if (properties.getDatasource().getDbPartitionNum() > 1) {
                String schemaName = logicSchemaName + i;
                dataSource.setUrl(jdbcUrl.replace(logicSchemaName, schemaName));
            } else {
                dataSource.setUrl(jdbcUrl);
            }
            dataSource.setUsername(properties.getDatasource().getUsername());
            dataSource.setPassword(properties.getDatasource().getPassword());
            dataSourceMap.put("ds" + i, dataSource);
        }

        //sharding table strategy config
        TableRuleConfiguration shardingTableRuleConfiguration = new TableRuleConfiguration(
                properties.getDatasource().getLogicTable(),
                String.format("ds${0..%d}.%s${0..%d}",
                        properties.getDatasource().getDbPartitionNum() - 1,
                        properties.getDatasource().getLogicTable(),
                        properties.getDatasource().getTablePartitionNum() - 1)
        );

        Object algorithmBean;
        try {
            algorithmBean = context.getBean(properties.getAlgorithm().getAlgorithmName());
            if (!(algorithmBean instanceof ShardingAlgorithm)) {
                throw new IllegalArgumentException(
                        "configuration item spring.sharding.algorithm.algorithmName not exist"
                );
            }
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.algorithm.algorithmName not exist"
            );
        }

        if (algorithmBean instanceof PreciseShardingAlgorithm) {
            if (StringUtils.isEmpty(properties.getAlgorithm().getRangeAlgorithmName())) {
                shardingTableRuleConfiguration.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(
                        properties.getAlgorithm().getShardingColumn(),
                        (PreciseShardingAlgorithm<?>) algorithmBean
                ));
            } else {
                Object rangeAlgorithmBean;
                try {
                    rangeAlgorithmBean = context.getBean(properties.getAlgorithm().getRangeAlgorithmName());
                    if (!(rangeAlgorithmBean instanceof RangeShardingAlgorithm)) {
                        throw new IllegalArgumentException(
                                "configuration item spring.sharding.algorithm.rangeAlgorithmName not exist"
                        );
                    }
                } catch (NoSuchBeanDefinitionException e) {
                    throw new IllegalArgumentException(
                            "configuration item spring.sharding.algorithm.rangeAlgorithmName not exist"
                    );
                }
                shardingTableRuleConfiguration.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(
                        properties.getAlgorithm().getShardingColumn(),
                        (PreciseShardingAlgorithm<?>) algorithmBean,
                        (RangeShardingAlgorithm<?>) rangeAlgorithmBean
                ));
            }
        }
        if (algorithmBean instanceof ComplexKeysShardingAlgorithm) {
            shardingTableRuleConfiguration.setTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration(
                    properties.getAlgorithm().getShardingColumn(),
                    (ComplexKeysShardingAlgorithm<?>) algorithmBean
            ));
        }
        if (StringUtils.isNotEmpty(properties.getAlgorithm().getExpression())) {
            shardingTableRuleConfiguration.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration(
                    properties.getAlgorithm().getShardingColumn(),
                    properties.getAlgorithm().getExpression()
            ));
        }
        if (algorithmBean instanceof HintShardingAlgorithm) {
            shardingTableRuleConfiguration.setTableShardingStrategyConfig(new HintShardingStrategyConfiguration(
                    (HintShardingAlgorithm<?>) algorithmBean
            ));
        }

        //sharding rule config
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getTableRuleConfigs().add(shardingTableRuleConfiguration);

        return ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfiguration, new Properties());
    }

}
