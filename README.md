# ShardingSphere-Plus
ShardingSphere-Plus is a toolkit which is compatible with ShardingSphere of version 5. 

## Features
-   Support custom sharding algorithm with parameters
-   Simplify datasource when you make single database and multi tables sharding
-   Simplify sharding configuration when you make multi tables sharding
-   Support pluggable custom interface for sharding algorithm
-   Built in StrHash sharding algorithm with parameters using murmurhash to avoid data skew

## How to use

### Spring Boot
-   Add ShardingSphere-Plus Boot dependency
    - Latest Version: 1.0.0
    - Maven:
      ```xml
      <dependency>
          <groupId>com.shardingsphereplus</groupId>
          <artifactId>shardingsphere-plus-spring-boot-starter</artifactId>
          <version>Latest Version</version>
      </dependency>
      ```
-   Add Spring Boot Configuration Parameters

### Spring
-   Add ShardingSphere-Plus Spring dependency
    - Latest Version: 1.0.0
    - Maven:
      ```xml
      <dependency>
          <groupId>com.shardingsphereplus</groupId>
          <artifactId>shardingsphere-plus-spring</artifactId>
          <version>Latest Version</version>
      </dependency>
      ```
-   Add Spring Configuration Parameters

## Spring Boot Configuration

### Single Database and Multi Tables Sharding
```text
spring.sharding.algorithm.shardingColumn=user->name,user_ext->id    //format: tableName1->column,tableName2->column
spring.sharding.datasource.dbServer=localhost:3306
spring.sharding.datasource.serverTimeZone="UTC" //default: Asia/Shanghai
spring.sharding.datasource.characterEncoding=utf8 //default: utf8
spring.sharding.datasource.rewriteBatchedStatements=false //default: true
spring.sharding.datasource.logicDbName=user
spring.sharding.datasource.logicTable=user,user_ext
//if diff table has diff sharding algorithm
spring.sharding.algorithm.shardingTableAlgorithmName=user->StrHash[startIndex:1|endIndex:2],user_ext->INLINE    //format: tableName1->algorithm1,tableName2->algorithm2
//if diff table has same sharding algorithm
spring.sharding.algorithm.shardingTableAlgorithmName=StrHash
spring.sharding.datasource.username=test
spring.sharding.datasource.password=test
//if diff table has diff partition num
spring.sharding.datasource.tablePartitionNum=user->32,user_ext->16  //format: tableName1->num1,tableName2->num2
//if diff table has same partition num
spring.sharding.datasource.tablePartitionNum=32
//print actual sql
spring.sharding.sqlShow=true
```

### Multi Database and Multi Tables Sharding
```text
spring.sharding.algorithm.shardingColumn=user->name,user_ext->id
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

## Spring Configuration
XML Configuration: 
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphereplus="http://shardingsphereplus.com/schema/shardingsphereplus-spring"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://shardingsphereplus.com/schema/shardingsphereplus-spring
       http://shardingsphereplus.com/schema/shardingsphereplus-spring.xsd">
    <shardingsphereplus:datasource id="dataSource"
            db-server="localhost:3306"
            logic-dbname="user"
            username="test"
            password="test"
            logic-table="t_user"
            sharding-column="user_id"
            sharding-table-algorithm-name="StrHash"
            table-partition-num="64"
    />
</beans>
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
