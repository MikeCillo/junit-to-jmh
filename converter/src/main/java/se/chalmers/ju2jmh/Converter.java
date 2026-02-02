package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import picocli.CommandLine;
import se.chalmers.ju2jmh.api.ExceptionTest;
import se.chalmers.ju2jmh.api.JU2JmhBenchmark;
import se.chalmers.ju2jmh.api.Rules;
import se.chalmers.ju2jmh.api.ThrowingConsumer;
import se.chalmers.ju2jmh.model.UnitTestClass;
import com.github.javaparser.ParserConfiguration.LanguageLevel; //linguaggio
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Console;// gestione dei conflitti quando il file di output esiste:
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "ju2jmh", mixinStandardHelpOptions = true)
public class Converter implements Callable<Integer> {
    @CommandLine.Parameters(description =
            "Root path(s) of the input source files. `${sys:path.separator}` may be used as a"
                    + " separator to specify multiple directories.", index = "0")
    private String sourcePath;

    @CommandLine.Parameters(description =
            "Root path(s) of the input class files. `${sys:path.separator}` may be used as a"
                    + " separator to specify multiple directories.", index = "1")
    private String classPath;

    @CommandLine.Parameters(description = "Root path for the output source files.", index = "2")
    private Path outputPath;

    @CommandLine.Parameters(
            description = "Fully qualified names of the classes to convert to benchmarks.",
            index = "3..*")
    private List<String> classNames;

    @CommandLine.Option(
            names = {"--ju4-runner-benchmark"},
            description = "Generate benchmarks delegating their execution to the JUnit 4 JUnitCore "
                    + "runner.")
    private boolean juRunnerBenchmark;

    @CommandLine.Option(
            names = {"--tailored-benchmark"},
            description = "Generate benchmarks customised to and optimised based on the specific "
                    + "JUnit features used by the individual tests.")
    private boolean tailoredBenchmark;

    @CommandLine.Option(
            names = {"-i", "--ignore-failures"},
            description = "Generate the remaining benchmark classes even if conversion of some "
                    + "input classes fails.")
    private boolean ignoreFailures;

    @CommandLine.Option(
            names = {"--class-names-file"},
            description = "File to load class names from.")
    private Path classNamesFile;

    @CommandLine.Option(
            names = {"-m", "--methods"},
            description = "Specific methods to convert (comma separated). If omitted, all tests are converted.",
            split = ","
    )

    private List<String> targetMethods;

    @CommandLine.Option(
            names = {"--strict"},
            description = "Fail if a requested specific method is not found/converted."
    )
    private boolean strict = false;

    @CommandLine.Option(
            names = {"--on-conflict"},
            description = "Policy when an output file already exists: ask|overwrite|merge. Default: ask"
    )
    private String onConflict = "ask";

    @CommandLine.Option(names = {"-q", "--quiet"}, description = "Suppress output logs like [CREATE], [OVERWRITE], etc.")
    private boolean quiet = false;

    private static CompilationUnit loadApiSource(Class<?> apiClass) throws IOException {
        return StaticJavaParser.parseResource(
                apiClass.getCanonicalName().replace('.', '/') + ".java");
    }


