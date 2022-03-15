package com.shardingsphereplus.sharding.spring.boot.autoconfigure.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.shardingsphereplus.sharding.spring.boot.autoconfigure.Configuration;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class DatasourceConfiguration implements Configuration<Map<String, DataSource>> {

    private final String logicDbName;
    private final String[] dbServers;
    private final String characterEncoding;
    private final boolean rewriteBatchedStatements;
    private final String partitionJoinDelimiter;
    private final String username;
    private final String password;

    public DatasourceConfiguration(String logicDbName, String[] dbServers, String username, String password) {
        this(logicDbName, dbServers, username, password, "utf8", true, "_");
    }

    public DatasourceConfiguration(String logicDbName,
                                   String[] dbServers,
                                   String username,
                                   String password,
                                   String characterEncoding,
                                   boolean rewriteBatchedStatements,
                                   String partitionJoinDelimiter) {
        this.logicDbName = logicDbName;
        this.dbServers = dbServers;
        this.username = username;
        this.password = password;
        this.characterEncoding = characterEncoding;
        this.rewriteBatchedStatements = rewriteBatchedStatements;
        this.partitionJoinDelimiter = partitionJoinDelimiter;
    }

    @Override
    public Map<String, DataSource> build() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        int i = 0;
        for (String address : dbServers) {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            String schemaName = logicDbName + partitionJoinDelimiter + i;
            String jdbcAddress = StringUtils.joinWith("//", "jdbc:mysql:", address);
            String jdbcUrl = StringUtils.joinWith("/", jdbcAddress, logicDbName);
            String characterEncodingParam = StringUtils.joinWith("=", "characterEncoding", characterEncoding);
            String rewriteBatchedStatementsParam = StringUtils.joinWith("=", "rewriteBatchedStatements", rewriteBatchedStatements);
            String jdbcParams = StringUtils.joinWith("&", characterEncodingParam, rewriteBatchedStatementsParam);
            dataSource.setUrl(StringUtils.joinWith("?", jdbcUrl, jdbcParams));
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSourceMap.put(schemaName, dataSource);

            i ++;
        }
        return dataSourceMap;
    }

}
