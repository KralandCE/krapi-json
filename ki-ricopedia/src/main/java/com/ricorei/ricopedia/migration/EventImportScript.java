package com.ricorei.ricopedia.migration;

import com.grack.nanojson.JsonAppendableWriter;
import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;
import org.kralandce.krapi.core.bean.ImmutableKEvent;
import org.kralandce.krapi.core.bean.KEvent;
import org.kralandce.krapi.core.model.kraland.MKCity;
import org.kralandce.krapi.core.model.kraland.MKEvent;
import org.kralandce.krapi.core.model.kraland.MKProvince;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.ricorei.ricopedia.migration.ScriptUtil.getSplitEventFilePath;
import static com.ricorei.ricopedia.migration.ScriptUtil.getSplitEventPath;
import static com.ricorei.ricopedia.migration.ScriptUtil.readJsonArrayWithoutHeader;
import static com.ricorei.ricopedia.migration.ScriptUtil.sqlToJavaDateTime;

final class EventImportScript {

    private final HashMap<Integer, String> villeTable;
    private final HashMap<Integer, String> provinceTable;
    private final HashMap<Integer, KEvent> entryToEvent;

    private EventImportScript() {
        this.entryToEvent = new HashMap<>();
        this.provinceTable = new HashMap<>();
        this.villeTable = new HashMap<>();
    }

    public static void main(String[] args) throws IOException, JsonParserException {

        String version = "v6a";

        /*
         * Phpmyadmin exports in json in a one line file.
         * Events are often a file of hundreds Mbytes
         * Parsing it without splitting it first may hang your computer.
         */
        //EventImportScript.splitEventsIntoFiles(version);


        EventImportScript eventImportScript = new EventImportScript();
        eventImportScript.readFromSplitFiles(version);

        //eventImportScript.readFromFullFile(version);
    }

    private static void splitEventsIntoFiles(String version) {

        try {
            Files.createDirectory(getSplitEventPath(version));
        }
        catch(IOException e) {
            // nothing
        }

        int eventsPerFile = 10_000;

        System.out.println("Importing events ... this may takes some time");

        JsonArray evenementJArray = ScriptUtil.readJsonArrayWithHeader(version, "evenement.json");

        System.out.println("Done importing events ...");

        JsonAppendableWriter jsonWriter = null;
        try {

            for( int i = 0; i < evenementJArray.size(); i++ ) {

                if( i % eventsPerFile == 0 ) {

                    if( jsonWriter != null ) {
                        jsonWriter.end();
                        jsonWriter.done();

                        System.out.println("Writing events ... " + (i - eventsPerFile));

                    }

                    Path splitFile = getSplitEventFilePath(version, "event_" + i + ".json");
                    BufferedWriter bufferedWriter = Files.newBufferedWriter(splitFile, Charset.forName("UTF-8"));
                    jsonWriter = JsonWriter.on(bufferedWriter).array();
                }

                JsonObject entry = evenementJArray.getObject(i);

                jsonWriter.value(entry);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        if( jsonWriter != null ) {
            jsonWriter.end();
            jsonWriter.done();
        }
    }

    public void readFromSplitFiles(String version) {
        populateForeignTables(version);

        System.out.println("Fetching files ... ");

        List<String> fileList = Collections.emptyList();
        try(Stream<Path> stream = Files.list(getSplitEventPath(version))) {
            fileList = stream.map(String::valueOf).filter(path -> !path.startsWith(".")).sorted().collect(Collectors.toList());
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        for( String file : fileList ) {
            Path filePath = Paths.get(file);

            System.out.println("Reading ... " + file);

            JsonArray evenementJArray = readJsonArrayWithoutHeader(version, filePath);

            for( int i = 0; i < evenementJArray.size(); i++ ) {
                JsonObject entry = evenementJArray.getObject(i);
                int entryID = Integer.parseInt(entry.getString("id"));

                this.entryToEvent.put(checkNotNull(entryID), createEvent(entry));
            }
        }
    }

    @Deprecated
    public void readFromFullFile(String version) throws IOException, JsonParserException {
        populateForeignTables(version);

        System.out.println("Importing events ... this may takes some time");

        JsonArray evenementJArray = ScriptUtil.readJsonArrayWithHeader(version, "evenement.json");

        System.out.println("Done importing events ...");

        for( int i = 0; i < evenementJArray.size(); i++ ) {
            JsonObject entry = evenementJArray.getObject(i);
            int entryID = Integer.parseInt(entry.getString("id"));

            this.entryToEvent.put(checkNotNull(entryID), createEvent(entry));
        }
    }

    private void populateForeignTables(String version) {

        JsonArray listVilleJArray = ScriptUtil.readJsonArrayWithHeader(version, "eville.json");
        JsonArray listProvinceJArray = ScriptUtil.readJsonArrayWithHeader(version, "listprovince.json");

        for( int i = 0; i < listVilleJArray.size(); i++ ) {
            JsonObject entry = listVilleJArray.getObject(i);
            int entryID = Integer.parseInt(entry.getString("id"));

            String ville = entry.getString("ville");
            this.villeTable.put(entryID, checkNotNull(ville));
        }

        for( int i = 0; i < listProvinceJArray.size(); i++ ) {
            JsonObject entry = listProvinceJArray.getObject(i);
            int entryID = Integer.parseInt(entry.getString("id"));

            String province = entry.getString("nom");
            this.provinceTable.put(entryID, checkNotNull(province));
        }
    }

    private ImmutableKEvent createEvent(JsonObject main) {

        ImmutableKEvent.Builder eventBuilder = ImmutableKEvent.builder();

        String refVille = main.getString("ville");

        if( refVille != null ) {
            eventBuilder.setCity(MKCity.Name.of(this.villeTable.get(Integer.parseInt(refVille))));
        }

        String refProvince = main.getString("province");

        if( refProvince != null ) {
            eventBuilder.setProvince(MKProvince.Name.of(this.provinceTable.get(Integer.parseInt(refProvince))));
        }

        eventBuilder.setNation(null); // Ricopedia do not store Nation for events. Maybe it was not possible in 2012.

        eventBuilder.setData(MKEvent.Content.of(main.getString("raw")));

        int eventTypeRef = Integer.parseInt(main.getString("type"));

        String eventType;

        switch(eventTypeRef) {
            case 1:
                eventType = "ev_normal";
                break;
            case 2:
                eventType = "ev_anim";
                break;
            case 3:
                eventType = "ev_maj";
                break;
            default:
                throw new UnsupportedOperationException();
        }

        eventBuilder.setType(MKEvent.Type.of(eventType));

        eventBuilder.setDate(MKEvent.Date.of(sqlToJavaDateTime(main.getString("date"))));

        return eventBuilder.build();
    }
}
