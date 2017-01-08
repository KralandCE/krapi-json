package com.ricorei.ricopedia.migration;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParserException;
import org.kralandce.krapi.core.bean.ImmutableKPeople;
import org.kralandce.krapi.core.bean.KPeople;
import org.kralandce.krapi.core.model.kraland.MKAccount;
import org.kralandce.krapi.core.model.kraland.MKCity;
import org.kralandce.krapi.core.model.kraland.MKJob;
import org.kralandce.krapi.core.model.kraland.MKNation;
import org.kralandce.krapi.core.model.kraland.MKSex;
import org.kralandce.krapi.core.model.kraland.MKWealth;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.ricorei.ricopedia.migration.ScriptUtil.readJsonArray;
import static com.ricorei.ricopedia.migration.ScriptUtil.sqlToJavaDateTime;

/**
 * Transitional script used to migrate Ricopedia database to the new API format.
 */
final class CharacterImportScript {

    private final HashMap<Integer, String> avatarTable;
    private final HashMap<Integer, String> md5Table;
    private final HashMap<Integer, String> md5PathTable;
    private final HashMap<Integer, KPeople> entryToPeople;
    private final HashMap<Integer, AdditionalData> entryToAdditionalData;

    private CharacterImportScript() {
        this.avatarTable = new HashMap<>();
        this.md5Table = new HashMap<>();
        this.md5PathTable = new HashMap<>();
        this.entryToPeople = new HashMap<>();
        this.entryToAdditionalData = new HashMap<>();
    }

    public static void main(String[] args) throws IOException, JsonParserException {

        String version = "v6a";

        CharacterImportScript characterImportScript = new CharacterImportScript();

        characterImportScript.readFromJSON(version);

        if( version.equalsIgnoreCase("v6a") ) {
            characterImportScript.entryToAdditionalData.entrySet().forEach(entry -> {
                if( entry.getValue().deletion == null ) {
                    entry.getValue().deletion = LocalDate.of(2013, 2, 21); // because there were not update when it crashed
                }

            });
        }
    }

    public void readFromJSON(String version) throws IOException, JsonParserException {
        JsonArray ppersonnageJArray = readJsonArray(version, "ppersonnage.json");
        JsonArray listhashJArray = readJsonArray(version, "listhash.json");
        JsonArray listavatarJArray = readJsonArray(version, "listavatar.json");

        for( int i = 0; i < listavatarJArray.size(); i++ ) {
            JsonObject entry = listavatarJArray.getObject(i);
            int entryID = Integer.parseInt(entry.getString("id"));

            String avatarURL = entry.getString("avatar");
            this.avatarTable.put(checkNotNull(entryID), checkNotNull(avatarURL));
        }

        for( int i = 0; i < listhashJArray.size(); i++ ) {
            JsonObject entry = listhashJArray.getObject(i);
            int entryID = Integer.parseInt(entry.getString("id"));

            String md5 = entry.getString("md5");
            String md5Path = entry.getString("path");

            this.md5Table.put(entryID, checkNotNull(md5));
            this.md5PathTable.put(entryID, checkNotNull(md5Path));
        }

        for( int i = 0; i < ppersonnageJArray.size(); i++ ) {
            JsonObject entry = ppersonnageJArray.getObject(i);
            int entryID = Integer.parseInt(entry.getString("id"));

            this.entryToPeople.put(entryID, createPeople(entry));
            this.entryToAdditionalData.put(entryID, createAdditionalData(entry));
        }
    }

    private AdditionalData createAdditionalData(JsonObject main) {


        AdditionalData more = new AdditionalData();

        more.setCreation(sqlToJavaDateTime(main.getString("date_creation")).toLocalDate());

        if( !main.isNull("date_disparition") ) {
            more.setDeletion(sqlToJavaDateTime(main.getString("date_creation")).toLocalDate());
        }

        String refHashAsString = main.getString("ref_hash");

        if( refHashAsString != null ) {
            int refHash = Integer.parseInt(refHashAsString);
            more.setAvatarMd5(this.md5Table.get(refHash));
            more.setAvatarMd5Path(this.md5PathTable.get(refHash));
        }

        return more;
    }

    private ImmutableKPeople createPeople(JsonObject main) {

        ImmutableKPeople.Builder peopleBuilder = ImmutableKPeople.builder();

        peopleBuilder.setAccountIdentifier(MKAccount.Identifier.of(Integer.parseInt(main.getString("id_kraland"))));
        peopleBuilder.setAccountName(MKAccount.Name.of(main.getString("pseudo")));
        peopleBuilder.setAccountPermissionLevel(null);
        peopleBuilder.setAccountAvatarURL(
            MKAccount.AvatarURL.of(this.avatarTable.get(Integer.parseInt(main.getString("ref_avatar")))));

        peopleBuilder.setJobAccumulatedLevel(MKJob.AccumulatedLevel.of(Integer.parseInt(main.getString("niveau"))));
        peopleBuilder.setJobArea(MKJob.Area.of(Integer.parseInt(main.getString("area"))));
        peopleBuilder.setJobIdentifier(MKJob.Identifier.of(Integer.parseInt(main.getString("fonction"))));

        peopleBuilder.setCityAddressIdentifier(MKCity.Identifier.of(Integer.parseInt(main.getString("domiciliation"))));
        peopleBuilder.setNationAddressIdentifier(MKNation.Identifier.of(Integer.parseInt(main.getString("empire"))));

        peopleBuilder.setSexIdentifier(MKSex.Identifier.of(Integer.parseInt(main.getString("sexe"))));
        peopleBuilder.setCharacterWealthLevel(MKWealth.Level.of(Integer.parseInt(main.getString("argent"))));

        return peopleBuilder.build();
    }

    private static final class AdditionalData {
        private LocalDate creation;
        private LocalDate deletion;
        private String avatarMd5Path;
        private String avatarMd5;

        private AdditionalData() {
            this.creation = null;
            this.deletion = null;
            this.avatarMd5 = null;
            this.avatarMd5 = null;
        }

        public void setCreation(LocalDate date) {
            this.creation = checkNotNull(date);
        }

        public void setDeletion(LocalDate date) {
            this.deletion = date;
        }

        public void setAvatarMd5Path(String path) {
            this.avatarMd5Path = checkNotNull(path);
        }

        public void setAvatarMd5(String hashMd5) {
            this.avatarMd5 = checkNotNull(hashMd5);
        }
    }
}
