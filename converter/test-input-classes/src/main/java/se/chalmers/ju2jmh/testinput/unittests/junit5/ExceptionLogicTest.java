package se.chalmers.ju2jmh.testinput.unittests.junit5;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExceptionLogicTest {

    //exception logic test, should be converted to a benchmark that expects an exception to be thrown
    @Test
    public void testExpectedException() {
        Assertions.assertThrows(NumberFormatException.class, () -> {
           int result = Integer.parseInt("not a number");
           Assertions.fail("Expected NumberFormatException was not thrown, got result: " + result);
        });
    }

    @Test
    public void testNoException() {
        Assertions.assertEquals(12345, Integer.parseInt("12345"), "Expected parsing to succeed without exceptions");
    }
}