package com.sap.fontus.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class IOUtils {
    private IOUtils() {
    }

    public static byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024 * 4];
        int n;

        while (-1 != (n = inputStream.read(buffer))) {
            out.write(buffer, 0, n);
        }

        return out.toByteArray();
    }

    public static void writeToFile(List<String> strings, File output) throws IOException {
        if (output.isDirectory()) {
            throw new IOException("File is a directory: " + output.getAbsolutePath());
        }

        if (!output.exists()) {
            if (!output.createNewFile()) {
                throw new IOException("Could not create file: " + output.getAbsolutePath());
            }
        }

        if (!output.canWrite()) {
            throw new IOException("File is not writable: " + output.getAbsolutePath());
        }

        String concatenated = String.join("\n", strings);

        BufferedWriter writer = new BufferedWriter(new FileWriter(output, StandardCharsets.UTF_8));

        writer.write(concatenated);

        writer.flush();
        writer.close();
    }

    public static List<String> readAllLines(File file) throws IOException {
        try(BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
    }
}
