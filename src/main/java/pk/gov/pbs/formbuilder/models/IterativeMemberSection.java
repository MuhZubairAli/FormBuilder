package pk.gov.pbs.formbuilder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;

public abstract class IterativeMemberSection extends MemberSection{
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
        return new SectionContext(BId, hhno, null, sno, ino);
    }

    @Override
    public void setFormContext(SectionContext context){
        this.BId = context.getBlockIdentifier();
        this.hhno = context.getHHNo();
        this.sno = context.getMemberID();
        this.ino = context.getIterationNumber();
    }
}
