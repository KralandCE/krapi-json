package org.kralandce.krapi.json.contract;

import com.google.gson.JsonElement;

public interface FieldJson extends FieldContract {

    public JsonElement toJson(Object value);

    public Object toValue(JsonElement jsonElement);
}
