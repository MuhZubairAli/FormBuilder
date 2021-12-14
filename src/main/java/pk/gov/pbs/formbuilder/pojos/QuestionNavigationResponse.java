package pk.gov.pbs.formbuilder.pojos;

import pk.gov.pbs.formbuilder.utils.ValueStore;

public class QuestionNavigationResponse {
    private int sCode; //Status code
    private ValueStore dCode; //Data code / Error code

    public QuestionNavigationResponse(int status, ValueStore dataCode) {
        this.sCode = status;
        this.dCode = dataCode;
    }

    public int getStatusCode() {
        return sCode;
    }

    public ValueStore getDataCode() {
        return dCode;
    }
}
