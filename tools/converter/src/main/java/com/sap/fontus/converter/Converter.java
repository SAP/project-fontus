package com.sap.fontus.converter;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.*;
import com.sap.fontus.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import picocli.CommandLine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        description = "Converts a juturna config to a fontus config",
        name = "juturna converter",
        mixinStandardHelpOptions = true,
        version = "juturna converter 0.0.1"
)
public class Converter implements Callable<Void> {


    @CommandLine.Option(
            names = {"-j", "--juturna"},
            required = true,
            paramLabel = "Juturna Config",
            description = "Juturna config file to convert"
    )
    private File juturnaConfigFile;

    @CommandLine.Option(
            names = {"-f", "--fontus"},
            required = true,
            paramLabel = "Fontus Config",
            description = "Fontus config to merge with"
    )
    private File fontusConfigFile;

    @CommandLine.Option(
            names = {"-o", "--output"},
            required = true,
            paramLabel = "Output",
            description = "Output destination for the converted and merged config"
    )
    private File outputFile;

    @Override
    public Void call() {
        try {
            Configuration fontusConfig = this.readConfig();
            JSONObject juturnaObject = this.readJuturnaConfig();

            JSONArray jsonArray = (JSONArray) juturnaObject.get("sinks");
            Configuration configuration = this.mergeSinks(fontusConfig, jsonArray);
            jsonArray = (JSONArray) juturnaObject.get("sources");
            Configuration configuration1 = this.mergeSources(fontusConfig, jsonArray);
            configuration.append(configuration1);

            this.saveConfig(configuration);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Configuration mergeSources(Configuration fontusConfig, JSONArray jsonArray) {
        List<Source> sources = new ArrayList<>();
        for (Object sourceJsonO : jsonArray) {
            JSONObject sourceJson = (JSONObject) sourceJsonO;
            try {
                FunctionCall call = this.parseFunctionCall(sourceJson);
                boolean missing = true;
                for (Source source : fontusConfig.getSourceConfig().getSources()) {
                    if (call.equals(source.getFunction())) {
                        missing = false;
                        break;
                    }
                }
                if (missing) {
                    String sourceName = generateName(call);
                    sources.add(new Source(sourceName, call));
                }
            } catch (Exception e) {
                System.err.println("Some error occured with this:");
                System.err.println(sourceJson.toString());
                System.err.printf("%s: %s%n", e, e.getMessage());
            }
        }
        Configuration configuration = new Configuration();
        configuration.getSourceConfig().append(new SourceConfig(sources));
        return configuration;
    }

    private Configuration mergeSinks(Configuration fontusConfig, JSONArray jsonArray) {
        List<Sink> sinks = new ArrayList<>();
        for (Object sinkJsonO : jsonArray) {
            JSONObject sinkJson = (JSONObject) sinkJsonO;
            try {
                FunctionCall call = this.parseFunctionCall(sinkJson);
                boolean missing = true;
                for (Sink sink : fontusConfig.getSinkConfig().getSinks()) {
                    if (call.equals(sink.getFunction())) {
                        missing = false;
                        break;
                    }
                }
                if (missing) {
                    List<SinkParameter> sinkParameters = parseParameters(sinkJson);
                    List<String> categories = this.parseCategories(sinkJson);
                    List<Position> positions = parsePositions(sinkJson);
                    String sinkName = generateName(call);
                    sinks.add(new Sink(sinkName, call, sinkParameters, categories, new DataProtection(), FunctionCall.EmptyFunctionCall, positions));
                }
            } catch (Exception e) {
                System.err.println("Some error occured with this:");
                System.err.println(sinkJson.toString());
                System.err.println(e + ": " + e.getMessage());
            }
        }
        Configuration configuration = new Configuration();
        configuration.getSinkConfig().append(new SinkConfig(sinks));
        return configuration;
    }

    private Class<?> resolveClass(String name) throws ClassNotFoundException {
        switch (name) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "char":
                return char.class;
            case "void":
                return void.class;
            default:
                return Class.forName(name);
        }
    }

    private List<String> parseStringList(JSONObject sinkJson, String listName, String elementName) {
        List<String> list = new ArrayList<>();
        JSONArray array = (JSONArray) sinkJson.get(listName);
        for (Object categoryObject : array) {
            JSONObject category = (JSONObject) categoryObject;
            list.add(category.getString(elementName));
        }
        return list;
    }

    private List<String> parseCategories(JSONObject sinkJson) {
        return this.parseStringList(sinkJson, "categories", "category");
    }

    private List<String> parseVendors(JSONObject sinkJson) {
        return this.parseStringList(sinkJson, "vendors", "vendor");
    }

    private List<String> parsePurposes(JSONObject sinkJson) {
        return this.parseStringList(sinkJson, "purposes", "purpose");
    }

    private static String generateName(FunctionCall call) {
        String name = String.format("%s.", Utils.dotToSlash(call.getOwner()));
        if (call.getName().equals(Constants.Init)) {
            name += call.getOwner().substring(call.getOwner().lastIndexOf('/'));
        } else {
            name += call.getName();
        }
        return name;
    }

    private static List<SinkParameter> parseParameters(JSONObject sinkJson) {
        List<SinkParameter> list = new ArrayList<>();
        JSONArray array = (JSONArray) sinkJson.get("parameters");
        for (Object parameterObject : array) {
            JSONObject parameter = (JSONObject) parameterObject;
            int paramIndex = parameter.getInt("paramIndex");
            list.add(new SinkParameter(paramIndex));
        }
        return list;
    }

    private static List<Position> parsePositions(JSONObject sinkJson) {
        List<Position> list = new ArrayList<>();
        JSONArray array = (JSONArray) sinkJson.get("positions");
        for (Object positionObject : array) {
            JSONObject position = (JSONObject) positionObject;
            String className = position.getString("className");
            String methodName = position.getString("methodName");
            int javaSourcePosition = position.getInt("javaSourcePosition");
            list.add(new Position(className,methodName,javaSourcePosition));
        }
        return list;
    }

    private FunctionCall parseFunctionCall(JSONObject jsonObject) throws ClassNotFoundException, NoSuchMethodException {
        String fqn = jsonObject.getString("fqn");

        int start = 0;
        if (fqn.contains(":")) {
            start = fqn.indexOf(':') + 1;
        }
        int end = fqn.indexOf('(');
        String className = fqn.substring(start, end);
        String methodName;
        boolean isConstructor;
        int opcodes = Opcodes.INVOKEVIRTUAL;
        if (Character.isLowerCase(className.charAt(className.lastIndexOf('.') + 1))) {
            methodName = className.substring(className.lastIndexOf('.') + 1);
            className = className.substring(0, className.lastIndexOf('.'));
            isConstructor = false;
        } else {
            methodName = "<init>";
            isConstructor = true;
            opcodes = Opcodes.INVOKESPECIAL;
        }
        Class<?> cls = Class.forName(className);
        boolean isInterface = cls.isInterface();
        if (isInterface) {
            opcodes = Opcodes.INVOKEINTERFACE;
        }
        String internalName = Utils.dotToSlash(className);
        String[] parameters = fqn.substring(fqn.indexOf('(') + 1, fqn.indexOf(')')).split(", ");
        List<Class<?>> parameterClasses = new ArrayList<>();
        for (String s : parameters) {
            String parameter = this.convertForArray(s);
            parameterClasses.add(this.resolveClass(parameter));
        }
        Type type;
        if (isConstructor) {
            type = Type.getType(cls.getConstructor(parameterClasses.toArray(new Class<?>[0])));
        } else {
            type = Type.getType(cls.getMethod(methodName, parameterClasses.toArray(new Class<?>[0])));
        }
        return new FunctionCall(opcodes, internalName, methodName, type.getDescriptor(), isInterface);
    }

    private String convertForArray(final String string) throws ClassNotFoundException {
        String result = string;
        if (result.contains("[]")) {
            result = result.substring(0, result.indexOf('['));
            Class<?> cls = this.resolveClass(result);
            if (!cls.isPrimitive()) {
                result = String.format("L%s;", result);
            } else {
                result = Descriptor.classNameToDescriptorName(cls.getName());
            }
            for (int i = string.indexOf('['); i > 0; i = string.indexOf('[', i + 1)) {
                result = "[" + result;
            }
        }
        return result;
    }

    private JSONObject readJuturnaConfig() throws IOException {
        try(
                FileReader fileReader = new FileReader(this.juturnaConfigFile, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(fileReader)
        ) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
            return new JSONObject(content.toString());
        }
    }

    private Configuration readConfig() {
        return ConfigurationLoader.loadConfigurationFrom(this.fontusConfigFile);
    }

    private void saveConfig(Configuration configuration) throws IOException {
        ObjectMapper objectMapper = new XmlMapper(new WstxInputFactory(), new WstxOutputFactory());
        objectMapper.registerModule(new JaxbAnnotationModule());
        ObjectWriter objectWriter = objectMapper.writerFor(Configuration.class);
        FileWriter fileWriter = new FileWriter(this.outputFile, StandardCharsets.UTF_8);
        objectWriter.writeValue(fileWriter, configuration);
    }

    public static void main(String[] args) {
        new CommandLine(new Converter())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }
}
