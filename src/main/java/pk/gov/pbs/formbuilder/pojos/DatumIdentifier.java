package pk.gov.pbs.formbuilder.pojos;

import androidx.annotation.NonNull;

import pk.gov.pbs.formbuilder.utils.ValueStore;

public class DatumIdentifier {
    public String key;
    public String section;
    public String column;
    public ValueStore value;

    public DatumIdentifier(String key){
        this.key = key;
    }

    public DatumIdentifier(String section, String column, ValueStore value){
        this(section, column);
        this.value = value;
    }

    public DatumIdentifier(String section, String column, int value){
        this(section, column, new ValueStore(value));
    }

    public DatumIdentifier(int section, int column, int value) {
        this(section, column, new ValueStore(value));
    }

    public DatumIdentifier(int section, int column, ValueStore value) {
        this(section, column);
        this.value = value;
    }

    public DatumIdentifier(String section, String column){
        this.section = section;
        this.column = column;
    }

    public DatumIdentifier(int section, int column) {
        this.section = String.valueOf(section);
        this.column = String.valueOf(column);
    }

    public DatumIdentifier setValue(ValueStore value){
        this.value = value;
        return this;
    }

    @NonNull
    public DatumIdentifier setValue(int value){
        this.value = new ValueStore(value);
        return this;
    }

    @NonNull
    public DatumIdentifier setValue(String value){
        this.value = new ValueStore(value);
        return this;
    }

    public String toString(){
        if(this.key != null)
            return this.key;
        String result = String.format("s%sq%s",section,column);
        if(value != null)
            return result + "_" + value.toString();
        else
            return result;
    }
}
