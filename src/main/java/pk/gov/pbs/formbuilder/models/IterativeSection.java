package pk.gov.pbs.formbuilder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;

public abstract class IterativeSection extends HouseholdSection {
    @NotNull
    @Expose
    @Unique
    @SerializedName("INo")
    public Integer ino;

    public Integer getIterationNumber(){
        return ino;
    }

    @Override
    public SectionContext getFormContext(){
        return new SectionContext(BId, hhno, null, null, ino);
    }

    @Override
    public void setFormContext(SectionContext context){
        this.BId = context.getBlockIdentifier();
        this.hhno = context.getHHNo();
        this.ino = context.getIterationNumber();
    }
}
