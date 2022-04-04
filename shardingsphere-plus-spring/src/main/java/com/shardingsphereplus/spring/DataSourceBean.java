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
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DataSourceBean implements FactoryBean<DataSource>, InitializingBean {

    private String dbServer;
    private String serverTimeZone;
    private String characterEncoding;
    private String logicDbName;
    private String logicTable;
    private String username;
    private String password;
    private String readDatasource;
    private String writeDatasource;
    private String rewriteBatchedStatements;
    private String shardingDatasource;
    private String tablePartitionNum;
    private String shardingColumn;
    private String shardingTableAlgorithmName;
    private String joinDelimiter;
    private String sqlShow;

    private DataSource dataSource;

    @Override
    public DataSource getObject() {
        return dataSource;
    }

    @Override
    public Class<?> getObjectType() {
        return dataSource.getClass();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.dataSource = buildDataSource();
    }

    private DataSource buildDataSource() throws SQLException {
        String[] dbServers = dbServer.split(",");
        String[] logicTables = logicTable.split(",");
        String[] shardingTableAlgorithms = shardingTableAlgorithmName.split(",");
        String[] shardingColumns = shardingColumn.split(",");
        String[] tablePartitionNumList = tablePartitionNum.split(",");
        boolean rewriteBatchedStatementsBoolean = "true".equals(rewriteBatchedStatements);

        Map<String, DataSource> dataSourceMap = new DatasourceConfiguration(
                logicDbName, dbServers,
                username,
                password,
                serverTimeZone,
                characterEncoding,
                rewriteBatchedStatementsBoolean,
                joinDelimiter).build();

        List<ShardingTableRuleConfiguration> tableRuleConfigurations = new TableRuleConfiguration(
                new ActualNodesConfiguration(
                        logicTables, logicDbName, tablePartitionNumList,
                        shardingDatasource,
                        joinDelimiter).build(),
                new ShardingColumnConfiguration(logicTables, shardingColumns).build(),
                new ShardingAlgorithmConfiguration(logicTables, shardingTableAlgorithms).build()
        ).build();
        Map<String, ShardingSphereAlgorithmConfiguration> shardingAlgorithmConfigurations =
                new AlgorithmRuleConfiguration(shardingTableAlgorithms, joinDelimiter).build();
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingConfiguration(tableRuleConfigurations, shardingAlgorithmConfigurations).build();

        List<RuleConfiguration> ruleConfigurations = new ArrayList<>();
        ruleConfigurations.add(shardingRuleConfiguration);
        if (StringUtils.isNotEmpty(readDatasource)
                && StringUtils.isNotEmpty(readDatasource)) {
            ruleConfigurations.add(new ReadWriteRuleConfiguration(
                    readDatasource,
                    writeDatasource,
                    joinDelimiter,
                    logicDbName).build());
        }

        //build common properties
        Properties commonProperties = new Properties();
        commonProperties.setProperty("sql-show", sqlShow);

        return ShardingSphereDataSourceFactory.createDataSource(logicDbName, dataSourceMap, ruleConfigurations, commonProperties);
    }

    public void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }

    public void setServerTimeZone(String serverTimeZone) {
        this.serverTimeZone = serverTimeZone;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public void setLogicDbName(String logicDbName) {
        this.logicDbName = logicDbName;
    }

    public void setLogicTable(String logicTable) {
        this.logicTable = logicTable;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setReadDatasource(String readDatasource) {
        this.readDatasource = readDatasource;
    }

    public void setWriteDatasource(String writeDatasource) {
        this.writeDatasource = writeDatasource;
    }

    public void setRewriteBatchedStatements(String rewriteBatchedStatements) {
        this.rewriteBatchedStatements = rewriteBatchedStatements;
    }

    public void setShardingDatasource(String shardingDatasource) {
        this.shardingDatasource = shardingDatasource;
    }

    public void setTablePartitionNum(String tablePartitionNum) {
        this.tablePartitionNum = tablePartitionNum;
    }

    public void setShardingColumn(String shardingColumn) {
        this.shardingColumn = shardingColumn;
    }

    public void setShardingTableAlgorithmName(String shardingTableAlgorithmName) {
        this.shardingTableAlgorithmName = shardingTableAlgorithmName;
    }

    public void setJoinDelimiter(String joinDelimiter) {
        this.joinDelimiter = joinDelimiter;
    }

    public void setSqlShow(String sqlShow) {
        this.sqlShow = sqlShow;
    }

}
