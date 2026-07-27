package com.pelugestion.util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lectura y escritura de ficheros CSV.
 *
 * - Detecta automaticamente el separador (';' o ',').
 * - Soporta campos entrecomillados con comas/saltos de linea dentro.
 * - Al leer, ignora el BOM UTF-8 que anaden algunas apps (Excel).
 * - Al escribir, anade BOM UTF-8 y usa ';' para que Excel en espanol
 *   abra el fichero con las columnas y las tildes correctas.
 */
public final class CsvUtil {

    private CsvUtil() {}

    /** Lee un CSV y devuelve la lista de filas (cada fila = array de celdas). */
    public static List<String[]> read(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        String content = new String(bytes, StandardCharsets.UTF_8);

        // Quitar BOM UTF-8 si existe
        if (!content.isEmpty() && content.charAt(0) == '\uFEFF') {
            content = content.substring(1);
        }

        char delimiter = detectDelimiter(content);
        return parse(content, delimiter);
    }

    /** Escribe filas a un CSV (con BOM UTF-8 y separador ';', amigable para Excel). */
    public static void write(Path file, List<String[]> rows) throws IOException {
        char delimiter = ';';
        try (Writer w = new OutputStreamWriter(
                Files.newOutputStream(file), StandardCharsets.UTF_8)) {
            w.write('\uFEFF'); // BOM para que Excel detecte UTF-8
            for (String[] row : rows) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) sb.append(delimiter);
                    sb.append(escape(row[i] == null ? "" : row[i], delimiter));
                }
                sb.append("\r\n");
                w.write(sb.toString());
            }
        }
    }

    private static char detectDelimiter(String content) {
        int newline = content.indexOf('\n');
        String firstLine = newline >= 0 ? content.substring(0, newline) : content;
        long semis = firstLine.chars().filter(c -> c == ';').count();
        long commas = firstLine.chars().filter(c -> c == ',').count();
        return semis >= commas ? ';' : ',';
    }

    private static String escape(String value, char delimiter) {
        boolean needsQuotes = value.indexOf(delimiter) >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;
        if (!needsQuotes) return value;
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    /** Parser CSV con maquina de estados (soporta comillas y saltos internos). */
    private static List<String[]> parse(String content, char delimiter) {
        List<String[]> rows = new ArrayList<>();
        List<String> current = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
                        field.append('"');
                        i++; // comilla escapada ""
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == delimiter) {
                    current.add(field.toString());
                    field.setLength(0);
                } else if (c == '\r') {
                    // ignorar; el \n cierra la fila
                } else if (c == '\n') {
                    current.add(field.toString());
                    field.setLength(0);
                    rows.add(current.toArray(new String[0]));
                    current = new ArrayList<>();
                } else {
                    field.append(c);
                }
            }
        }

        // Ultima fila si el fichero no termina en salto de linea
        if (field.length() > 0 || !current.isEmpty()) {
            current.add(field.toString());
            rows.add(current.toArray(new String[0]));
        }

        return rows;
    }
}
