package com.ricorei.ricopedia.migration;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
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

    public static JsonArray readJsonArray(String version, String fileName) {

        Path path = getPath(version, fileName);

        //There is a header in exported JSON files from phpmyadmin
        //Header is using 8 lines

        try(BufferedReader bufferedReader = Files.newBufferedReader(path)) {

            for( int i = 0; i < 8; i++ ) {
                bufferedReader.readLine();
            }

            return JsonParser.array().from(bufferedReader);
        }
        catch(JsonParserException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
