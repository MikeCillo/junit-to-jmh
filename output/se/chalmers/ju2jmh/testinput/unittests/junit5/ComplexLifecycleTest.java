package se.chalmers.ju2jmh.testinput.unittests.junit5;

import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;

public class ComplexLifecycleTest {

    private static List<String> sharedResource;

    private List<String> transactionLog;

    @BeforeAll
    public static void globalSetup() {
        // Setup Eseguito una volta per l'intero benchmark
        sharedResource = new ArrayList<>();
        sharedResource.add("DB_CONFIG");
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
    }

    @AfterAll
    public static void globalTeardown() {
        sharedResource = null;
    }

    @BeforeEach
    public void methodSetup() {
        // Setup Eseguito prima di ogni invocazione del benchmark
        transactionLog = new ArrayList<>();
        transactionLog.add("START_TX");
    }

    @AfterEach
    public void methodTeardown() {
        transactionLog.clear();
    }

    @Test
    public void testInteraction() {
        // Usa sia la risorsa statica che quella di istanza
        if (!sharedResource.contains("DB_CONFIG")) {
            throw new RuntimeException("Global setup failed");
        }
        transactionLog.add("COMMIT");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testInteraction() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testInteraction, this.description("testInteraction"));
        }

        @java.lang.Override
        public void beforeClass() throws java.lang.Throwable {
            super.beforeClass();
            ComplexLifecycleTest.globalSetup();
        }

        @java.lang.Override
        public void afterClass() throws java.lang.Throwable {
            ComplexLifecycleTest.globalTeardown();
            super.afterClass();
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().methodSetup();
        }

        @java.lang.Override
        public void after() throws java.lang.Throwable {
            this.implementation().methodTeardown();
            super.after();
        }

        private ComplexLifecycleTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ComplexLifecycleTest();
        }

        @java.lang.Override
        public ComplexLifecycleTest implementation() {
            return this.implementation;
        }
    }
}
