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
}
