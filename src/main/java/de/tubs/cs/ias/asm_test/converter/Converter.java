package de.tubs.cs.ias.asm_test.converter;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.asm.FunctionCall;
import de.tubs.cs.ias.asm_test.config.*;
import de.tubs.cs.ias.asm_test.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import picocli.CommandLine;

import java.io.*;
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
    public Void call() throws Exception {
        try {
            Configuration fontusConfig = readConfig();
            JSONObject juturnaObject = readJuturnaConfig();

            JSONArray jsonArray = (JSONArray) juturnaObject.get("sinks");
            Configuration configuration = mergeSinks(fontusConfig, jsonArray);
            jsonArray = (JSONArray) juturnaObject.get("sources");
            Configuration configuration1 = mergeSources(fontusConfig, jsonArray);
            configuration.append(configuration1);

            saveConfig(configuration);
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
                FunctionCall call = parseFunctionCall(sourceJson);
                boolean isContained = false;
                for (Source source : fontusConfig.getSourceConfig().getSources()) {
                    if (call.equals(source.getFunction())) {
                        isContained = true;
                        break;
                    }
                }
                if (!isContained) {
                    String sourceName = generateName(call);
                    sources.add(new Source(sourceName, call));
                }
            } catch (Exception e) {
                System.err.println("Some error occured with this:");
                System.err.println(sourceJson.toString());
                System.err.println(e + ": " + e.getMessage());
            }
        }
        Configuration configuration = new Configuration();
        configuration.getSourceConfig().append(new SourceConfig(sources));
        return configuration;
    }

    private Configuration mergeSinks(Configuration fontusConfig, JSONArray jsonArray) throws NoSuchMethodException, ClassNotFoundException {
        List<Sink> sinks = new ArrayList<>();
        for (Object sinkJsonO : jsonArray) {
            JSONObject sinkJson = (JSONObject) sinkJsonO;
            try {
                FunctionCall call = parseFunctionCall(sinkJson);
                boolean isContained = false;
                for (Sink sink : fontusConfig.getSinkConfig().getSinks()) {
                    if (call.equals(sink.getFunction())) {
                        isContained = true;
                        break;
                    }
                }
                if (!isContained) {
                    List<SinkParameter> sinkParameters = parseParameters(sinkJson);
                    String category = parseCategory(sinkJson);
                    String sinkName = generateName(call);
                    sinks.add(new Sink(sinkName, call, sinkParameters, category));
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

    private String parseCategory(JSONObject sinkJson) {
        JSONArray array = (JSONArray) sinkJson.get("parameters");
        String category = null;
        for (Object parameterObject : array) {
            JSONObject parameter = (JSONObject) parameterObject;
            category = parameter.getString("category");
        }
        return category;
    }

    private String generateName(FunctionCall call) {
        String name = Utils.fixupReverse(call.getOwner()) + ".";
        if (call.getName().equals(Constants.Init)) {
            name += call.getOwner().substring(call.getOwner().lastIndexOf('/'));
        } else {
            name += call.getName();
        }
        return name;
    }

    private List<SinkParameter> parseParameters(JSONObject sinkJson) {
        List<SinkParameter> list = new ArrayList<>();
        JSONArray array = (JSONArray) sinkJson.get("parameters");
        for (Object parameterObject : array) {
            JSONObject parameter = (JSONObject) parameterObject;
            int paramIndex = parameter.getInt("paramIndex");
            list.add(new SinkParameter(paramIndex));
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
        Class cls = Class.forName(className);
        boolean isInterface = cls.isInterface();
        if (isInterface) {
            opcodes = Opcodes.INVOKEINTERFACE;
        }
        String internalName = Utils.fixupReverse(className);
        String[] parameters = fqn.substring(fqn.indexOf('(') + 1, fqn.indexOf(')')).split(", ");
        List<Class> parameterClasses = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            String parameter = convertForArray(parameters[i]);
            parameterClasses.add(resolveClass(parameter));
        }
        Type type;
        if (isConstructor) {
            type = Type.getType(cls.getConstructor(parameterClasses.toArray(new Class[0])));
        } else {
            type = Type.getType(cls.getMethod(methodName, parameterClasses.toArray(new Class[0])));
        }
        return new FunctionCall(opcodes, internalName, methodName, type.getDescriptor(), isInterface);
    }

    private String convertForArray(final String string) throws ClassNotFoundException {
        String result = string;
        if (result.contains("[]")) {
            result = result.substring(0, result.indexOf('['));
            Class<?> cls = resolveClass(result);
            if (!cls.isPrimitive()) {
                result = "L" + result + ";";
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
        FileReader fileReader = new FileReader(this.juturnaConfigFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line);
        }
        return new JSONObject(content.toString());
    }

    private Configuration readConfig() {
        return ConfigurationLoader.loadConfigurationFrom(this.fontusConfigFile);
    }

    private void saveConfig(Configuration configuration) throws IOException {
        ObjectMapper objectMapper = new XmlMapper(new WstxInputFactory(), new WstxOutputFactory());
        objectMapper.registerModule(new JaxbAnnotationModule());
        ObjectWriter objectWriter = objectMapper.writerFor(Configuration.class);
        FileWriter fileWriter = new FileWriter(this.outputFile);
        objectWriter.writeValue(fileWriter, configuration);
    }

    public static void main(String[] args) {
        new CommandLine(new Converter())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }
}
