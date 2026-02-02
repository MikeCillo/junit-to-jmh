package se.chalmers.ju2jmh.testinput.unittests.junit5;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExceptionLogicTest {

    @Test
    public void testExpectedException() {
        // Il convertitore deve mantenere questa struttura lambda
        Assertions.assertThrows(NumberFormatException.class, () -> {
            Integer.parseInt("Non è un numero");
        });
    }

    @Test
    public void testNoException() {
        // Caso di controllo
        Integer.parseInt("12345");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testExpectedException() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testExpectedException, this.description("testExpectedException"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testNoException() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testNoException, this.description("testNoException"));
        }

        private ExceptionLogicTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ExceptionLogicTest();
        }

        @java.lang.Override
        public ExceptionLogicTest implementation() {
            return this.implementation;
        }
    }
}