    private void writeSourceCodeToFile(CompilationUnit benchmark, File outputFile) throws IOException {
        outputFile.getParentFile().mkdirs();

        // 1. Se il file viene creato
        if (!outputFile.exists()) {
            if (!quiet) System.out.println("[CREATE] " + outputFile.getPath());
            try (OutputStreamWriter out = new OutputStreamWriter(
                    new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
                out.append(benchmark.toString());
            }
            return;
        }

        //   Se il contenuto è identico SALTA
        try {
            String existingContent = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
            //  eliminazione spazi dalle stringhe
            if (existingContent.trim().equals(benchmark.toString().trim())) {
                return;
            }
        } catch (IOException e) {
        }

        // 3. Gestione Conflitti (Se il contenuto è DIVERSO)
        String policy = (onConflict == null) ? "ask" : onConflict.toLowerCase();
        boolean doOverwrite = false;
        boolean doMerge = false;

        if (policy.equals("overwrite")) doOverwrite = true;
        else if (policy.equals("merge")) doMerge = true;
        else { // ask
            Console console = System.console();
            if (console != null) {
                String prompt = String.format("File %s has CHANGED. Choose: [o]verwrite, [m]erge (keep previous benchmarks), [s]kip: ", outputFile.getName());
                String line = console.readLine(prompt);
                if (line != null && line.trim().toLowerCase().startsWith("m")) doMerge = true;
                else if (line != null && line.trim().toLowerCase().startsWith("o")) doOverwrite = true;
                else return;
            } else {
                doOverwrite = true;
            }
        }

        if (doOverwrite) {
            if (!quiet) System.out.println("[OVERWRITE] " + outputFile.getPath());
            createBackup(outputFile);
            try (OutputStreamWriter out = new OutputStreamWriter(
                    new FileOutputStream(outputFile, false), StandardCharsets.UTF_8)) {
                out.append(benchmark.toString());
            }
            return;
        }

        if (doMerge) {
            try {
                CompilationUnit existingCU = StaticJavaParser.parse(outputFile);

                // Merge IMPORTS
                existingCU.getImports().forEach(im -> {
                    if (!benchmark.getImports().contains(im)) benchmark.addImport(im);
                });

                TypeDeclaration<?> newType = benchmark.getTypes().get(0);
                TypeDeclaration<?> existingType = existingCU.getTypes().stream()
                        .filter(t -> t.getNameAsString().equals(newType.getNameAsString()))
                        .findFirst().orElse(null);

                if (existingType != null && existingType.isClassOrInterfaceDeclaration() && newType.isClassOrInterfaceDeclaration()) {
                    ClassOrInterfaceDeclaration existingCid = existingType.asClassOrInterfaceDeclaration();
                    ClassOrInterfaceDeclaration newCid = newType.asClassOrInterfaceDeclaration();

                    // Cerca metodi benchmark nella classe principale
                    mergeMethods(existingCid, newCid);

                    // Cerca metodi benchmark nella classe interna "_Benchmark"
                    existingCid.getMembers().stream()
                            .filter(member -> member.isClassOrInterfaceDeclaration())
                            .map(member -> member.asClassOrInterfaceDeclaration())
                            .filter(inner -> inner.getNameAsString().equals("_Benchmark"))
                            .findFirst()
                            .ifPresent(existingInner -> {
                                newCid.getMembers().stream()
                                        .filter(m -> m.isClassOrInterfaceDeclaration())
                                        .map(m -> m.asClassOrInterfaceDeclaration())
                                        .filter(newInner -> newInner.getNameAsString().equals("_Benchmark"))
                                        .findFirst()
                                        .ifPresent(newInner -> {
                                            mergeMethods(existingInner, newInner);
                                        });
                            });

                    if (!quiet) System.out.println("[MERGE] " + outputFile.getPath());
                    createBackup(outputFile);

                    try (OutputStreamWriter out = new OutputStreamWriter(
                            new FileOutputStream(outputFile, false), StandardCharsets.UTF_8)) {
                        out.append(benchmark.toString());
                    }
                }
            } catch (Exception e) {
                System.err.println("[WARN] Merge failed for " + outputFile.getName() + ": " + e.getMessage() + ". Overwriting.");
                try (OutputStreamWriter out = new OutputStreamWriter(
                        new FileOutputStream(outputFile, false), StandardCharsets.UTF_8)) {
                    out.append(benchmark.toString());
                }
            }
        }
    }

    // Metodo Helper per evitare duplicazione codice
    private void mergeMethods(ClassOrInterfaceDeclaration source, ClassOrInterfaceDeclaration dest) {
        List<MethodDeclaration> oldBenchmarks = source.getMethods().stream()
                .filter(m -> m.getNameAsString().startsWith("benchmark_"))
                .collect(Collectors.toList());

        int count = 0;
        for (MethodDeclaration oldMethod : oldBenchmarks) {
            boolean present = dest.getMethods().stream()
                    .anyMatch(nm -> nm.getNameAsString().equals(oldMethod.getNameAsString()));
            if (!present) {
                dest.addMember(oldMethod.clone());
                count++;
            }
        }
        if (count > 0) {
            System.out.println("   -> Recovered " + count + " methods from " + source.getNameAsString());
        }
    }

    // helper per normalizzare una ImportDeclaration e confrontarla senza duplicati
    private String importKey(com.github.javaparser.ast.ImportDeclaration im) {
        StringBuilder sb = new StringBuilder();
        sb.append(im.getNameAsString());
        if (im.isStatic()) sb.append(" static");
        if (im.isAsterisk()) sb.append(".*");
        return sb.toString();
    }

    private void generateNestedBenchmarks() throws ClassNotFoundException, IOException, InvalidInputClassException {
        InputClassRepository repository = new InputClassRepository(toPaths(sourcePath), toPaths(classPath));

        // Costruiamo un builder singolo e aggiungiamo tutte le classi/metodi ad esso
        NestedBenchmarkSuiteBuilder benchmarkSuiteBuilder =
                new NestedBenchmarkSuiteBuilder(toPaths(sourcePath), toPaths(classPath));

        // Manteniamo una mappa delle classi per cui l'utente ha richiesto metodi espliciti
        Map<String, List<String>> explicitlyRequestedMethods = new HashMap<>();

        for (String inputArg : classNames) {
            String className = inputArg;
            List<String> methodsForThisClass = new ArrayList<>();

            // Gestione del #
            if (inputArg.contains("#")) {
                String[] parts = inputArg.split("#", 2);
                className = parts[0];
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    methodsForThisClass.addAll(Arrays.asList(parts[1].split(",")));
                    explicitlyRequestedMethods.put(className, methodsForThisClass);
                }
            } else {
                // -m
                if (targetMethods != null && !targetMethods.isEmpty()) {
                    methodsForThisClass.addAll(targetMethods);
                }
            }

            // Stampa non siamo in modalità quiet
            if (!quiet) {
                if (methodsForThisClass.isEmpty()) {
                    System.out.println("Convertendo TUTTA la classe: " + className);
                } else {
                    System.out.println("Convertendo PARZIALMENTE " + className + " metodi: " + methodsForThisClass);
                }
            }

            // Aggiungi la classe al builder con la lista specificata
            if (methodsForThisClass.isEmpty()) {
                benchmarkSuiteBuilder.addTestClass(className);
            } else {
                benchmarkSuiteBuilder.addTestClass(className, methodsForThisClass);
            }
        }

        Map<String, CompilationUnit> suite = benchmarkSuiteBuilder.buildSuite();

        // Se c'è --strict  verifichiamo che per le classi dove l'utente ha chiesto
        // metodi espliciti siano effettivamente stati generati dei benchmark.
        if (strict && !explicitlyRequestedMethods.isEmpty()) {
            for (Map.Entry<String, List<String>> e : explicitlyRequestedMethods.entrySet()) {
                String className = e.getKey();
                boolean foundBenchmarksForClass = false;
                CompilationUnit generated = suite.get(className);
                if (generated != null) {
                    // Cerchiamo inner classes che abbiano metodi con prefisso 'benchmark_'
                    for (TypeDeclaration<?> type : generated.getTypes()) {
                        // Scorriamo i membri della compilation unit e le classi per trovare metodi benchmark
                        List<MethodDeclaration> methods = type.findAll(MethodDeclaration.class);
                        for (MethodDeclaration md : methods) {
                            if (md.getNameAsString().startsWith("benchmark_")) {
                                foundBenchmarksForClass = true;
                                break;
                            }
                        }
                        if (foundBenchmarksForClass) {
                            break;
                        }
                    }
                }
                if (!foundBenchmarksForClass) {
                    String msg = "Requested methods " + e.getValue() + " not found for class " + className;
                    if (!ignoreFailures) {
                        throw new InvalidInputClassException(msg);
                    } else {
                        System.err.println("Warning: " + msg);
                    }
                }
            }
        }

        for (String generatedClassName : suite.keySet()) {
            File outputFile = outputPath.resolve(
                    generatedClassName.replace('.', File.separatorChar) + ".java").toFile();
            writeSourceCodeToFile(suite.get(generatedClassName), outputFile);
        }

        // Scrittura classe base (una volta sola)
        writeSourceCodeToFile(
                loadApiSource(JU2JmhBenchmark.class),
                outputPath.resolve(
                                JU2JmhBenchmark.class.getCanonicalName()
                                        .replace('.', File.separatorChar) + ".java")
                        .toFile());
    }


