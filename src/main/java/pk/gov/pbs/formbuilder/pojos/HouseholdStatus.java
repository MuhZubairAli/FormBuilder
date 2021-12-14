package pk.gov.pbs.formbuilder.pojos;

public class HouseholdStatus {
    public Long aid;
    public Long sid;
    public Integer section_status;
    public Integer form_status;

    public HouseholdStatus() {}

    public HouseholdStatus(Long aid, Long sid, Integer section_status, Integer form_status) {
        this.aid = aid;
        this.sid = sid;
        this.section_status = section_status;
        this.form_status = form_status;
    }
}
