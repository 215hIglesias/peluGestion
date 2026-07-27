package com.pelugestion.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Ayudas para leer/escribir fechas de forma tolerante.
 * Internamente se guardan en ISO (yyyy-MM-dd).
 */
public final class DateUtil {

    private DateUtil() {}

    private static final DateTimeFormatter[] FORMATOS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
    };

    /** Intenta interpretar una fecha en varios formatos. Devuelve null si no puede. */
    public static LocalDate parseFlexible(String text) {
        if (text == null) return null;
        String t = text.trim();
        if (t.isEmpty()) return null;
        for (DateTimeFormatter f : FORMATOS) {
            try {
                return LocalDate.parse(t, f);
            } catch (Exception ignored) {
                // probar el siguiente formato
            }
        }
        return null;
    }

    /** Devuelve la fecha en ISO (yyyy-MM-dd) o cadena vacia si es null. */
    public static String toIso(LocalDate date) {
        return date != null ? date.toString() : "";
    }
}
