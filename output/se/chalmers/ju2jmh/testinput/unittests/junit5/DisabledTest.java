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
}
