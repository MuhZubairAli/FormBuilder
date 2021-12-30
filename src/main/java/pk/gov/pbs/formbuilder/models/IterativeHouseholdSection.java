package pk.gov.pbs.formbuilder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;

public abstract class IterativeHouseholdSection extends HouseholdSection {
    @NotNull
    @Expose
    @Unique
    @SerializedName("INo")
    public Integer ino;

    public Integer getIterationNumber(){
        return ino;
    }

    @Override
    public SectionContext getSectionContext(){
        return new SectionContext(bId, hhno, null, null, ino);
    }

    @Override
    public void setSectionContext(SectionContext context){
        this.bId = context.getBlockIdentifier();
        this.hhno = context.getHHNo();
        this.ino = context.getIterationNumber();
    }
}
