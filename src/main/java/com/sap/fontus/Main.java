package com.sap.fontus;

import com.sap.fontus.agent.InstrumentationConfiguration;
import com.sap.fontus.config.ConfigurationLoader;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.IOUtils;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.offline.OfflineClassInstrumenter;
import com.sap.fontus.utils.offline.OfflineJarInstrumenter;
import picocli.CommandLine;

import java.io.*;
import java.util.concurrent.Callable;

@CommandLine.Command(
        description = "Replaces all String instances with taint-aware Strings.",
        name = "asm_taint",
        mixinStandardHelpOptions = true,
        version = "asm_taint 0.0.1"
)
public final class Main implements Callable<Void> {
    private static final Logger logger = LogUtils.getLogger();

    @CommandLine.Option(
            names = {"-f", "--file"},
            required = true,
            paramLabel = "Input",
            description = "The input class/jar file or directory"
    )
    private File inputFile;

    @CommandLine.Option(
            names = {"-o", "--out"},
            required = true,
            paramLabel = "Output",
            description = "The output class/jar file or directory"
    )
    private File outputFile;

    @CommandLine.Option(
            names = {"--instrumented-classes"},
            required = true,
            paramLabel = "Instrumented Classes",
            description = "Output file which contains a list of all instrumented classes"
    )
    private File instrumentedClasses;

    @CommandLine.Option(
            names = {"-c", "--config"},
            required = false,
            paramLabel = "Config",
            description = "Configuration file"
    )
    private File configFile;

    @CommandLine.Option(
            names = {"-t", "--taintmethod"},
            required = false,
            paramLabel = "Taint method",
            description = "Taint method, which should be used. Valid values:  ${COMPLETION-CANDIDATES}",
            defaultValue = TaintMethod.defaultTaintMethodName
    )
    private TaintMethod taintMethod;

    @CommandLine.Option(
            names = {"-l", "--logging"},
            paramLabel = "Logging",
            description = "Turns on logging"
    )
    private boolean logging;

    @CommandLine.Option(
            names = {"-h", "--hybrid"},
            paramLabel = "Hybrid Tainting",
            description = "Flag for activating hybrid tainting"
    )
    private boolean isHybrid;

    @CommandLine.Option(
            names = {"-p", "--parallel"},
            paramLabel = "Parallel instrumentation",
            description = "Parallel instrumentation of classes (can speedup instrumentation)"
    )
    private boolean isParallel;

    private Configuration configuration;

    private OfflineJarInstrumenter offlineJarInstrumenter;
    private OfflineClassInstrumenter offlineClassInstrumenter;

    private Main() {
    }

    private void instrumentDirectory(File input, File output) throws IOException {
        // Create the output directory
        if (!output.isDirectory()) {
            if (!output.mkdirs()) {
                logger.error("Error Creating output directory!");
                return;
            }
        }
        for (File f : input.listFiles()) {
            File o = new File(output.getPath() + File.separator + f.getName());
            // Recurse
            this.walkFileTree(f, o);
        }
    }

    private void walkFileTree(File input, File output) throws IOException {
        if (input.isDirectory()) {
            // Some directories end with .jar, so check for directory first
            this.instrumentDirectory(input, output);
        } else if (input.length() == 0L) {
            // There sometimes are .jar files that are empty, handle those..
            logger.info("File of size 0: {}", input.getName());
        } else if (input.getName().endsWith(Constants.CLASS_FILE_SUFFIX)) {
            this.offlineClassInstrumenter.instrumentClassFile(input, output);
        } else if (input.getName().endsWith(Constants.JAR_FILE_SUFFIX)) {
            this.offlineJarInstrumenter.instrumentJarFile(input, output);
        } else {
            logger.error("Input file name must have class or jar extension!");
        }
    }

    private void loadConfiguration() {
        this.configuration = ConfigurationLoader.loadAndMergeConfiguration(this.configFile, this.taintMethod);
        this.configuration.setHybridMode(this.isHybrid);
        this.configuration.setLoggingEnabled(this.logging);
        this.configuration.setParallel(this.isParallel);
        Configuration.setConfiguration(configuration);
    }

    @Override
    public Void call() throws IOException {
        this.loadConfiguration();
        InstrumentationConfiguration.init(this.inputFile, this.outputFile);
        this.offlineJarInstrumenter = new OfflineJarInstrumenter(this.configuration);
        this.offlineClassInstrumenter = new OfflineClassInstrumenter(this.configuration);

        this.walkFileTree(this.inputFile, this.outputFile);

        IOUtils.writeToFile(this.offlineJarInstrumenter.getClasses(), this.instrumentedClasses);

        return null;
    }

    public static void main(String[] args) {
        new CommandLine(new Main())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }

}
