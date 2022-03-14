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
        private int dbPartitionNum = 1;
        private String tablePartitionNum = "2";
        private String jdbcUrl;
        private String username;
        private String password;
        private String logicTable;

        public int getDbPartitionNum() {
            return dbPartitionNum;
        }

        public void setDbPartitionNum(int dbPartitionNum) {
            this.dbPartitionNum = dbPartitionNum;
        }

        public String getTablePartitionNum() {
            return tablePartitionNum;
        }

        public void setTablePartitionNum(String tablePartitionNum) {
            this.tablePartitionNum = tablePartitionNum;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public void setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
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
    }

    public static class Algorithm {
        private String shardingColumn;
        private String shardingAlgorithmName = "StrHash";

        public String getShardingColumn() {
            return shardingColumn;
        }

        public void setShardingColumn(String shardingColumn) {
            this.shardingColumn = shardingColumn;
        }

        public String getShardingAlgorithmName() {
            return shardingAlgorithmName;
        }

        public void setShardingAlgorithmName(String shardingAlgorithmName) {
            this.shardingAlgorithmName = shardingAlgorithmName;
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
