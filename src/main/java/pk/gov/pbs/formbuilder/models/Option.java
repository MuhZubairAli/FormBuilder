package pk.gov.pbs.formbuilder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;
import pk.gov.pbs.formbuilder.pojos.DatumIdentifier;


public class Option extends Table {
    @NotNull
    @Expose
    @Unique
    @SerializedName("S")
    public String s;

    @NotNull
    @Expose
    @Unique
    @SerializedName("C")
    public String c;

    @Expose
    @Unique
    @SerializedName("V")
    public String v;

    @NotNull
    @Expose
    @Unique
    @SerializedName("Desc")
    public String desc;

    public Option() {}
    public Option (DatumIdentifier identifier, String desc){
        this.s = identifier.section;
        this.c = identifier.column;
        if(identifier.value != null)
            this.v = identifier.value.toString();
        this.desc = desc;
    }

    public Option (String s, String c, String v, String desc){
        this.s = s;
        this.c = c;
        this.v = v;
        this.desc = desc;
    }

    public Option (int s, int c, int v, String desc){
        this(String.valueOf(s), String.valueOf(c), String.valueOf(v), desc);
    }
}
