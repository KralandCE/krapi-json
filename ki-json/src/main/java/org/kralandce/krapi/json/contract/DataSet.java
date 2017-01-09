package org.kralandce.krapi.json.contract;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DataSet {
    private final Map<String, Object> values;

    public DataSet() {
        this.values = new HashMap<>();
    }

    public DataSet add(String label, Object value) {
        checkNotNull(label);
        this.values.put(label, value);
        return this;
    }

    public Object get(String label) {
        return this.values.get(label);
    }

    public boolean validate(DataContract contract) {
        for( FieldContract fieldContract : contract ) {

            // Keys must be set, even if value is null
            if( !this.values.containsKey(fieldContract.getLabel()) ) {
                return false;
            }

            Object value = this.values.get(fieldContract.getLabel());

            // If key is set and if value is not null, value must be of the right class
            if( value != null ) {
                if( !value.getClass().equals(fieldContract.getFieldClass()) ) {
                    return false;
                }
            }

        }

        return true;
    }
}
