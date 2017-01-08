package com.ricorei.ricopedia.migration;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParserException;
import org.kralandce.krapi.core.bean.ImmutableKEvent;
import org.kralandce.krapi.core.bean.KEvent;
import org.kralandce.krapi.core.model.kraland.MKCity;
import org.kralandce.krapi.core.model.kraland.MKEvent;
import org.kralandce.krapi.core.model.kraland.MKProvince;

import java.io.IOException;
import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.ricorei.ricopedia.migration.ScriptUtil.readJsonArray;
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

        String version = "v6b";

        EventImportScript eventImportScript = new EventImportScript();

        eventImportScript.readFromJSON(version);
    }

    public void readFromJSON(String version) throws IOException, JsonParserException {
        JsonArray evenementJArray = readJsonArray(version, "evenement.json");
        JsonArray listVilleJArray = readJsonArray(version, "eville.json");
        JsonArray listProvinceJArray = readJsonArray(version, "listprovince.json");

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

        for( int i = 0; i < evenementJArray.size(); i++ ) {
            JsonObject entry = evenementJArray.getObject(i);
            int entryID = Integer.parseInt(entry.getString("id"));

            this.entryToEvent.put(checkNotNull(entryID), createEvent(entry));
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
