# junit-to-jmh

A powerful tool for automatically generating [JMH (Java Microbenchmark Harness)](https://github.com/openjdk/jmh) benchmarks from existing **JUnit 4** and **JUnit 5** unit tests.

This tool aims to bridge the gap between functional testing and performance testing, allowing developers to easily convert complex unit test suites into robust microbenchmarks with minimal manual effort.

A number of utility scripts as well as scripts and source code used for conducting experiments on the tool can be found in the [junit-to-jmh-experiment](https://github.com/alniniclas/junit-to-jmh-experiment) repo.

## 🔑 Key Features

* **JUnit 4 & JUnit 5 Support:** Seamlessly converts tests written in both major JUnit versions.
* **Granular Conversion:** Convert entire test classes or selectively target specific test methods.
* **Execution Strategies:** Choose between *AST Inlining* (extracting and repacking code) or *Wrapper Delegation* (using the native JUnit Platform Launcher).
* **Smart Conflict Resolution:** Interactive CLI prompt to `overwrite`, `merge`, or `skip` existing files, with automatic `.bak` backups to prevent data loss.
* **Fail-Fast Validation:** Validates requested methods against the source code's Abstract Syntax Tree (AST) and aborts execution if typos or missing methods are detected.
* **Batch Processing:** Load lists of test classes from external text files for CI/CD automation.

## 🛠 Prerequisites

* Java 17 or higher.
* A project using Gradle (or Maven) with compiled source classes and test classes.

## 📖 Basic Usage

To run the junit-to-jmh tool, execute the following command in the `junit-to-jmh` project directory:

```bash
$ ./gradlew :converter:run --args="-h"
```

When generating JMH benchmarks, the tool requires three mandatory path arguments, followed by the test classes to convert and any optional flags:

The root directory of the source files for the unit tests (.java).

The root directory of the compiled class files for the tests (.class).

The root directory where the resulting benchmark source files should be generated.

A list of fully-qualified test class names to generate benchmarks from.

```bash
Example (Single Class):
$ ./gradlew :converter:run --args="/path/to/src/test/java /path/to/build/classes /path/to/jmh/java com.example.MyTest"
```

⚙️ Advanced Features & Flags
1. Targeting Specific Methods

Instead of converting an entire class, you can specify exactly which methods to convert using the # separator followed by a comma-separated list of method names.
The tool uses a Fail-Fast validation: if a method does not exist in the AST, the process aborts immediately.

```bash
Converts only 'testA' and 'testB' from MyTest
$ ./gradlew :converter:run --args="... com.example.MyTest#testA,testB"
```
Alternatively, you can use the -m or --methods flag for a global filter.

2. Execution Strategies (Inlining vs. Wrapper)

By default, the tool parses the AST and generates Nested benchmarks by inlining the setup and test logic. For highly complex tests (e.g., tests with complex lifecycles, nested rules, or third-party runners), you can use the Wrapper strategy:

* --ju-runner-benchmark: Generates a benchmark that acts as a wrapper, delegating the actual execution to the underlying JUnit Platform Launcher. 

```bash
$ ./gradlew :converter:run --args="... com.example.ComplexTest --ju-runner-benchmark"
```

3. Conflict Resolution & Backups

If the tool attempts to generate a file that already exists in the output path, it will trigger a conflict resolution policy.

* --on-conflict <policy>: Defines the behavior.

  * ask (default): Pauses execution and prompts the user in the terminal: Choose: [o]verwrite, [m]erge, [s]kip.

  * overwrite: Automatically overwrites the existing file. Ideal for CI/CD pipelines.

  * merge: Intelligently merges new benchmark methods into the existing file without deleting manually added code.

Automatic Backups: Whenever a file is overwritten or merged, the tool automatically creates a .bak copy of the original file in the same directory, ensuring zero data loss.

4. Batch Conversion via File

When working with a large number of tests, it is easier to provide a plaintext file containing the fully-qualified names of the test classes (one per line).

```bash
$ ./gradlew :converter:run --args="... --class-names-file=/path/to/test-classes.txt"
```

End-to-End Example
Assuming you have a Gradle project at /tmp/my-project, here is how you might set up variables and run a targeted, safe conversion:

```bash
# 1. Define Paths
SRC="/tmp/my-project/src/test/java"
BIN="/tmp/my-project/build/classes/java/test"
OUT="/tmp/my-project/src/jmh/java"

# 2. Run the tool to convert specific methods using the Wrapper approach, 
# auto-merging if the file exists.
./gradlew :converter:run --args="$SRC $BIN $OUT \
com.example.integration.DatabaseTest#testConnection,testQuery \
--ju-runner-benchmark \
--on-conflict merge"
```

Once generated, you can build and run your JMH benchmarks using the standard JMH Gradle/Maven plugins:

```bash
$cd /tmp/my-project$ ./gradlew jmhJar
$ java -jar build/libs/my-project-jmh.jar -wi 3 -i 5 -f 1
```