package se.chalmers.ju2jmh.testinput.unittests.junit5;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DisabledTest {

    @Test
    public void activeTest() {
        // Questo DOVREBBE essere convertito
    }

    @Test
    @Disabled("Questo test non deve diventare un benchmark")
    public void skippedTest() {
        // Questo NON deve apparire nell'output o deve essere commentato
        throw new RuntimeException("Non dovrei essere eseguito");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_activeTest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::activeTest, this.description("activeTest"));
        }

        private DisabledTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DisabledTest();
        }

        @java.lang.Override
        public DisabledTest implementation() {
            return this.implementation;
        }
    }
}
