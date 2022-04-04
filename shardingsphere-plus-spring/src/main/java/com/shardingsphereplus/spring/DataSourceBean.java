package com.shardingsphereplus.spring;

import com.shardingsphereplus.config.DataSourceFactory;
import com.shardingsphereplus.config.configuration.DatasourceConfiguration;
import com.shardingsphereplus.config.configuration.rule.ReadWriteRuleConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.AlgorithmRuleConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.table.ActualNodesConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.table.ShardingAlgorithmConfiguration;
import com.shardingsphereplus.config.configuration.rule.sharding.table.ShardingColumnConfiguration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;

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

    private DataSource buildDataSource() throws Exception {
        String[] dbServers = dbServer.split(",");
        String[] logicTables = logicTable.split(",");
        String[] shardingTableAlgorithms = shardingTableAlgorithmName.split(",");
        String[] shardingColumns = shardingColumn.split(",");
        String[] tablePartitionNumList = tablePartitionNum.split(",");
        boolean rewriteBatchedStatementsBoolean = "true".equals(rewriteBatchedStatements);

        DatasourceConfiguration datasourceConfiguration = new DatasourceConfiguration(
                logicDbName, dbServers,
                username,
                password,
                serverTimeZone,
                characterEncoding,
                rewriteBatchedStatementsBoolean,
                joinDelimiter);
        ActualNodesConfiguration actualNodesConfiguration = new ActualNodesConfiguration(
                logicTables, logicDbName, tablePartitionNumList,
                shardingDatasource,
                joinDelimiter);
        ShardingColumnConfiguration shardingColumnConfiguration = new ShardingColumnConfiguration(logicTables, shardingColumns);
        ShardingAlgorithmConfiguration shardingAlgorithmConfiguration = new ShardingAlgorithmConfiguration(logicTables, shardingTableAlgorithms);
        AlgorithmRuleConfiguration algorithmRuleConfiguration =
                new AlgorithmRuleConfiguration(shardingTableAlgorithms, joinDelimiter);
        ReadWriteRuleConfiguration readWriteRuleConfiguration = new ReadWriteRuleConfiguration(
                readDatasource,
                writeDatasource,
                joinDelimiter,
                logicDbName);

        //build common properties
        DataSourceFactory.CommonConfig commonConfig = new DataSourceFactory.CommonConfig(sqlShow);
        DataSourceFactory.Condition condition = new DataSourceFactory.Condition(shardingColumns, logicTables, logicDbName);

        return DataSourceFactory.build(
                datasourceConfiguration,
                actualNodesConfiguration,
                shardingColumnConfiguration,
                shardingAlgorithmConfiguration,
                readWriteRuleConfiguration,
                algorithmRuleConfiguration,
                condition, commonConfig, logicDbName
        );
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
