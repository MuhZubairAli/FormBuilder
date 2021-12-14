package pk.gov.pbs.formbuilder.inputs.grouped;

import android.widget.TableRow;

import pk.gov.pbs.formbuilder.inputs.abstracts.input.GroupInput;
import pk.gov.pbs.formbuilder.validator.Validator;

public abstract class GroupInputTableRow extends GroupInput {
    protected TableRow inputElement;

    public GroupInputTableRow(String index, int resId, String... extras) {
        super(index, resId, extras);
    }

    public GroupInputTableRow(String index, int resId, Validator validator, String... extras) {
        super(index, resId, validator, extras);
    }

    public TableRow getInputView() {
        return inputElement;
    }

    public void setInputView(TableRow inputElement) {
        this.inputElement = inputElement;
    }

    @Override
    public boolean hasIndex(String abIndex) {
        return getIndex().equalsIgnoreCase(abIndex);
    }
}
