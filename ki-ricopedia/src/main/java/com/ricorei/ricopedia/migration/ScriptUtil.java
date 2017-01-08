package com.ricorei.ricopedia.migration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class ScriptUtil {

    private static final DateTimeFormatter sqlDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ScriptUtil() {

    }

    public static LocalDateTime sqlToJavaDateTime(String sqlDate) {
        return LocalDateTime.parse(sqlDate, sqlDateFormatter);
    }

    public static Path getPath(String version, String fileName) {
        String home = System.getProperty("user.home");
        return Paths.get(home, "KralandCE", version, fileName);
    }
}
