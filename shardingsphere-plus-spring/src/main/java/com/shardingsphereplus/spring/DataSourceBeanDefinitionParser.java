package com.shardingsphereplus.spring;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

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
        if (StringUtils.isEmpty(element.getAttribute(ATTRIBUTE_TABLE_PARTITION_NUM))) {
            throw new IllegalArgumentException(
                    String.format("configuration item [%s] can not be empty", ATTRIBUTE_TABLE_PARTITION_NUM)
            );
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DataSourceBean.class);
        builder.addPropertyValue("dbServer", element.getAttribute(ATTRIBUTE_DB_SERVER));
        builder.addPropertyValue("characterEncoding", element.getAttribute(ATTRIBUTE_CHARACTER_ENCODING));
        builder.addPropertyValue("logicDbName", element.getAttribute(ATTRIBUTE_LOGIC_DB_NAME));
        builder.addPropertyValue("logicTable", element.getAttribute(ATTRIBUTE_LOGIC_TABLE));
        builder.addPropertyValue("username", element.getAttribute(ATTRIBUTE_USERNAME));
        builder.addPropertyValue("password", element.getAttribute(ATTRIBUTE_PASSWORD));
        builder.addPropertyValue("readDatasource", element.getAttribute(ATTRIBUTE_READ_DATASOURCE));
        builder.addPropertyValue("writeDatasource", element.getAttribute(ATTRIBUTE_WRITE_DATASOURCE));
        builder.addPropertyValue("rewriteBatchedStatements", element.getAttribute(ATTRIBUTE_REWRITE_BATCHED_STATEMENTS));
        builder.addPropertyValue("shardingDatasource", element.getAttribute(ATTRIBUTE_SHARDING_DATASOURCE));
        builder.addPropertyValue("tablePartitionNum", element.getAttribute(ATTRIBUTE_TABLE_PARTITION_NUM));
        builder.addPropertyValue("shardingColumn", element.getAttribute(ATTRIBUTE_SHARDING_COLUMN));
        builder.addPropertyValue("shardingTableAlgorithmName", element.getAttribute(ATTRIBUTE_SHARDING_TABLE_ALGORITHM));
        builder.addPropertyValue("joinDelimiter", element.getAttribute(ATTRIBUTE_SHARDING_JOIN_DELIMITER));
        builder.addPropertyValue("sqlShow", element.getAttribute(ATTRIBUTE_SQL_SHOW));
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        return builder.getBeanDefinition();
    }

}
