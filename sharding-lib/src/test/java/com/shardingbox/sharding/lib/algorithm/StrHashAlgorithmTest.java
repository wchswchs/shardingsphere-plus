package com.shardingbox.sharding.lib.algorithm;

import com.shardingbox.sharding.lib.algorithm.standard.StrHashAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class StrHashAlgorithmTest {

    private final String logicTable = "table";
    private final List<String> partitions = new ArrayList<>();
    private PreciseShardingValue<String> preciseShardingValue;

    private final StrHashAlgorithm strHashAlgorithm = new StrHashAlgorithm();

    @Test
    @DisplayName("截取子串分片测试")
    public void testDoSharding4Substring() {
        strHashAlgorithm.setEndIndex(2);
        Assertions.assertEquals(
                StringUtils.joinWith("_", logicTable, partitions.get(11)),
                strHashAlgorithm.doSharding(partitions, preciseShardingValue)
        );
    }

    @Test
    @DisplayName("整串分片测试")
    public void testDoSharding() {
        Assertions.assertEquals(
                StringUtils.joinWith("_", logicTable, partitions.get(17)),
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
        for (int i = 0; i < 32; i ++) {
            String partition = StringUtils.leftPad(String.valueOf(i), 3, '0');
            partitions.add(partition);
        }

        String columnName = "column1";
        String shardingValue = "S48c1jogpnd3";
        preciseShardingValue =
                new PreciseShardingValue<>(logicTable, columnName, shardingValue);

    }

}
