package se.chalmers.ju2jmh.testinput.unittests.junit5;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HeavyCalculationTest {

    @Test
    public void testSortLargeList() {
        // Genera una lista di 10.000 interi casuali
        List<Integer> numbers = new Random().ints(10_000).boxed().collect(Collectors.toList());

        // Operazione costosa (O(N log N)) che il benchmark dovrà misurare
        Collections.sort(numbers);
    }

    @Test
    public void testStreamProcessing() {
        // Calcolo pesante usando gli Stream: somma dei quadrati dei numeri pari fino a 100.000
        long sum = IntStream.range(0, 100_000)
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> n * n)
                .sum();

        // Un blackhole implicito (se il convertitore supporta il return value sarebbe meglio,
        // ma per ora vediamo se compila)
        if (sum == 0) throw new RuntimeException("Impossibile");
    }
}