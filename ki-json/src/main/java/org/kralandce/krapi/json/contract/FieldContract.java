package org.kralandce.krapi.json.contract;

public interface FieldContract {

    public String getFieldLabel();

    public Class getFieldClass();

    default public boolean isClass(Class clazz) {
        return getFieldClass().equals(clazz);
    }
}
