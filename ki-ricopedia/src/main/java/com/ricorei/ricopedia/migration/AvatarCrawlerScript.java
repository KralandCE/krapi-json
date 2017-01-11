package com.ricorei.ricopedia.migration;

import com.google.common.collect.ImmutableList;
import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParserException;
import org.kralandce.krapi.core.model.kraland.MKAccount;
import org.kralandce.krapi.core.parser.picture.AvatarCrawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class AvatarCrawlerScript {

    private final List<MKAccount.AvatarURL> avatarTable;

    private AvatarCrawlerScript() {
        this.avatarTable = new ArrayList<>();
    }

    public static void main(String[] args) throws IOException, JsonParserException {

        String version = "v6a";

        AvatarCrawlerScript avatarCrawlerScript = new AvatarCrawlerScript();

        avatarCrawlerScript.readFromJSON(version);

        ImmutableList.Builder<AvatarCrawler> avatarCrawlerBuilder = ImmutableList.builder();

        avatarCrawlerScript.avatarTable.forEach(
            url -> avatarCrawlerBuilder.add(new AvatarCrawler(MKAccount.Identifier.of(0), url)));

        ImmutableList<AvatarCrawler> avatarCrawlers = avatarCrawlerBuilder.build();

        System.out.println("Crawling " + avatarCrawlers.size() + " avatars");

        avatarCrawlers.subList(0, 30).parallelStream().forEach(AvatarCrawler::fetch);

        avatarCrawlers.stream().filter(avatarCrawler -> avatarCrawler.getError().isPresent()).forEach(System.out::println);
    }

    public void readFromJSON(String version) throws IOException, JsonParserException {
        JsonArray listavatarJArray = ScriptUtil.readJsonArrayWithHeader(version, "listavatar.json");

        for( int i = 0; i < listavatarJArray.size(); i++ ) {
            JsonObject entry = listavatarJArray.getObject(i);

            int entryID = Integer.parseInt(entry.getString("id"));
            String avatarURL = entry.getString("avatar");

            this.avatarTable.add(MKAccount.AvatarURL.of(avatarURL));
        }
    }
}
