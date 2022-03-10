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
        if (StringUtils.isEmpty(properties.getAlgorithm().getAlgorithmName())
        || StringUtils.isEmpty(properties.getAlgorithm().getDbAlgorithmName())
        || StringUtils.isEmpty(properties.getAlgorithm().getTableAlgorithm())) {
            throw new IllegalArgumentException(
                    "one of configuration item spring.sharding.algorithm.[algorithm/dbAlgorithm/tableAlgorithm] must has value"
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

        //build sharding algorithm
        String dbAlgorithm = properties.getAlgorithm().getAlgorithmName();
        String dbRangeAlgorithm = properties.getAlgorithm().getRangeAlgorithmName();
        if (StringUtils.isNotEmpty(properties.getAlgorithm().getDbAlgorithmName())) {
            dbAlgorithm = properties.getAlgorithm().getDbAlgorithmName();
        }
        if (StringUtils.isNotEmpty(properties.getAlgorithm().getDbRangeAlgorithmName())) {
            dbRangeAlgorithm = properties.getAlgorithm().getDbRangeAlgorithmName();
        }
        buildShardingAlgorithmConfiguration(shardingTableRuleConfiguration, dbAlgorithm, dbRangeAlgorithm, "database");

        String tableAlgorithm = properties.getAlgorithm().getAlgorithmName();
        String tableRangeAlgorithm = properties.getAlgorithm().getRangeAlgorithmName();
        if (StringUtils.isNotEmpty(properties.getAlgorithm().getTableAlgorithm())) {
            tableAlgorithm = properties.getAlgorithm().getTableAlgorithm();
        }
        if (StringUtils.isNotEmpty(properties.getAlgorithm().getTableRangeAlgorithm())) {
            tableRangeAlgorithm = properties.getAlgorithm().getTableRangeAlgorithm();
        }
        buildShardingAlgorithmConfiguration(shardingTableRuleConfiguration, tableAlgorithm, tableRangeAlgorithm, "table");

        //sharding rule config
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getTableRuleConfigs().add(shardingTableRuleConfiguration);

        return ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfiguration, new Properties());
    }

    private void buildShardingAlgorithmConfiguration(TableRuleConfiguration shardingTableRuleConfiguration, String algorithm, String rangeAlgorithm, String type) {
        Object algorithmBean;
        try {
            algorithmBean = context.getBean(algorithm);
            if (!(algorithmBean instanceof ShardingAlgorithm)) {
                throw new IllegalArgumentException(
                        "one of configuration item spring.sharding.algorithm.[algorithmName/tableAlgorithm] not exist"
                );
            }
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalArgumentException(
                    "configuration item spring.sharding.algorithm.algorithmName not exist"
            );
        }

        if (algorithmBean instanceof PreciseShardingAlgorithm) {
            if (StringUtils.isEmpty(rangeAlgorithm)) {
                if ("database".equals(type)) {
                    shardingTableRuleConfiguration.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration(
                            properties.getAlgorithm().getShardingColumn(),
                            (PreciseShardingAlgorithm<?>) algorithmBean
                    ));
                } else {
                    shardingTableRuleConfiguration.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(
                            properties.getAlgorithm().getShardingColumn(),
                            (PreciseShardingAlgorithm<?>) algorithmBean
                    ));
                }
            } else {
                Object rangeAlgorithmBean;
                try {
                    rangeAlgorithmBean = context.getBean(rangeAlgorithm);
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
                if ("database".equals(type)) {
                    shardingTableRuleConfiguration.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration(
                            properties.getAlgorithm().getShardingColumn(),
                            (PreciseShardingAlgorithm<?>) algorithmBean,
                            (RangeShardingAlgorithm<?>) rangeAlgorithmBean
                    ));
                } else {
                    shardingTableRuleConfiguration.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(
                            properties.getAlgorithm().getShardingColumn(),
                            (PreciseShardingAlgorithm<?>) algorithmBean,
                            (RangeShardingAlgorithm<?>) rangeAlgorithmBean
                    ));
                }
            }
        }
        if (algorithmBean instanceof ComplexKeysShardingAlgorithm) {
            if ("database".equals(type)) {
                shardingTableRuleConfiguration.setDatabaseShardingStrategyConfig(new ComplexShardingStrategyConfiguration(
                        properties.getAlgorithm().getShardingColumn(),
                        (ComplexKeysShardingAlgorithm<?>) algorithmBean
                ));
            } else {
                shardingTableRuleConfiguration.setTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration(
                        properties.getAlgorithm().getShardingColumn(),
                        (ComplexKeysShardingAlgorithm<?>) algorithmBean
                ));
            }
        }
        if (StringUtils.isNotEmpty(properties.getAlgorithm().getExpression())) {
            if ("database".equals(type)) {
                shardingTableRuleConfiguration.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration(
                        properties.getAlgorithm().getShardingColumn(),
                        properties.getAlgorithm().getExpression()
                ));
            } else {
                shardingTableRuleConfiguration.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration(
                        properties.getAlgorithm().getShardingColumn(),
                        properties.getAlgorithm().getExpression()
                ));
            }
        }
        if (algorithmBean instanceof HintShardingAlgorithm) {
            if ("database".equals(type)) {
                shardingTableRuleConfiguration.setDatabaseShardingStrategyConfig(new HintShardingStrategyConfiguration(
                        (HintShardingAlgorithm<?>) algorithmBean
                ));
            } else {
                shardingTableRuleConfiguration.setTableShardingStrategyConfig(new HintShardingStrategyConfiguration(
                        (HintShardingAlgorithm<?>) algorithmBean
                ));
            }
        }
    }

}
