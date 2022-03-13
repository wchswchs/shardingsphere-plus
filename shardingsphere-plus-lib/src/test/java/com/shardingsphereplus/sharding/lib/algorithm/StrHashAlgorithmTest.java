package com.shardingsphereplus.sharding.lib.algorithm;

import com.shardingsphereplus.sharding.lib.algorithm.standard.StrHashAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class StrHashAlgorithmTest {

    private final List<String> partitions = new ArrayList<>();
    private PreciseShardingValue<String> preciseShardingValue;

    private final StrHashAlgorithm strHashAlgorithm = new StrHashAlgorithm();

    @Test
    @DisplayName("截取子串分片测试")
    public void testDoSharding4Substring() {
        strHashAlgorithm.setEndIndex(2);
        Assertions.assertEquals(
                partitions.get(27),
                strHashAlgorithm.doSharding(partitions, preciseShardingValue)
        );
    }

    @Test
    @DisplayName("整串分片测试")
    public void testDoSharding() {
        Assertions.assertEquals(
                partitions.get(20),
                strHashAlgorithm.doSharding(partitions, preciseShardingValue)
        );
    }

    @Test
    @DisplayName("空分片字段异常测试")
    public void testEmptyException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            strHashAlgorithm.doSharding(partitions, (PreciseShardingValue<String>) null);
        });
    }

    @BeforeEach
    public void setUp() {
        String logicTable = "table";
        for (int i = 0; i < 32; i ++) {
            partitions.add(logicTable + "_" + i);
        }
        String columnName = "column1";
        String shardingValue = "P0001";
        preciseShardingValue =
                new PreciseShardingValue<>(logicTable, columnName, shardingValue);

    }

}
