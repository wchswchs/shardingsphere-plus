# ShardingSphere-Plus
ShardingSphere-Plus is a toolkit which is compatible with ShardingSphere of version 5. 

## Features
-   Support custom sharding algorithm with parameters
-   Simplify datasource when you make single database and multi tables sharding
-   Simplify sharding configuration when you make multi tables sharding
-   Support pluggable custom interface for sharding algorithm
-   Built in StrHash sharding algorithm with parameters using murmurhash to avoid data skew

## How to use
-   Add ShardingSphere-Plus dependency
    - Latest Version: 1.0.0
    - Maven:
      ```xml
      <dependency>
          <groupId>com.shardingsphereplus</groupId>
          <artifactId>shardingsphere-plus-spring-boot-starter</artifactId>
          <version>Latest Version</version>
      </dependency>
      ```
-   Inject your custom extension bean for ShardingSphere
-   Add Configuration Parameters

## Spring Boot Configuration Parameters

### Single Database and Multi Tables Sharding
```text
spring.sharding.algorithm.shardingColumn=name
spring.sharding.datasource.dbServer=localhost:3306,localhost:3307,localhost:3308
spring.sharding.datasource.characterEncoding=utf8 //default: utf8
spring.sharding.datasource.rewriteBatchedStatements=false //default: true
spring.sharding.datasource.logicDbName=user
spring.sharding.datasource.shardingDatasource=0..2 //0: localhost:3306, 2: localhost:3308
spring.sharding.datasource.logicTable=user,user_ext
//if diff table has diff sharding algorithm
spring.sharding.algorithm.shardingTableAlgorithmName=user->StrHash[startIndex:1|endIndex:2],user_ext->INLINE
//if diff table has same sharding algorithm
spring.sharding.algorithm.shardingTableAlgorithmName=StrHash
spring.sharding.datasource.username=test
spring.sharding.datasource.password=test
//if diff table has diff partition num
spring.sharding.datasource.tablePartitionNum=user->32,user_ext->16
//if diff table has same partition num
spring.sharding.datasource.tablePartitionNum=32
//print actual sql
spring.sharding.sqlShow=true
```

### Read Write Splitter
```text
spring.sharding.algorithm.shardingColumn=name
spring.sharding.datasource.dbServer=localhost:3306,localhost:3307,localhost:3308
spring.sharding.datasource.characterEncoding=utf8 //default: utf8
spring.sharding.datasource.rewriteBatchedStatements=false //default: true
spring.sharding.datasource.logicDbName=user
spring.sharding.datasource.writeDatasource=0 //0: localhost:3306
spring.sharding.datasource.readDatasource=1,2 //1: localhost:3307, 2: localhost:3308
spring.sharding.datasource.username=test
spring.sharding.datasource.password=test
//print actual sql
spring.sharding.sqlShow=true
```

## StrHash Sharding Algorithm
StrHash algorithm is a function that you can use to substring part of sharding column value to route table partition.

### Purpose
Make data in partitions sharded more evenly than other hash algorithms in ShardingSphere.

### Configuration
The following configuration are:
```text
spring.sharding.algorithm.shardingTableAlgorithmName=user->StrHash[startIndex:1|endIndex:2],user_ext->INLINE
```
### Benchmark
```text
Benchmark                              Mode  Cnt         Score         Error  Units
StrHashAlgorithmBenchmark.doSharding  thrpt    5  10222628.045 ± 1914287.262  ops/s
StrHashAlgorithmBenchmark.doSharding   avgt    5        ≈ 10⁻⁶                 s/op
```
