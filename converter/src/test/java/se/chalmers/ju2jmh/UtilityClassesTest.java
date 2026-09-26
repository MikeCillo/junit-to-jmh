package se.chalmers.ju2jmh;


import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class UtilityClassesTest {

    @Test
    public void testClassNamesEnclosingAndOutermost() {
        // Test Boundary: nested class (Equivalence Class: Nested)
        assertEquals("com.example.Outer", ClassNames.enclosingClassName("com.example.Outer$Inner").orElse(null));
        assertEquals("com.example.Outer", ClassNames.outermostClassName("com.example.Outer$Inner$Deep"));

        // Test Boundary: non-nested class (Equivalence Class: Non-Nested)
        assertFalse(ClassNames.enclosingClassName("com.example.Outer").isPresent());
        assertEquals("com.example.Outer", ClassNames.outermostClassName("com.example.Outer"));

        // real class with nested class
        assertEquals("java.util.Map", ClassNames.enclosingClassName(Map.Entry.class).orElse(null));
        assertFalse(ClassNames.enclosingClassName(String.class).isPresent());
    }

    @Test
    public void testClassNamesShortAndSimple() {
        // package + nested class
        assertEquals("Outer$Inner", ClassNames.shortClassName("com.example.Outer$Inner"));
        assertEquals("Inner", ClassNames.simpleClassName("com.example.Outer$Inner"));

        // Boundary case: class without package
        assertEquals("NoPackageClass", ClassNames.shortClassName("NoPackageClass"));
        assertEquals("NoPackageClass", ClassNames.simpleClassName("NoPackageClass"));
    }

    @Test
    public void testBytecodeDescriptorValidation() {
        // Test positive case: Valid descriptor
        assertEquals("java.lang.String", Bytecode.referenceFieldTypeDescriptorToClassName("Ljava/lang/String;"));

        // Boundary Value / Error Guessing: error cases with invalid descriptors
        IllegalArgumentException thrown1 = assertThrows(IllegalArgumentException.class, () -> {
            Bytecode.referenceFieldTypeDescriptorToClassName("java/lang/String;"); //  L missing
        });
        assertTrue(thrown1.getMessage().contains("Invalid format"));

        IllegalArgumentException thrown2 = assertThrows(IllegalArgumentException.class, () -> {
            Bytecode.referenceFieldTypeDescriptorToClassName("Ljava/lang/String"); // ; missing
        });
        assertTrue(thrown2.getMessage().contains("Invalid format"));
    }
}