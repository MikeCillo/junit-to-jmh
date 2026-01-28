package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class AmberGranularityTest {

    // 1. Feature Java 17 (Record)
    public record Person(String name, int age) {
    }

    // 2. Metodo che VOGLIAMO convertire
    @Test
    public void testAmberFeature() {
        Person p = new Person("Michele", 25);
        System.out.println(p);
    }

    // 3. Metodo che VOGLIAMO IGNORARE
    @Test
    public void testToSkip() {
        System.out.println("Non mi devi vedere nel benchmark!");
    }
}
