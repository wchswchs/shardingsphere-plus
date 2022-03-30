package com.shardingsphereplus.spring;

import com.shardingsphereplus.config.configuration.DatasourceConfiguration;
import com.shardingsphereplus.config.configuration.rule.ReadWriteRuleConfiguration;
import com.shardingsphereplus.config.configuration.rule.ShardingConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.AlgorithmRuleConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.TableRuleConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.table.ActualNodesConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.table.ShardingAlgorithmConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.table.ShardingColumnConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.w3c.dom.Element;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String ATTRIBUTE_CHARACTER_ENCODING = "character-encoding";
    private static final String ATTRIBUTE_DB_SERVER = "db-server";
    private static final String ATTRIBUTE_LOGIC_DB_NAME = "logic-dbname";
    private static final String ATTRIBUTE_LOGIC_TABLE = "logic-table";
    private static final String ATTRIBUTE_USERNAME = "username";
    private static final String ATTRIBUTE_PASSWORD = "password";
    private static final String ATTRIBUTE_READ_DATASOURCE = "read-datasource";
    private static final String ATTRIBUTE_WRITE_DATASOURCE = "write-datasource";
    private static final String ATTRIBUTE_REWRITE_BATCHED_STATEMENTS = "rewrite-batched-statements";
    private static final String ATTRIBUTE_SHARDING_DATASOURCE = "sharding-datasource";
    private static final String ATTRIBUTE_TABLE_PARTITION_NUM = "table-partition-num";
    private static final String ATTRIBUTE_SHARDING_COLUMN = "sharding-column";
    private static final String ATTRIBUTE_SHARDING_TABLE_ALGORITHM = "sharding-table-algorithm-name";
    private static final String ATTRIBUTE_SHARDING_JOIN_DELIMITER = "join-delimiter";
    private static final String ATTRIBUTE_SQL_SHOW = "sql-show";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        if (StringUtils.isEmpty(element.getAttribute(ATTRIBUTE_TABLE_PARTITION_NUM))
                || Integer.parseInt(element.getAttribute(ATTRIBUTE_TABLE_PARTITION_NUM)) < 0) {
            throw new IllegalArgumentException(
                    String.format("configuration item [%s] can not be empty or lower than 0", ATTRIBUTE_TABLE_PARTITION_NUM)
            );
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DataSource.class, () -> {
            String[] dbServers = element.getAttribute(ATTRIBUTE_DB_SERVER).split(",");
            String logicDbName = element.getAttribute(ATTRIBUTE_LOGIC_DB_NAME);
            String[] logicTables = element.getAttribute(ATTRIBUTE_LOGIC_TABLE).split(",");
            String[] shardingTableAlgorithms = element.getAttribute(ATTRIBUTE_SHARDING_TABLE_ALGORITHM).split(",");
            String[] shardingColumns = element.getAttribute(ATTRIBUTE_SHARDING_COLUMN).split(",");
            String[] tablePartitionNumList = element.getAttribute(ATTRIBUTE_TABLE_PARTITION_NUM).split(",");
            boolean rewriteBatchedStatements = "true".equals(element.getAttribute(ATTRIBUTE_REWRITE_BATCHED_STATEMENTS));

            try {
                Map<String, DataSource> dataSourceMap = new DatasourceConfiguration(
                        logicDbName, dbServers,
                        element.getAttribute(ATTRIBUTE_USERNAME),
                        element.getAttribute(ATTRIBUTE_PASSWORD),
                        element.getAttribute(ATTRIBUTE_CHARACTER_ENCODING),
                        rewriteBatchedStatements,
                        element.getAttribute(ATTRIBUTE_SHARDING_JOIN_DELIMITER)).build();

                List<ShardingTableRuleConfiguration> tableRuleConfigurations = new TableRuleConfiguration(
                        new ActualNodesConfiguration(
                                logicTables, logicDbName, tablePartitionNumList,
                                element.getAttribute(ATTRIBUTE_SHARDING_DATASOURCE),
                                element.getAttribute(ATTRIBUTE_SHARDING_JOIN_DELIMITER)).build(),
                        new ShardingColumnConfiguration(logicTables, shardingColumns).build(),
                        new ShardingAlgorithmConfiguration(logicTables, shardingTableAlgorithms).build()
                ).build();
                Map<String, ShardingSphereAlgorithmConfiguration> shardingAlgorithmConfigurations =
                        new AlgorithmRuleConfiguration(shardingTableAlgorithms, element.getAttribute(ATTRIBUTE_SHARDING_JOIN_DELIMITER)).build();
                ShardingRuleConfiguration shardingRuleConfiguration = new ShardingConfiguration(tableRuleConfigurations, shardingAlgorithmConfigurations).build();

                List<RuleConfiguration> ruleConfigurations = new ArrayList<>();
                ruleConfigurations.add(shardingRuleConfiguration);
                if (StringUtils.isNotEmpty(element.getAttribute(ATTRIBUTE_READ_DATASOURCE))
                        && StringUtils.isNotEmpty(element.getAttribute(ATTRIBUTE_READ_DATASOURCE))) {
                    ruleConfigurations.add(new ReadWriteRuleConfiguration(
                            element.getAttribute(ATTRIBUTE_READ_DATASOURCE),
                            element.getAttribute(ATTRIBUTE_WRITE_DATASOURCE),
                            element.getAttribute(ATTRIBUTE_SHARDING_JOIN_DELIMITER),
                            logicDbName).build());
                }

                //build common properties
                Properties commonProperties = new Properties();
                commonProperties.setProperty("sql-show", element.getAttribute(ATTRIBUTE_SQL_SHOW));

                return ShardingSphereDataSourceFactory.createDataSource(logicDbName, dataSourceMap, ruleConfigurations, commonProperties);
            } catch (Exception ex) {
                XmlReaderContext readerContext = parserContext.getReaderContext();
                readerContext.error(ex.getMessage(), readerContext.extractSource(element), ex.getCause());
            }
            return null;
        });

        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        return builder.getBeanDefinition();
    }

}
