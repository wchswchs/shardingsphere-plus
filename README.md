# ShardingSphere-Plus
ShardingSphere-Plus is a toolkit which is compatible with ShardingSphere of version 4. 

## Features
-   Support custom sharding algorithm with parameters
-   Fully compatible with ShardingSphere of version 4
-   Support pluggable custom interface for sharding algorithm
-   Built in StrHash sharding algorithm with parameters

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
| Name                                              | Description                                                  |
| ------------------------------------------------- | ------------------------------------------------------------ |
| spring.sharding.datasource.jdbcUrl                | LogicDatabase url，for example：spring.sharding.datasource.jdbcUrl=jdbc:mysql://localhost:3306/test |
| spring.sharding.datasource.username               | for example：spring.sharding.datasource.username=test        |
| spring.sharding.datasource.password               | for example：spring.sharding.datasource.password=test        |
| spring.sharding.datasource.dbPartitionNum         | for example：spring.sharding.datasource.dbPartitionNum=32    |
| spring.sharding.datasource.logicTable             | for example：spring.sharding.datasource.logicTable=test_table |
| spring.sharding.datasource.tablePartitionNum      | for example：spring.sharding.datasource.tablePartitionNum=32 |
| spring.sharding.algorithm.shardingColumn          | for example：spring.sharding.algorithm.shardingColumn=order_id |
| spring.sharding.algorithm.algorithmName           | Sharding Standard Algorithm Name, it's your algorithm bean name, <br />for example：spring.sharding.algorithm.algorithmName=strHash |
| spring.sharding.algorithm.rangeAlgorithmName      | Sharding Range Algorithm Name, it's your algorithm bean name, <br />for example：spring.sharding.algorithm.rangeAlgorithmName=range |
| spring.sharding.algorithm.expression              | Sharding Inline Algorithm Name, it's your algorithm bean name, <br />for example：spring.sharding.algorithm.expression=t_order_item$->{order_id % 2} |
| spring.sharding.algorithm.dbAlgorithmName         | If not config spring.sharding.algorithm.algorithmName, can use it for database |
| spring.sharding.algorithm.dbRangeAlgorithmName    | If not config spring.sharding.algorithm.rangeAlgorithmName, can use it for database |
| spring.sharding.algorithm.tableAlgorithmName      | If not config spring.sharding.algorithm.algorithmName, can use it for table |
| spring.sharding.algorithm.tableRangeAlgorithmName | If not config spring.sharding.algorithm.rangeAlgorithmName, can use it for table |

## StrHash Sharding Algorithm
StrHash algorithm is a function that you can use to substring part of sharding column value to route table partition.
The following configuration are:
| Name                                         | Description                                          |
| -------------------------------------------- | ---------------------------------------------------- |
| spring.sharding.algorithm.strHash.startIndex | Start position of sharding column value to substring |
| spring.sharding.algorithm.strHash.endIndex   | End position of sharding column value to substring   |