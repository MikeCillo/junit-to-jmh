package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

/**
 * Validation for JUnit 5 lifecycle annotations and granular conversion.
 */
public class SimpleJUnit5Test {

    private boolean isSetup = false;

    @BeforeEach
    public void setup() {
        this.isSetup = true;
    }

    @Test
    public void testJUnit5() {
        if (!isSetup) {
            throw new IllegalStateException("Setup method was not executed");
        }
    }
}
