package com.shardingsphereplus.sharding.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.sharding")
public class ShardingProperties {

    private final Algorithm algorithm = new Algorithm();
    private Datasource datasource = new Datasource();

    private String partitionJoinDelimiter = "_";
    private String sqlShow = "false";

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public Datasource getDatasource() {
        return datasource;
    }

    public void setDatasource(Datasource datasource) {
        this.datasource = datasource;
    }

    public String getPartitionJoinDelimiter() {
        return partitionJoinDelimiter;
    }

    public void setPartitionJoinDelimiter(String partitionJoinDelimiter) {
        this.partitionJoinDelimiter = partitionJoinDelimiter;
    }

    public String getSqlShow() {
        return sqlShow;
    }

    public void setSqlShow(String sqlShow) {
        this.sqlShow = sqlShow;
    }

    public static class Datasource {
        private String tablePartitionNum = "2";
        private String dbAddress;
        private String logicDbName;
        private String characterEncoding = "utf8";
        private boolean rewriteBatchedStatements = true;
        private String username;
        private String password;
        private String logicTable;
        private String writeDatasource;
        private String readDatasource;

        public String getTablePartitionNum() {
            return tablePartitionNum;
        }

        public void setTablePartitionNum(String tablePartitionNum) {
            this.tablePartitionNum = tablePartitionNum;
        }

        public String getDbAddress() {
            return dbAddress;
        }

        public void setDbAddress(String dbAddress) {
            this.dbAddress = dbAddress;
        }

        public String getLogicDbName() {
            return logicDbName;
        }

        public void setLogicDbName(String logicDbName) {
            this.logicDbName = logicDbName;
        }

        public String getCharacterEncoding() {
            return characterEncoding;
        }

        public void setCharacterEncoding(String characterEncoding) {
            this.characterEncoding = characterEncoding;
        }

        public boolean isRewriteBatchedStatements() {
            return rewriteBatchedStatements;
        }

        public void setRewriteBatchedStatements(boolean rewriteBatchedStatements) {
            this.rewriteBatchedStatements = rewriteBatchedStatements;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getLogicTable() {
            return logicTable;
        }

        public void setLogicTable(String logicTable) {
            this.logicTable = logicTable;
        }

        public String getWriteDatasource() {
            return writeDatasource;
        }

        public void setWriteDatasource(String writeDatasource) {
            this.writeDatasource = writeDatasource;
        }

        public String getReadDatasource() {
            return readDatasource;
        }

        public void setReadDatasource(String readDatasource) {
            this.readDatasource = readDatasource;
        }
    }

    public static class Algorithm {
        private String shardingColumn;
        private String shardingTableAlgorithmName = "StrHash";

        public String getShardingColumn() {
            return shardingColumn;
        }

        public void setShardingColumn(String shardingColumn) {
            this.shardingColumn = shardingColumn;
        }

        public String getShardingTableAlgorithmName() {
            return shardingTableAlgorithmName;
        }

        public void setShardingTableAlgorithmName(String shardingTableAlgorithmName) {
            this.shardingTableAlgorithmName = shardingTableAlgorithmName;
        }
    }

    public static class StrHash {
        private int startIndex = -1;
        private int endIndex = -1;

        public int getStartIndex() {
            return startIndex;
        }

        public void setEndIndex(int endIndex) {
            this.endIndex = endIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public void setStartIndex(int startIndex) {
            this.startIndex = startIndex;
        }
    }

}
