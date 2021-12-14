package pk.gov.pbs.formbuilder.pojos;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OptionTupleWeb {
    @Expose
    @SerializedName("APID")
    public Long aid;

    @NonNull
    @Expose
    @SerializedName("SID")
    public Long sid;

    @NonNull
    @Expose
    @SerializedName("S")
    public Integer s;

    @NonNull
    @Expose
    @SerializedName("C")
    public String c;

    @NonNull
    @Expose
    @SerializedName("V")
    public Integer v;

}
