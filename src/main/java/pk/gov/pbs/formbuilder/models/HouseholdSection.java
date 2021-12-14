package pk.gov.pbs.formbuilder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.SqlExclude;
import pk.gov.pbs.database.annotations.Unique;
import pk.gov.pbs.utils.ExceptionReporter;

public abstract class HouseholdSection extends Section {
    @NotNull
    @Expose
    @Unique
    @SerializedName("HHNo")
    public Integer hhno;

    @Override
    public FormContext getFormContext(){
        return new FormContext(pcode, hhno, null, null, null);
    }

    @Override
    public void setFormContext(FormContext context){
        this.pcode = context.getPCode();
        this.hhno = context.getHHNo();
    }
}
