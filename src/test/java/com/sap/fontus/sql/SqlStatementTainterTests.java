package com.sap.fontus.sql;

import com.sap.fontus.sql.tainter.StatementTainter;
import com.sap.fontus.sql.tainter.Taint;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlStatementTainterTests {


    static Stream<Arguments> statementsWithExpectedOutput() throws IOException {
        return TestCaseFileParser.parseTestCases("src/test/resources/com/sap/fontus/ValidStatementsWithExpectedOutput.txt").stream();
    }

    @ParameterizedTest(name = "{index} ==> ''{0}'' should result in ''{1}''")
    @MethodSource("statementsWithExpectedOutput")
    void testTainterResult(String input, String expectedOutput) throws JSQLParserException {
        List<Taint> taints = new ArrayList<>();
        taints.add(new Taint("Max", "777"));
        taints.add(new Taint("Mustermann", "6666666666"));
        taints.add(new Taint("name", "22222"));
        taints.add(new Taint("PCA", "22222"));
        StatementTainter tainter = new StatementTainter(taints);

        Statements stmts = CCJSqlParserUtil.parseStatements(input);
        stmts.accept(tainter);

        String actualOutput = stmts.toString().trim();
        assertEquals(expectedOutput, actualOutput);
    }
}
