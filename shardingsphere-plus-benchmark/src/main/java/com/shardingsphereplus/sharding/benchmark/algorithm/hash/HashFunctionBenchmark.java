package com.shardingsphereplus.sharding.benchmark.algorithm.hash;

import com.google.common.hash.Hashing;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.charset.StandardCharsets;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@State(Scope.Benchmark)
@Fork(1)
@Threads(20)
public class HashFunctionBenchmark {

    private String str;

    @Benchmark
    public int testHashCode() {
        return str.hashCode();
    }

    @Benchmark
    public void testMurmurHash3() {
        Hashing.murmur3_128(0x1234ABCD).newHasher()
                .putString(str, StandardCharsets.UTF_8)
                .hash().asInt();
    }

    @Setup
    public void init() {
        str = "S00001";
    }

    public static void main(final String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HashFunctionBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

}
