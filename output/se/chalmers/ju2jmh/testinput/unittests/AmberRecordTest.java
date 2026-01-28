package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.jupiter.api.Test;

// --- FEATURE JAVA 17 (AMBER) ---
// I 'record' sono stati introdotti ufficialmente in Java 16.
// Se il parser è vecchio, qui darà errore di sintassi.
record Utente(String nome, int id) {
}

public class AmberRecordTest {

    @Test
    public void testAmber() {
        Utente u = new Utente("Mario", 1);
        System.out.println("Utente: " + u.nome());
    }
}
