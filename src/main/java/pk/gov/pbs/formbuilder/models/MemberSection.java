package pk.gov.pbs.formbuilder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;

public abstract class MemberSection extends HouseholdSection {
    @NotNull
    @Expose
    @Unique
    @SerializedName("SNo")
    public Integer sno;

    public Integer getMemberId(){
        return sno;
    }

    @Override
    public SectionContext getSectionContext(){
        return new SectionContext(bId, hhno, null, sno, null);
    }

    @Override
    public void setSectionContext(SectionContext context){
        this.bId = context.getBlockIdentifier();
        this.hhno = context.getHHNo();
        this.sno = context.getMemberID();
    }
}
