package org.kralandce.krapi.json.contract;

import com.google.common.collect.ImmutableList;

public interface DataContract<S, M> {
    public S toSingle(DataSet dataSet);

    public M toMultiple(ImmutableList<DataSet> dataSets);

    public DataSet fromSingle(S element);

    public ImmutableList<DataSet> fromMultiple(M elements);

    public ImmutableList<? extends FieldContract> getContracts();
}
