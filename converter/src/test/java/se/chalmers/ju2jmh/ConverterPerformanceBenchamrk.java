package se.chalmers.ju2jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.List;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class ConverterPerformanceBenchamrk {

    private InputClassRepository repository;
    private WrapperBenchmarkFactory benchmarkFactory;
    private String targetClassName;
    private List<Path> sourcePaths;
    private List<Path> classPaths;

    @Setup(Level.Trial)
    public void setUp() throws Exception {
        sourcePaths= List.of(Path.of("converter/test-input-classes/src/main/java"));
        classPaths = List.of(Path.of("converter/test-input-classes/build/classes/java/main"));
        targetClassName= "se.chalmers.ju2jmh.testinput.unittests.SimpleUnitTest";

        repository = new InputClassRepository(sourcePaths, classPaths);
        repository.findClass(targetClassName);

        benchmarkFactory = new WrapperBenchmarkFactory(repository);
    }

    @Benchmark
    public void testAstGeneration(Blackhole blackhole) throws Exception {
        var benchmark = benchmarkFactory.createBenchmarkFromTest(targetClassName);
        blackhole.consume(benchmark);
    }

    @Benchmark
    public void testFullConversion(Blackhole blackhole) throws Exception {
        InputClassRepository repository = new InputClassRepository(sourcePaths, classPaths);
        WrapperBenchmarkFactory benchmarkFactory = new WrapperBenchmarkFactory(repository);
        var benchmark = benchmarkFactory.createBenchmarkFromTest(targetClassName);
        blackhole.consume(benchmark);
    }
}
