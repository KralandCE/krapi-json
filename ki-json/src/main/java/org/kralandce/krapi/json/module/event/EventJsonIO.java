package org.kralandce.krapi.json.module.event;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.kralandce.krapi.json.contract.DataContract;
import org.kralandce.krapi.json.contract.DataSet;
import org.kralandce.krapi.json.contract.FieldContract;

import java.time.LocalDate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class EventJsonIO {

    public static final DataContract versionContract = DataContract.builder()
        .add(FieldContract.of("major", Integer.class))
        .add(FieldContract.of("minor", Integer.class))
        .build();

    public static final DataContract dateContract = DataContract.builder()
        .add(FieldContract.of("year", Integer.class))
        .add(FieldContract.of("month", Integer.class))
        .add(FieldContract.of("day", Integer.class))
        .build();

    public static final DataContract fileContract = DataContract.builder()
        .add(FieldContract.of("version", JsonObject.class))
        .add(FieldContract.of("date", JsonObject.class))
        .add(FieldContract.of("events", JsonArray.class))
        .build();

    private EventJsonIO() {

    }

    private static JsonObject toJsonObject(DataSet dataSet, DataContract contract) {
        checkNotNull(dataSet);
        checkNotNull(contract);
        checkArgument(dataSet.validate(contract));

        JsonObject jsonObject = new JsonObject();
        for( FieldContract fieldContract : contract ) {
            Object field = dataSet.get(fieldContract.getLabel());

            JsonElement fieldAsJson = null;

            if( fieldContract.getFieldClass() == Integer.class ) {
                fieldAsJson = new JsonPrimitive((Integer) field);
            }

            if( fieldContract.getFieldClass() == String.class ) {
                fieldAsJson = new JsonPrimitive((String) field);
            }

            if( fieldContract.getFieldClass() == JsonObject.class ) {
                fieldAsJson = (JsonObject) field;
            }

            if( fieldContract.getFieldClass() == JsonArray.class ) {
                fieldAsJson = (JsonArray) field;
            }

            if( fieldAsJson != null ) {
                jsonObject.add(fieldContract.getLabel(), fieldAsJson);
            }
        }

        return jsonObject;
    }

    public static void main(String[] args) {
        LocalDate date = LocalDate.of(2017, 1, 9);

        DataSet event = new DataSet();

        Version1Minor0.write(date, ImmutableList.of(event));

    }

    public static final class Version1Minor0 {

        public static final DataContract singleEventContract = DataContract.builder()
            .add(FieldContract.of("year", Integer.class))
            .add(FieldContract.of("month", Integer.class))
            .add(FieldContract.of("day", Integer.class))
            .add(FieldContract.of("hour", Integer.class))
            .add(FieldContract.of("minute", Integer.class))
            .add(FieldContract.of("second", Integer.class))
            .add(FieldContract.of("nation", Integer.class))
            .add(FieldContract.of("province", String.class))
            .add(FieldContract.of("city", String.class))
            .add(FieldContract.of("type", String.class))
            .add(FieldContract.of("content", String.class))
            .build();
        private static final DataSet VERSION_SET = new DataSet().add("major", 1).add("minor", 0);

        private Version1Minor0() {

        }

        public static void write(LocalDate date, ImmutableList<DataSet> events) {
            checkNotNull(date);
            checkNotNull(events);

            DataSet dateSet = new DataSet().add("year", date.getYear())
                .add("month", date.getMonthValue())
                .add("day", date.getDayOfMonth());

            JsonObject dateJson = toJsonObject(dateSet, dateContract);
            JsonObject versionJson = toJsonObject(VERSION_SET, versionContract);

            JsonArray eventsJson = new JsonArray();

            for( DataSet item : events ) {
                JsonObject eventJson = toJsonObject(item, singleEventContract);
                eventsJson.add(eventJson);
            }

            DataSet fileSet = new DataSet().add("version", versionJson).add("date", dateJson).add("events", eventsJson);

            JsonObject fileObject = toJsonObject(fileSet, fileContract);
        }

    }
}
