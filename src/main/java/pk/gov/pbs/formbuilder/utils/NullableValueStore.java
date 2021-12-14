package pk.gov.pbs.formbuilder.utils;

import androidx.annotation.Nullable;

public class NullableValueStore extends ValueStore {

    public NullableValueStore(@Nullable String val) {
        super("");
        mStoredValue = val;
    }

    public NullableValueStore(@Nullable Boolean val) {
        super("");
        setValue(val);
    }

    public NullableValueStore(@Nullable Integer val) {
        super("");
        setValue(val);
    }

    public NullableValueStore(@Nullable Long val) {
        super("");
        setValue(val);
    }

    public NullableValueStore(@Nullable Double val) {
        super("");
        setValue(val);
    }

    public NullableValueStore(@Nullable Float val) {
        super("");
        setValue(val);
    }

    public NullableValueStore(@Nullable Character val) {
        super("");
        setValue(val);
    }

    public void setValue(@Nullable String str) {
        if (str != null)
            super.setValue(str);
        else
            mStoredValue = null;
    }

    public void setValue(@Nullable Boolean val) {
        if (val != null)
            super.setValue(val);
        else
            mStoredValue = null;
    }

    public void setValue(@Nullable Short val) {
        if (val != null)
            super.setValue(val);
        else
            mStoredValue = null;
    }

    public void setValue(@Nullable Integer val) {
        if (val != null)
            super.setValue(val);
        else
            mStoredValue = null;
    }

    public void setValue(@Nullable Long val) {
        if (val != null)
            super.setValue(val);
        else
            mStoredValue = null;
    }

    public void setValue(@Nullable Double val) {
        if (val != null)
            super.setValue(val);
        else
            mStoredValue = null;
    }

    public void setValue(@Nullable Float val) {
        if (val != null)
            super.setValue(val);
        else
            mStoredValue = null;
    }

    public void setValue(@Nullable Character val) {
        if (val != null)
            super.setValue(val);
        else
            mStoredValue = null;
    }
}
