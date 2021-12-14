package pk.gov.pbs.formbuilder.pojos;

import com.google.gson.annotations.Expose;

public class Annex {
    @Expose
    public String code;
    @Expose
    public String desc;

    public Annex() {}

    public Annex(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
