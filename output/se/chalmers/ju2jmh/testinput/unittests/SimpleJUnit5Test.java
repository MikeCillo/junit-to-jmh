package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class SimpleJUnit5Test {

    @BeforeEach
    public void setup() {
        System.out.println(">>> SETUP ESEGUITO <<<");
    }

    @Test
    public void testJUnit5() {
        System.out.println("Test JUnit 5");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testJUnit5() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testJUnit5, this.description("testJUnit5"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setup();
        }

        private SimpleJUnit5Test implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SimpleJUnit5Test();
        }

        @java.lang.Override
        public SimpleJUnit5Test implementation() {
            return this.implementation;
        }
    }
}
