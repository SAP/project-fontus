package com.sap.fontus.sql;

import org.junit.jupiter.params.provider.Arguments;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

class TestCaseFileParser {
    static List<Arguments> parseTestCases(@SuppressWarnings("SameParameterValue") String filename) throws IOException {
        List<Arguments> testCases = new ArrayList<>();
        TestCaseFileReader reader = new TestCaseFileReader(filename);
        while (!reader.isEndOfFile()) {
            String[] lines = reader.readToBlank();
            if (lines.length < 2) {
                String e = "Expected at least two lines followed by a blank line or end of file.";
                reader.throwOnLine(e);
            }

            String input = lines[0];
            StringJoiner outputJoiner = new StringJoiner("\n");
            for (int i = 1; i < lines.length; i++) {
                outputJoiner.add(lines[i]);
            }
            String output = outputJoiner.toString();

            //String[] testCase = new String[]{input, output};
            testCases.add(Arguments.of(input, output));
        }

        return testCases;
    }

    private static class TestCaseFileReader {
        private BufferedReader reader;
        private int lineNumber;
        private boolean isEndOfFile;

        TestCaseFileReader(String filename) throws FileNotFoundException {
            reader = new BufferedReader(new FileReader(filename));
        }

        String[] readToBlank() throws IOException {
            if(isEndOfFile) {
                return new String[]{};
            }

            List<String> lines = new LinkedList<>();
            while(true) {
                String line = readLine();
                if(line == null || line.isEmpty()) {
                    break;
                }
                lines.add(line);
            }

            return lines.toArray(new String[0]);
        }

        boolean isEndOfFile() {
            return isEndOfFile;
        }

        void throwOnLine(String message) {
            String s = String.format("Error on line %s: %s", lineNumber, message);
            throw new RuntimeException(s);
        }

        private String readLine() throws IOException {
            String line = reader.readLine();
            if(line != null) {
                lineNumber++;

                if(line.startsWith("#")) {
                    return readLine();
                }
            } else {
                isEndOfFile = true;
            }

            return line;
        }
    }
}
