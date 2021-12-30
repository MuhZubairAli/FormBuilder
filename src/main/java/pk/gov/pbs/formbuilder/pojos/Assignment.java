package pk.gov.pbs.formbuilder.pojos;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;

import pk.gov.pbs.database.annotations.NotNull;
import pk.gov.pbs.database.annotations.Unique;
import pk.gov.pbs.formbuilder.models.Table;

public abstract class Assignment extends Table {
    @NotNull
    @Expose
    @Unique
    public String PCode;

    @NotNull
    @Expose
    @Unique
    public String EBCode;

    @Nullable
    @Expose
    public Integer Quarter;

    @NotNull
    @Expose
    @Unique
    public Integer HHNo;

    @Nullable
    @Expose
    public Double Longitude;

    @Nullable
    @Expose
    public Double Latitude;

    @Nullable
    @Expose
    @Unique
    public String Assignee;

    @Nullable
    @Expose
    public String Assigner;

    @Nullable
    @Expose
    public String DBegin; //Date Begin

    @Nullable
    @Expose
    public String DEnd; //Date End

    @Nullable
    @Expose
    public String DAssigned; // Date assigned

    public Assignment(){
    }

    public Assignment(String PCode, String EBCode, @Nullable Integer quarter, Integer HHNo, @Nullable Double longitude, @Nullable Double latitude, @Nullable String assignee, @Nullable String assigner, @Nullable String DBegin, @Nullable String DEnd, @Nullable String DAssigned) {
        this.PCode = PCode;
        this.EBCode = EBCode;
        Quarter = quarter;
        this.HHNo = HHNo;
        Longitude = longitude;
        Latitude = latitude;
        Assignee = assignee;
        Assigner = assigner;
        this.DBegin = DBegin;
        this.DEnd = DEnd;
        this.DAssigned = DAssigned;
    }

    @Nullable
    public String getPCode() {
        return PCode;
    }

    @Nullable
    public String getEBCode() {
        return EBCode;
    }

    @Nullable
    public Integer getQuarter() {
        return Quarter;
    }

    @Nullable
    public Integer getHHNo() {
        return HHNo;
    }

    @Nullable
    public Double getLongitude() {
        return Longitude;
    }

    @Nullable
    public Double getLatitude() {
        return Latitude;
    }

    @Nullable
    public String getAssignee() {
        return Assignee;
    }

    @Nullable
    public String getAssigner() {
        return Assigner;
    }

    @Nullable
    public String getDateBegin() {
        return DBegin;
    }

    @Nullable
    public String getDateEnd() {
        return DEnd;
    }

    @Nullable
    public String getDateAssigned() {
        return DAssigned;
    }

    public void setHHNo(int hhNo){
        HHNo = hhNo;
    }
}
