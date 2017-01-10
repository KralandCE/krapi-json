package org.kralandce.krapi.json.contract;

import java.util.HashMap;
import java.util.List;
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

    private boolean validate(FieldContract fieldContract) {

        // Keys must be set, even if value is null
        if( !this.values.containsKey(fieldContract.getFieldLabel()) ) {
            return false;
        }

        Object value = this.values.get(fieldContract.getFieldLabel());

        // If key is set and if value is not null, value must be of the right class
        if( value != null ) {
            if( !fieldContract.isClass(value.getClass()) ) {
                return false;
            }
        }

        return true;
    }

    public boolean validate(List<? extends FieldContract> fieldContracts) {
        for( FieldContract fieldContract : fieldContracts ) {
            if( !validate(fieldContract) ) {
                return false;
            }
        }

        return true;
    }
}