    private void generateJUBenchmarks()
            throws ClassNotFoundException, IOException, InvalidInputClassException {
        InputClassRepository repository =
                new InputClassRepository(toPaths(sourcePath), toPaths(classPath));
        WrapperBenchmarkFactory benchmarkFactory = new WrapperBenchmarkFactory(repository);
        List<CompilationUnit> benchmarks = new ArrayList<>(classNames.size());
        for (String className : classNames) {
            try {
                benchmarks.add(benchmarkFactory.createBenchmarkFromTest(className));
            } catch (BenchmarkGenerationException e) {
                if (!ignoreFailures) {
                    throw e;
                }
            }
        }
        for (CompilationUnit benchmark : benchmarks) {
            TypeDeclaration<?> benchmarkClass = benchmark.getTypes().get(0);
            String benchmarkClassName = benchmarkClass.getFullyQualifiedName().orElseThrow();
            File outputFile = outputPath.resolve(
                    benchmarkClassName.replace('.', File.separatorChar) + ".java").toFile();
            writeSourceCodeToFile(benchmark, outputFile);
        }
    }

    private static void loadMissingCompilationUnits(
            String packageName, File file, InputClassRepository repository,
            Map<String, CompilationUnit> compilationUnits) throws ClassNotFoundException {
        String name = file.getName();
        if (file.isFile()) {
            if (name.endsWith(".java")) {
                String className =
                        packageName + name.substring(0, name.length() - ".java".length());
                if (!compilationUnits.containsKey(className)) {
                    compilationUnits.put(
                            className,
                            repository.findClass(className)
                                    .getSource()
                                    .findCompilationUnit()
                                    .orElseThrow());
                }
            }
        } else if (file.isDirectory()) {
            packageName = packageName + name + ".";
            File[] containedFiles = file.listFiles();
            if (containedFiles == null) {
                return;
            }
            for (File containedFile : containedFiles) {
                loadMissingCompilationUnits(
                        packageName, containedFile, repository, compilationUnits);
            }
        }
    }

