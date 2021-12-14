package pk.gov.pbs.formbuilder.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;


public class Annexure extends Table {
    @NotNull
    @Expose
    @Unique
    public String identifier;

    @NotNull
    @Expose
    @Unique
    public String desc;

    @NotNull
    @Expose
    @Unique
    public String code;

    public Annexure(){}

    public Annexure(@NonNull String identifier, @NonNull String desc, @NonNull Long code) {
        this.identifier = identifier;
        this.desc = desc;
        this.code = String.format("%d",code);
    }

    public Annexure(@NonNull String identifier, @NonNull String desc, @NonNull String code) {
        this.identifier = identifier;
        this.desc = desc;
        this.code = code;
    }

}
