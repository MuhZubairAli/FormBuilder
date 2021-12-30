package pk.gov.pbs.formbuilder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;

public abstract class HouseholdSection extends Section {
    @NotNull
    @Expose
    @Unique
    @SerializedName("HHNo")
    public Integer hhno;

    public SectionContext getSectionContext(){
        return new SectionContext(bId, hhno, null, null, null);
    }

    public void setSectionContext(SectionContext context){
        this.bId = context.getBlockIdentifier();
        this.hhno = context.getHHNo();
    }
}