    private void generateTailoredBenchmarks() throws ClassNotFoundException, IOException {
        InputClassRepository repository =
                new InputClassRepository(toPaths(sourcePath), toPaths(classPath));
        UnitTestClassRepository testClassRepository = new UnitTestClassRepository(repository);
        Map<String, CompilationUnit> compilationUnits = new HashMap<>();
        for (String className : classNames) {
            UnitTestClass testClass = testClassRepository.findClass(className);
            Predicate<String> nameValidator =
                    TailoredBenchmarkFactory.nameValidatorForCompilationUnit(
                            repository.findClass(className)
                                    .getSource()
                                    .findCompilationUnit()
                                    .orElseThrow());
            ClassOrInterfaceDeclaration benchmarkClass =
                    TailoredBenchmarkFactory.generateBenchmarkClass(testClass, nameValidator);
            TypeDeclaration<?> testClassSource = repository.findClass(className).getSource();
            testClassSource.addMember(benchmarkClass);
            compilationUnits.put(className, testClassSource.findCompilationUnit().orElseThrow());
        }
        for (Path path : toPaths(sourcePath)) {
            File[] files = path.toFile().listFiles();
            if (files == null) {
                continue;
            }
            for (File file : files) {
                loadMissingCompilationUnits("", file, repository, compilationUnits);
            }
        }
        compilationUnits.put(
                ExceptionTest.class.getCanonicalName(), loadApiSource(ExceptionTest.class));
        compilationUnits.put(Rules.class.getCanonicalName(), loadApiSource(Rules.class));
        compilationUnits.put(
                ThrowingConsumer.class.getCanonicalName(), loadApiSource(ThrowingConsumer.class));
        for (String className : compilationUnits.keySet()) {
            CompilationUnit benchmark = compilationUnits.get(className);
            File outputFile = outputPath.resolve(
                    className.replace('.', File.separatorChar) + ".java").toFile();
            writeSourceCodeToFile(benchmark, outputFile);
        }
    }

    private static List<Path> toPaths(String pathString) {
        return Arrays.stream(pathString.split(File.pathSeparator))
                .map(Path::of)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Integer call() throws ClassNotFoundException, IOException, InvalidInputClassException {
        if (!outputPath.toFile().exists()) {
            throw new FileNotFoundException("Output directory " + outputPath + " does not exist");
        }
        if (classNamesFile != null) {
            try (Stream<String> lines = Files.lines(classNamesFile)) {
                if (classNames == null) {
                    classNames = lines.collect(Collectors.toUnmodifiableList());
                } else {
                    classNames = Stream.concat(classNames.stream(), lines).collect(Collectors.toUnmodifiableList());
                }
            }
        }
        if (!juRunnerBenchmark) {
            if (!tailoredBenchmark) {
                generateNestedBenchmarks();
            } else {
                generateTailoredBenchmarks();
            }
        } else {
            generateJUBenchmarks();
        }
        return 0;
    }


    //  HELPER PER IL BACKUP
    private void createBackup(File file) {
        try {
            Path backupPath = Path.of(file.getAbsolutePath() + ".bak");
            Files.copy(file.toPath(), backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("[WARN] Failed to create backup for " + file.getName() + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        StaticJavaParser.getConfiguration().setLanguageLevel(LanguageLevel.JAVA_17);
        int exitCode = new CommandLine(new Converter()).execute(args);
        System.exit(exitCode);
    }
}
