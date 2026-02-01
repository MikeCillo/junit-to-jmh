package se.chalmers.ju2jmh.testinput.unittests.junit5;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StaticLifecycleTest {

    @BeforeAll
    public static void globalSetup() {
        // Questo deve diventare @Setup(Level.Trial)
        // È eseguito una volta sola per tutto il benchmark
    }

    @AfterAll
    public static void globalTeardown() {
        // Questo deve diventare @TearDown(Level.Trial)
    }

    @Test
    public void benchmarkWorkload() {
        // Il carico di lavoro
        int a = 1 + 1;
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_benchmarkWorkload() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::benchmarkWorkload, this.description("benchmarkWorkload"));
        }

        @java.lang.Override
        public void beforeClass() throws java.lang.Throwable {
            super.beforeClass();
            StaticLifecycleTest.globalSetup();
        }

        @java.lang.Override
        public void afterClass() throws java.lang.Throwable {
            StaticLifecycleTest.globalTeardown();
            super.afterClass();
        }

        private StaticLifecycleTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new StaticLifecycleTest();
        }

        @java.lang.Override
        public StaticLifecycleTest implementation() {
            return this.implementation;
        }
    }
}
