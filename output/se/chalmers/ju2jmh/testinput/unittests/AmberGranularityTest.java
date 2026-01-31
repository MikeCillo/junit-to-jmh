package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class AmberGranularityTest {

    // 1. Feature Java 17 (Record)
    public record Person(String name, int age) {
    }

    // 2. Metodo che VOGLIAMO convertire
    @Test
    public void testAmberFeature() {
        Person p = new Person("Benito", 24);
        System.out.println(p);
    }

    // 3. Metodo che VOGLIAMO IGNORARE
    @Test
    public void testToSkip() {
        System.out.println("Test to be skipped");
    }
}
