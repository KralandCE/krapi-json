package org.kralandce.krapi.json.contract;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class JsonContract implements DataContract<JsonObject, JsonArray> {

    private final ImmutableList<FieldJson> contracts;

    private JsonContract(ImmutableList<FieldJson> build) {
        this.contracts = build;
    }

    public static JsonContract.Builder builder() {
        return new JsonContract.Builder();
    }

    @Override
    public ImmutableList<? extends FieldContract> getContracts() {
        return this.contracts;
    }

    @Override
    public JsonObject toSingle(DataSet dataSet) {
        checkNotNull(dataSet);
        checkArgument(dataSet.validate(this.contracts));

        JsonObject jsonObject = new JsonObject();

        for( FieldJson fieldContract : this.contracts ) {

            Object field = dataSet.get(fieldContract.getFieldLabel());

            if( field != null ) {
                JsonElement fieldAsJson = fieldContract.toJson(field);
                jsonObject.add(fieldContract.getFieldLabel(), fieldAsJson);
            }
        }

        return jsonObject;
    }

    @Override
    public JsonArray toMultiple(ImmutableList<DataSet> dataSets) {
        JsonArray jsonArray = new JsonArray();

        for( DataSet dataSet : dataSets ) {
            jsonArray.add(toSingle(dataSet));
        }

        return jsonArray;
    }

    @Override
    public DataSet fromSingle(JsonObject jsonObject) {
        checkNotNull(jsonObject);

        DataSet dataSet = new DataSet();

        for( FieldJson fieldContract : this.contracts ) {

            String fieldLabel = fieldContract.getFieldLabel();

            JsonElement fieldAsJson = jsonObject.get(fieldLabel);

            if( fieldAsJson != null ) {
                Object fieldValue = fieldContract.toValue(fieldAsJson);
                dataSet.add(fieldLabel, fieldValue);
            }
            else {
                dataSet.add(fieldLabel, null);
            }

        }

        return dataSet;
    }

    @Override
    public ImmutableList<DataSet> fromMultiple(JsonArray jsonArray) {

        ImmutableList.Builder<DataSet> dataSetBuilder = ImmutableList.builder();

        for( JsonElement element : jsonArray ) {
            dataSetBuilder.add(fromSingle(element.getAsJsonObject()));
        }

        return dataSetBuilder.build();
    }

    public static final class Builder {

        private final ImmutableList.Builder<FieldJson> contractBuilder;

        private boolean build;

        private Builder() {
            this.contractBuilder = ImmutableList.builder();
            this.build = false;
        }

        public Builder add(FieldJson contract) {
            this.contractBuilder.add(contract);
            return this;
        }

        public Builder add(FieldJson contract, FieldJson... contracts) {
            add(contract);
            this.contractBuilder.add(contracts);
            return this;
        }

        public JsonContract build() {
            checkState(!this.build);
            this.build = true;
            return new JsonContract(this.contractBuilder.build());
        }
    }
}
