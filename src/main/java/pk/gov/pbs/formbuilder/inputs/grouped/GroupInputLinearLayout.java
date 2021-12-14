package pk.gov.pbs.formbuilder.inputs.grouped;

import android.widget.LinearLayout;

import pk.gov.pbs.formbuilder.inputs.abstracts.input.GroupInput;
import pk.gov.pbs.formbuilder.validator.Validator;

public abstract class GroupInputLinearLayout extends GroupInput {
    protected LinearLayout inputElement;

    public GroupInputLinearLayout(String index, int resource, String... extras) {
        super(index, resource, extras);
    }

    public GroupInputLinearLayout(String index, int resource, Validator validator, String... extras) {
        super(index, resource, validator, extras);
    }

    public LinearLayout getInputView() {
        return inputElement;
    }
    protected void setInputView(LinearLayout inputElement) {
        this.inputElement = inputElement;
    }

    @Override
    public boolean hasIndex(String abIndex) {
        return getIndex().equalsIgnoreCase(abIndex);
    }
}
