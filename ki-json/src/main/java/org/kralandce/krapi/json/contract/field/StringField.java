package org.kralandce.krapi.json.contract.field;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.kralandce.krapi.json.contract.FieldJson;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class StringField implements FieldJson {

    private final String fieldLabel;

    private StringField(String label) {
        this.fieldLabel = checkNotNull(label);
    }

    public static StringField of(String fieldLabel) {
        return new StringField(fieldLabel);
    }

    @Override
    public String getFieldLabel() {
        return this.fieldLabel;
    }

    @Override
    public Class getFieldClass() {
        return String.class;
    }

    @Override
    public JsonElement toJson(Object value) {
        checkArgument(isClass(value.getClass()));
        return new JsonPrimitive((String) value);
    }

    @Override
    public Object toValue(JsonElement jsonElement) {
        return jsonElement.getAsString();
    }
}
