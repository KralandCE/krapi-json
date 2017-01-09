package org.kralandce.krapi.json.contract;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkState;

public final class DataContract implements Iterable<FieldContract> {

    private final ImmutableList<FieldContract> contract;

    private DataContract(ImmutableList<FieldContract> build) {
        this.contract = build;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Iterator<FieldContract> iterator() {
        return this.contract.iterator();
    }

    public static final class Builder {

        private final ImmutableList.Builder<FieldContract> contractBuilder;

        private boolean build;

        private Builder() {
            this.contractBuilder = ImmutableList.builder();
            this.build = false;
        }

        public Builder add(FieldContract contract) {
            this.contractBuilder.add(contract);
            return this;
        }

        public Builder add(FieldContract contract, FieldContract... contracts) {
            add(contract);
            this.contractBuilder.add(contracts);
            return this;
        }

        public DataContract build() {
            checkState(!this.build);
            this.build = true;
            return new DataContract(this.contractBuilder.build());
        }
    }
}
