package org.kralandce.krapi.json.contract.field;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.kralandce.krapi.json.contract.FieldJson;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class JsonArrayField implements FieldJson {

    private final String fieldLabel;

    private JsonArrayField(String label) {
        this.fieldLabel = checkNotNull(label);
    }

    public static JsonArrayField of(String fieldLabel) {
        return new JsonArrayField(fieldLabel);
    }

    @Override
    public String getFieldLabel() {
        return this.fieldLabel;
    }

    @Override
    public Class getFieldClass() {
        return JsonArray.class;
    }

    @Override
    public JsonElement toJson(Object value) {
        checkArgument(isClass(value.getClass()));
        return (JsonElement) value;
    }

    @Override
    public Object toValue(JsonElement jsonElement) {
        return jsonElement;
    }
}
