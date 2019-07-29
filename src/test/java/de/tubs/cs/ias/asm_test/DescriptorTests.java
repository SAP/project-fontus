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
        Descriptor pd = Descriptor.parseDescriptor(Constants.ToStringDesc);
        assertEquals(d, pd, "toString should have no parameters and return Ljava/lang/String;");
    }

    @Test
    void parseCharArrayDescriptor() {
        Descriptor d = new Descriptor("[C", "V");
        Descriptor pd = Descriptor.parseDescriptor("([C)V");
        assertEquals(d, pd, "Both descriptors should have one char array parameter and void return type.");
    }

    @Test
    void parseArrayDescriptor() {
        Descriptor d = new Descriptor("[Ljava/lang/String;", "[C");
        Descriptor pd = Descriptor.parseDescriptor("([Ljava/lang/String;)[C");
        assertEquals(d, pd, "Both descriptors should have one String array array parameter and a char array return type.");
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
        return Stream.of(
                Arguments.of(
                        new Descriptor("I",
                                Constants.StringDesc,
                                "[Ljava/lang/Object;",
                                "F",
                                "Ljava/util/function/Function;",
                                "Ljava/lang/Float;",
                                "Ljava/util/List;"
                        ),
                        Constants.StringDesc,
                        Constants.TStringDesc,
                        new Descriptor("I",
                                Constants.TStringDesc,
                                "[Ljava/lang/Object;",
                                "F",
                                "Ljava/util/function/Function;",
                                "Ljava/lang/Float;",
                                "Ljava/util/List;"
                        )
                ),
                Arguments.of(
                        new Descriptor("[Ljava/lang/Object;", "Ljava/util/List;"),
                        Constants.StringDesc,
                        Constants.TStringDesc,
                        new Descriptor("[Ljava/lang/Object;", "Ljava/util/List;")),
                Arguments.of(
                        new Descriptor("[Ljava/lang/String;", "V"),
                        Constants.StringDesc,
                        Constants.TStringDesc,
                        new Descriptor("[" + Constants.TStringDesc, "V")
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
