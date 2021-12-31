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

    @Override
    public SectionContext getFormContext(){
        return new SectionContext(BId, hhno, null, null, null);
    }

    @Override
    public void setFormContext(SectionContext context){
        this.BId = context.getBlockIdentifier();
        this.hhno = context.getHHNo();
    }
}
