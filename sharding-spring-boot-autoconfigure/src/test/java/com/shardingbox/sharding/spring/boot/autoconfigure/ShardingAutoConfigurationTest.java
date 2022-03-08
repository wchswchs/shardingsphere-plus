package com.shardingbox.sharding.spring.boot.autoconfigure;

import com.shardingbox.sharding.lib.algorithm.standard.StrHashAlgorithm;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ShardingAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void init() {
        this.context = new AnnotationConfigApplicationContext();
        TestPropertyValues.of("spring.sharding.datasource.jdbcUrl:jdbc:mysql://localhost:3306/user_center").applyTo(this.context);
        TestPropertyValues.of("spring.sharding.datasource.logicTable:user").applyTo(this.context);
        TestPropertyValues.of("spring.sharding.datasource.username:root").applyTo(this.context);
        TestPropertyValues.of("spring.sharding.datasource.password:123456").applyTo(this.context);
    }

    @AfterEach
    void closeContext() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    @DisplayName("分片算法配置参数测试")
    public void testAlgorithm() {
        TestPropertyValues.of("spring.sharding.algorithm.strHash.endIndex:2").applyTo(context);
        context.register(ShardingAutoConfiguration.class);
        context.refresh();
        Assertions.assertThat(context.getBean(StrHashAlgorithm.class).getEndIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("逻辑表连接配置参数测试")
    public void testTableJoinDelimiter() {
        TestPropertyValues.of("spring.sharding.tableJoinDelimiter:-").applyTo(context);
        context.register(ShardingAutoConfiguration.class);
        context.refresh();
        Assertions.assertThat(context.getBean(StrHashAlgorithm.class).getPartitionJoinDelimiter()).isEqualTo("-");
    }

    @Test
    @DisplayName("无配置参数测试")
    public void testNon() {
        context.register(ShardingAutoConfiguration.class);
        context.refresh();
        Assertions.assertThat(context.getBean(StrHashAlgorithm.class).getPartitionJoinDelimiter()).isEqualTo("_");
        Assertions.assertThat(context.getBean(StrHashAlgorithm.class).getStartIndex()).isEqualTo(-1);
        Assertions.assertThat(context.getBean(StrHashAlgorithm.class).getEndIndex()).isEqualTo(-1);
    }

}
