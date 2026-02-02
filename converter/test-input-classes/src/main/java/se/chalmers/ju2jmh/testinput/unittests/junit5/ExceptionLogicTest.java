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
}