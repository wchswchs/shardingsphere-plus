package com.shardingbox.sharding.benchmark.algorithm;

import com.shardingbox.sharding.lib.algorithm.standard.StrHashAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@State(Scope.Benchmark)
@Fork(1)
public class StrHashAlgorithmBenchmark {

    private final List<String> partitions = new ArrayList<>();
    private final StrHashAlgorithm strHashAlgorithm = new StrHashAlgorithm();

    @Benchmark
    @Threads(1000)
    public void doSharding() {
        String columnName = "benchmark_column";
        String shardingValue = String.valueOf(ThreadLocalRandom.current().nextLong(0, 1000000));
        String logicTable = "benchmark_table";
        PreciseShardingValue<String> preciseShardingValue =
                new PreciseShardingValue<>(logicTable, columnName, shardingValue);
        strHashAlgorithm.doSharding(partitions, preciseShardingValue);
    }

    @Setup
    public void init() {
        for (int i = 0; i < 32; i ++) {
            String partition = StringUtils.leftPad(String.valueOf(i), 6, '0');
            partitions.add(partition);
        }
    }

    public static void main(final String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StrHashAlgorithmBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

}
