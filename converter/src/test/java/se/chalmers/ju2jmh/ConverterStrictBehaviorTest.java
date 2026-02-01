package se.chalmers.ju2jmh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import se.chalmers.ju2jmh.testinput.unittests.SimpleUnitTest;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConverterStrictBehaviorTest {

    @Test
    public void strictFailsWhenRequestedMethodMissing(@TempDir Path tempDir) throws Exception {
        Converter converter = new Converter();
        CommandLine cmd = new CommandLine(converter);

        // Prepare directories and add fixture class
        InputClassDirectory dir = new InputClassDirectory(tempDir);
        dir.add(SimpleUnitTest.class);

        String src = dir.sourcesDirectory().toString();
        String classes = dir.bytecodeDirectory().toString();
        Path outPath = tempDir.resolve("out");
        Files.createDirectories(outPath);
        String out = outPath.toString();

        String className = SimpleUnitTest.class.getName() + "#nonExistingMethod";

        String[] args = new String[]{"--strict", src, classes, out, className};

        // Parse args into converter fields
        cmd.parseArgs(args);

        // Execute should throw because strict is enabled and requested method doesn't exist
        assertThrows(InvalidInputClassException.class, () -> converter.call());
    }
}
