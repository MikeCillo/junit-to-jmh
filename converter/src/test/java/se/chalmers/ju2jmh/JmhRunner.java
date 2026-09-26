package se.chalmers.ju2jmh;


import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;


public class JmhRunner {
    public static void main(String[] args) throws Exception {
        Options options= new OptionsBuilder().include(ConverterPerformanceBenchamrk.class.getSimpleName()).build();
        new Runner(options).run();
    }
}
