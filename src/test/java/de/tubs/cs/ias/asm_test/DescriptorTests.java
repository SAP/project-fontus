package de.tubs.cs.ias.asm_test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.stream.Stream;

@SuppressWarnings({"DuplicateStringLiteralInspection", "SpellCheckingInspection"})
class DescriptorTests {

    @Test
    void parseToString() {
        Descriptor d = new Descriptor("Ljava/lang/String;");
        Descriptor pd = Descriptor.parseDescriptor("()Ljava/lang/String;");
        assertEquals(d, pd, "toString should have no parameters and return Ljava/lang/String;");
    }

    @ParameterizedTest(name = "(){0} = no parameters and {0} as return type.")
    @ValueSource(strings = {"Z", "B", "C", "S", "I", "F", "D", "J"})
    void parseNoParamReturnPrimitive(String primitive) {
        Descriptor d = new Descriptor(primitive);
        String descriptor = String.format("()%s", primitive);
        Descriptor pd = Descriptor.parseDescriptor(descriptor);
        assertEquals(d, pd, String.format("%s should parse to no params and %s as return type.", descriptor, primitive));
    }

    @ParameterizedTest(name = "({0})V = a {0} parameter and V as return type.")
    @ValueSource(strings = {"Z", "B", "C", "S", "I", "F", "D", "J"})
    void parsePrimitiveToVoid(String primitive) {
        String descriptor = String.format("(%s)V", primitive);
        Descriptor d = new Descriptor(new String[]{primitive}, "V");
        Descriptor pd = Descriptor.parseDescriptor(descriptor);
        assertEquals(d, pd, String.format("'%s' should parse to a '%s' param and 'V' as return type.", descriptor, primitive));

    }

    @ParameterizedTest
    @MethodSource("provideDescriptorData")
    void reassembleDescriptor(String descriptor, Descriptor d) {
        Descriptor pd = Descriptor.parseDescriptor(descriptor);
        assertEquals(descriptor, pd.toDescriptor(), String.format("%s should be equal to %s", descriptor, pd.toDescriptor()));
    }

    @ParameterizedTest
    @MethodSource("provideDescriptorData")
    void parseDescriptor(String descriptor, Descriptor d) {
        Descriptor pd = Descriptor.parseDescriptor(descriptor);
        assertEquals(d, pd, String.format("%s and %s should be equal.", d.toString(), pd.toString()));
    }

    @ParameterizedTest
    @MethodSource("provideDescriptorReplacementData")
    void replaceTypeInDescriptor(Descriptor initial, String from, String to, Descriptor result) {
        Descriptor rd = initial.replaceType(from, to);
        assertEquals(rd, result, String.format("After replacing all occurrences of '%s' in '%s' with '%s', the result should be: '%s'!", from, initial, to, result));
    }

    private static Stream<Arguments> provideDescriptorReplacementData() {
        String tstringDesc = Constants.TStringDesc + ";"; // TODO: fix globally
        String stringDesc = String.format("L%s;", Constants.String); // TODO: fix globally
        return Stream.of(
                Arguments.of(
                        new Descriptor("I",
                                stringDesc,
                                "[Ljava/lang/Object;",
                                "F",
                                "Ljava/util/function/Function;",
                                "Ljava/lang/Float;",
                                "Ljava/util/List;"
                        ),
                        stringDesc,
                        tstringDesc,
                        new Descriptor("I",
                                tstringDesc,
                                "[Ljava/lang/Object;",
                                "F",
                                "Ljava/util/function/Function;",
                                "Ljava/lang/Float;",
                                "Ljava/util/List;"
                        )
                ),
                Arguments.of(
                        new Descriptor("[Ljava/lang/Object;", "Ljava/util/List;"),
                        stringDesc,
                        tstringDesc,
                        new Descriptor("[Ljava/lang/Object;", "Ljava/util/List;")),
                Arguments.of(
                        new Descriptor("[Ljava/lang/String;", "V"),
                        stringDesc,
                        tstringDesc,
                        new Descriptor("[" + tstringDesc, "V")
                )
        );
    }

    private static Stream<Arguments> provideDescriptorData() {
        return Stream.of(
                Arguments.of("(ILjava/lang/String;[Ljava/lang/Object;FLjava/util/function/Function;Ljava/lang/Float;)Ljava/util/List;",
                        new Descriptor("I",
                                "Ljava/lang/String;",
                                "[Ljava/lang/Object;",
                                "F",
                                "Ljava/util/function/Function;",
                                "Ljava/lang/Float;",
                                "Ljava/util/List;"
                        )),
                Arguments.of("([Ljava/lang/Object;)Ljava/util/List;",
                        new Descriptor("[Ljava/lang/Object;", "Ljava/util/List;")),
                Arguments.of("([Ljava/lang/String;)V",
                        new Descriptor("[Ljava/lang/String;", "V")),
                Arguments.of("(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
                        new Descriptor("Ljava/lang/String;",
                                "[Ljava/lang/Object;",
                                "Ljava/lang/String;"
                        ))
        );
    }
}
