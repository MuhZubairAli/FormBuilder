package pk.gov.pbs.formbuilder.utils;

import androidx.annotation.NonNull;

public class ValueStore {
    protected String mStoredValue;

    public ValueStore(@NonNull String val){
        setValue(val);
    }

    public ValueStore(boolean val) {
        setValue(val);
    }

    public ValueStore(int val){
        setValue(val);
    }

    public ValueStore(long val) {
        setValue(val);
    }

    public ValueStore(double val) {
        setValue(val);
    }

    public ValueStore(float val) {
        setValue(val);
    }

    public ValueStore(char val) {
        setValue(val);
    }

    public void setValue(String str){
        this.mStoredValue = str;
    }

    public void setValue(boolean val){
        this.mStoredValue = String.format("%b", val);
    }

    public void setValue(short val) {
        this.mStoredValue = String.format("%d",val);
    }

    public void setValue(int val){
        this.mStoredValue = String.format("%d",val);
    }

    public void setValue(long val) {
        this.mStoredValue = String.format("%d", val);
    }

    public void setValue(double val) {
        this.mStoredValue = String.format("%.3f", val);
    }

    public void setValue(float val) {
        this.mStoredValue = String.format("%.3f", val);
    }

    public void setValue(char val) {
        this.mStoredValue = String.format("%c", val);
    }

    @Override
    public String toString(){
        return mStoredValue;
    }

    public boolean toBoolean(){
        return Boolean.parseBoolean(this.mStoredValue);
    }

    public int toInt(){
        return Integer.parseInt(this.mStoredValue);
    }

    public float toFloat(){
        return Float.parseFloat(this.mStoredValue);
    }

    public double toDouble(){
        return Double.parseDouble(this.mStoredValue);
    }

    public long toLong(){
        return Long.parseLong(this.mStoredValue);
    }

    public char toChar(){
        return this.mStoredValue.toCharArray()[0];
    }

    public Integer tryCastToInt(){
        try {
            return Integer.parseInt(this.mStoredValue);
        } catch (Exception e){
            String newValue = this.mStoredValue.replaceAll("\\D","");
            if (newValue.length() > 0)
                return Integer.parseInt(newValue);
        }

        return null;
    }

    public Float tryCastToFloat(){
        try {
            return Float.parseFloat(this.mStoredValue);
        } catch (Exception e){
            String newValue = this.mStoredValue.replaceAll("\\D","");
            if (newValue.length() > 0)
                return Float.parseFloat(newValue);
        }
        return null;
    }

    public Double tryCastToDouble(){
        try {
            return Double.parseDouble(this.mStoredValue);
        } catch (Exception e){
            String newValue = this.mStoredValue.replaceAll("\\D","");
            if (newValue.length() > 0)
                return Double.parseDouble(newValue);
        }
        return null;
    }

    public Long tryCastToLong(){
        try {
            return Long.parseLong(this.mStoredValue);
        } catch (Exception e){
            String newValue = this.mStoredValue.replaceAll("\\D","");
            if (newValue.length() > 0)
                return Long.parseLong(newValue);
        }
        return null;
    }

    public boolean equalsIgnoreCase(ValueStore obj) {
        if(obj == null && this.mStoredValue == null)
            return true;
        else if (obj == null)
            return false;
        return this.mStoredValue.equalsIgnoreCase(obj.toString());
    }

    public boolean equalsIgnoreCase(String str) {
        if(str == null && this.mStoredValue == null)
            return true;
        else if (str == null)
            return false;
        return this.mStoredValue.equalsIgnoreCase(str);
    }

    //Null is not considered empty
    public boolean isEmpty(){
        return this.mStoredValue.isEmpty();
    }

    public int length(){
        return this.mStoredValue.length();
    }
}
