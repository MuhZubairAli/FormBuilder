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
    public FormContext getFormContext(){
        return new FormContext(pcode, hhno, null, sno, null);
    }

    @Override
    public void setFormContext(FormContext context){
        this.pcode = context.getPCode();
        this.hhno = context.getHHNo();
        this.sno = context.getMemberID();
    }
}
