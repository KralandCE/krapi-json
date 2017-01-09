package org.kralandce.krapi.json.contract;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FieldContract {
    private final String label;
    private final Class type;

    private FieldContract(String fieldLabel, Class fieldType) {
        this.label = checkNotNull(fieldLabel);
        this.type = checkNotNull(fieldType);
    }

    public static FieldContract of(String fieldLabel, Class fieldClass) {
        return new FieldContract(fieldLabel, fieldClass);
    }

    public String getLabel() {
        return this.label;
    }

    public Class getFieldClass() {
        return this.type;
    }
}
