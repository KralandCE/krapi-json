package org.kralandce.krapi.json.contract.field;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.kralandce.krapi.json.contract.FieldJson;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class JsonObjectField implements FieldJson {

    private final String fieldLabel;

    private JsonObjectField(String label) {
        this.fieldLabel = checkNotNull(label);
    }

    public static JsonObjectField of(String fieldLabel) {
        return new JsonObjectField(fieldLabel);
    }

    @Override
    public String getFieldLabel() {
        return this.fieldLabel;
    }

    @Override
    public Class getFieldClass() {
        return JsonObject.class;
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
