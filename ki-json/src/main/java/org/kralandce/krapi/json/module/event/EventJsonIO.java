package org.kralandce.krapi.json.module.event;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.kralandce.krapi.json.contract.DataSet;
import org.kralandce.krapi.json.contract.JsonContract;
import org.kralandce.krapi.json.contract.field.IntegerField;
import org.kralandce.krapi.json.contract.field.JsonArrayField;
import org.kralandce.krapi.json.contract.field.JsonObjectField;
import org.kralandce.krapi.json.contract.field.StringField;

import java.time.LocalDate;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class EventJsonIO {

    private static final JsonContract VERSION_CONTRACT = JsonContract.builder()
        .add(IntegerField.of("major"))
        .add(IntegerField.of("minor"))
        .build();

    private static final JsonContract DATE_CONTRACT = JsonContract.builder()
        .add(IntegerField.of("year"))
        .add(IntegerField.of("month"))
        .add(IntegerField.of("day"))
        .build();

    private static final JsonContract FILE_CONTRACT = JsonContract.builder()
        .add(JsonObjectField.of("version"))
        .add(JsonObjectField.of("date"))
        .add(JsonArrayField.of("events"))
        .build();

    private EventJsonIO() {

    }

    public static void main(String[] args) {
        LocalDate date = LocalDate.of(2017, 1, 9);

        DataSet event = new DataSet();

        Version1Minor0.write(date, ImmutableList.of(event));

    }

    public static final class Version1Minor0 {

        private static final int VERSION_MAJOR = 1;
        private static final int VERSION_MINOR = 0;

        private static final DataSet VERSION_DATASET = new DataSet().add("major", VERSION_MAJOR).add("minor", VERSION_MINOR);

        private static final JsonContract SINGLE_EVENT_CONTRACT = JsonContract.builder()
            .add(IntegerField.of("hour"))
            .add(IntegerField.of("minute"))
            .add(IntegerField.of("nation"))
            .add(StringField.of("province"))
            .add(StringField.of("city"))
            .add(StringField.of("type"))
            .add(StringField.of("content"))
            .build();

        private Version1Minor0() {

        }

        public static Optional<String> write(LocalDate date, ImmutableList<DataSet> events) {
            checkNotNull(date);
            checkNotNull(events);

            for( DataSet dataSet : events ) {

                if( !dataSet.validate(SINGLE_EVENT_CONTRACT.getContracts()) ) {
                    return Optional.empty();
                }
            }

            DataSet dateSet = new DataSet().add("year", date.getYear()).add("month", date.getMonthValue()).add("day", date.getDayOfMonth());

            JsonObject dateJson = DATE_CONTRACT.toSingle(dateSet);
            JsonObject versionJson = VERSION_CONTRACT.toSingle(VERSION_DATASET);
            JsonArray eventsJson = SINGLE_EVENT_CONTRACT.toMultiple(events);

            DataSet fileSet = new DataSet().add("version", versionJson).add("date", dateJson).add("events", eventsJson);

            JsonObject fileObject = FILE_CONTRACT.toSingle(fileSet);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return Optional.of(gson.toJson(fileObject));
        }
    }
}
