package pk.gov.pbs.formbuilder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.SqlExclude;
import pk.gov.pbs.database.annotations.Unique;

public class Section extends Table {
    @NotNull
    @Expose
    @Unique
    @SerializedName("PCode")
    public String pcode;

    @NotNull
    @Expose
    @SqlExclude
    @SerializedName("SStatus")
    public Integer section_status; // 1=opened, 2=closed

    public FormContext getFormContext(){
        return new FormContext(pcode, null, null, null, null);
    }

    public void setFormContext(FormContext context){
        this.pcode = context.getPCode();
    }
}
