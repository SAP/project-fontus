package com.sap.fontus.generator;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.sap.fontus.config.*;
import picocli.CommandLine;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        description = "Generates a source and sink configuration for the passed classes",
        name = "source_sink_generator",
        mixinStandardHelpOptions = true,
        version = "source_sink_generator 0.0.1"
)
public class Generator implements Callable<Void> {
    @CommandLine.Option(
            names = {"-so", "--source"},
            required = false,
            split = ",",
            paramLabel = "Source",
            description = "The classes specified as sources"
    )
    private String[] sourceClasses;

    @CommandLine.Option(
            names = {"-so-obj", "--source-objects"},
            required = false,
            paramLabel = "Source Objects",
            description = "Specifies if java/lang/Object as return type should also be seen as source"
    )
    private boolean sourceObjects;

    @CommandLine.Option(
            names = {"-si", "--sink"},
            required = false,
            split = ",",
            paramLabel = "Sink",
            description = "The classes specified as sink"
    )
    private String[] sinkClasses;

    @CommandLine.Option(
            names = {"-si-obj", "--sink-objects"},
            required = false,
            paramLabel = "Sink Objects",
            description = "Specifies if java/lang/Object as parameter type should also be seen as sink"
    )
    private boolean sinkObjects;

    @Override
    public Void call() throws IOException {
        Configuration configuration = new Configuration();

        List<Source> sources = new ArrayList<>();
        if (this.sourceClasses != null) {
            for (String source : this.sourceClasses) {
                SourceGenerator sourceGenerator = new SourceGenerator(source);
                sources.addAll(sourceGenerator.readSources(this.sourceObjects));
            }
        }
        configuration.getSourceConfig().append(new SourceConfig(sources));

        List<Sink> sinks = new ArrayList<>();
        if (this.sinkClasses != null) {
            for (String sink : this.sinkClasses) {
                String className = sink;
                List<String> categories = new ArrayList<>();
                if (sink.contains("=")) {
                    className = sink.substring(0, sink.indexOf("="));
                    String[] categoriesArray = sink.substring(sink.indexOf("=") + 1).split("=");
                    categories = Arrays.asList(categoriesArray);
                }
                // For now don't support generating sinks with purposes and vendors
                SinkGenerator sinkGenerator = new SinkGenerator(className, categories, new ArrayList<>(), new ArrayList<>());
                sinks.addAll(sinkGenerator.readSinks(this.sinkObjects));
            }
        }
        configuration.getSinkConfig().append(new SinkConfig(sinks));

        ObjectMapper objectMapper = new XmlMapper(new WstxInputFactory(), new WstxOutputFactory());
        objectMapper.registerModule(new JaxbAnnotationModule());
        ObjectWriter objectWriter = objectMapper.writerFor(Configuration.class);
        StringWriter stringWriter = new StringWriter();
        objectWriter.writeValue(stringWriter, configuration);
        System.out.println(stringWriter.toString());
        return null;
    }

    public static void main(String[] args) {
        new CommandLine(new Generator())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }

}
