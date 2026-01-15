package regression;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;

public class ReferenceTest {

    private int value;

    @Before
    public void setup() {
        this.value = 10;
    }

    @Test
    public void testAddition() {
        Assert.assertEquals(15, value + 5);
    }

    @Test
    public void testMultiplication() {
        Assert.assertEquals(20, value * 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testAddition() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testAddition, this.description("testAddition"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testMultiplication() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testMultiplication, this.description("testMultiplication"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().setup();
        }

        private ReferenceTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ReferenceTest();
        }

        @java.lang.Override
        public ReferenceTest implementation() {
            return this.implementation;
        }
    }
}
