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
    @SerializedName("BIdentifier")
    public String bId; // Block Identifier | this is either block code or processing code

    @NotNull
    @Expose
    @SqlExclude
    @SerializedName("SStatus")
    public Integer section_status; // 1=opened, 2=closed

    public SectionContext getSectionContext(){
        return new SectionContext(bId, null, null, null, null);
    }

    public void setSectionContext(SectionContext context){
        this.bId = context.getBlockIdentifier();
    }
}
