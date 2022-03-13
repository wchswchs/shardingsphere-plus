# ShardingSphere-Plus
ShardingSphere-Plus is a toolkit which is compatible with ShardingSphere of version 5. 

## Features
-   Support custom sharding algorithm with parameters
-   Simplify datasource configuration
-   Support pluggable custom interface for sharding algorithm
-   Built in StrHash sharding algorithm with parameters using murmurhash

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
```xml
spring.sharding.algorithm.shardingColumn=name
spring.sharding.algorithm.strHash.startIndex=1
spring.sharding.algorithm.strHash.endIndex=3
spring.sharding.datasource.jdbcUrl=jdbc:mysql://localhost:3306/user
spring.sharding.datasource.logicTable=user,user_ext
spring.sharding.algorithm.AlgortihmName=user->StrHash[startIndex:1|endIndex:2],user_ext->INLINE
spring.sharding.datasource.password=test
spring.sharding.datasource.tablePartitionNum=3
spring.sharding.datasource.username=test
//print actual sql
spring.sharding.sqlShow=true
```

## StrHash Sharding Algorithm
StrHash algorithm is a function that you can use to substring part of sharding column value to route table partition.

### Purpose
Make data in partitions sharded more evenly than other hash algorithms in ShardingSphere.

### Configuration
The following configuration are:
```xml
spring.sharding.algorithm.AlgortihmName=user->StrHash[startIndex:1|endIndex:2],user_ext->INLINE
```
