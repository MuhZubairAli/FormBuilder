package pk.gov.pbs.formbuilder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.SqlExclude;
import pk.gov.pbs.database.annotations.Unique;

public abstract class Section extends Table {
    @NotNull
    @Expose
    @Unique
    @SerializedName("BId")
    public String BId;

    @NotNull
    @Expose
    @SqlExclude
    @SerializedName("SStatus")
    public Integer section_status; // 1=opened, 2=closed

    public SectionContext getFormContext(){
        return new SectionContext(BId, null, null, null, null);
    }

    public void setFormContext(SectionContext context){
        this.BId = context.getBlockIdentifier();
    }
}
