package pk.gov.pbs.formbuilder.utils;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ObservableValueStore extends ValueStore {
    ArrayList<OnValueChange> changeEvents;
    ValueStore oldValue;

    public ObservableValueStore(@NonNull String val) {
        super(val);
    }

    public ObservableValueStore(@NonNull String val, OnValueChange event) {
        super(val);
        addOnValueChangeListener(event);
    }

    public ObservableValueStore(boolean val) {
        super(val);
    }

    public ObservableValueStore(boolean val, OnValueChange event) {
        super(val);
        addOnValueChangeListener(event);
    }

    public ObservableValueStore(int val) {
        super(val);
    }

    public ObservableValueStore(int val, OnValueChange event) {
        super(val);
        addOnValueChangeListener(event);
    }

    public ObservableValueStore(long val) {
        super(val);
    }

    public ObservableValueStore(long val, OnValueChange event) {
        super(val);
        addOnValueChangeListener(event);
    }

    public ObservableValueStore(double val) {
        super(val);
    }

    public ObservableValueStore(double val, OnValueChange event) {
        super(val);
        addOnValueChangeListener(event);
    }

    public ObservableValueStore(float val) {
        super(val);
    }


    public ObservableValueStore(float val, OnValueChange event) {
        super(val);
        addOnValueChangeListener(event);
    }

    public ObservableValueStore(char val) {
        super(val);
    }

    public ObservableValueStore(char val, OnValueChange event) {
        super(val);
        addOnValueChangeListener(event);
    }

    @Override
    public void setValue(String str) {
        oldValue = new ValueStore(this.toString());
        super.setValue(str);
        callOnValueChange();
    }

    @Override
    public void setValue(boolean val) {
        oldValue = new ValueStore(this.toString());
        super.setValue(val);
        callOnValueChange();
    }

    @Override
    public void setValue(short val) {
        oldValue = new ValueStore(this.toString());
        super.setValue(val);
        callOnValueChange();
    }

    @Override
    public void setValue(int val) {
        oldValue = new ValueStore(this.toString());
        super.setValue(val);
        callOnValueChange();
    }

    @Override
    public void setValue(long val) {
        oldValue = new ValueStore(this.toString());
        super.setValue(val);
        callOnValueChange();
    }

    @Override
    public void setValue(double val) {
        oldValue = new ValueStore(this.toString());
        super.setValue(val);
        callOnValueChange();
    }

    @Override
    public void setValue(float val) {
        oldValue = new ValueStore(this.toString());
        super.setValue(val);
        callOnValueChange();
    }

    @Override
    public void setValue(char val) {
        oldValue = new ValueStore(this.toString());
        super.setValue(val);
        callOnValueChange();
    }

    private void callOnValueChange(){
        if(changeEvents != null) {
            for (short i = 0; i < changeEvents.size(); i++)
                (changeEvents.get(i)).onValueChange(this, oldValue);
        }
    }

    public void addOnValueChangeListener(OnValueChange event){
        if(changeEvents == null)
            changeEvents = new ArrayList<>();

        changeEvents.add(event);
    }

    public interface OnValueChange {
        void onValueChange(ValueStore newValue, ValueStore oldValue);
    }
}
