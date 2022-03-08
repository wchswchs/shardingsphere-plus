package com.shardingbox.sharding.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.sharding")
public class ShardingProperties {

    private final Algorithm algorithm = new Algorithm();
    private String tableJoinDelimiter = "_";
    private Datasource datasource = new Datasource();

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public String getTableJoinDelimiter() {
        return tableJoinDelimiter;
    }

    public void setTableJoinDelimiter(String tableJoinDelimiter) {
        this.tableJoinDelimiter = tableJoinDelimiter;
    }

    public Datasource getDatasource() {
        return datasource;
    }

    public void setDatasource(Datasource datasource) {
        this.datasource = datasource;
    }

    public static class Datasource {
        private int tablePartitionNum = 2;
        private int dbPartitionNum = 1;
        private String jdbcUrl;
        private String username;
        private String password;
        private String logicTable;

        public int getTablePartitionNum() {
            return tablePartitionNum;
        }

        public void setTablePartitionNum(int tablePartitionNum) {
            this.tablePartitionNum = tablePartitionNum;
        }

        public int getDbPartitionNum() {
            return dbPartitionNum;
        }

        public void setDbPartitionNum(int dbPartitionNum) {
            this.dbPartitionNum = dbPartitionNum;
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
        private String algorithmName = "strHash";
        private final StrHash strHash = new StrHash();

        public StrHash getStrHash() {
            return strHash;
        }

        public String getShardingColumn() {
            return shardingColumn;
        }

        public void setShardingColumn(String shardingColumn) {
            this.shardingColumn = shardingColumn;
        }

        public String getAlgorithmName() {
            return algorithmName;
        }

        public void setAlgorithmName(String algorithmName) {
            this.algorithmName = algorithmName;
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
