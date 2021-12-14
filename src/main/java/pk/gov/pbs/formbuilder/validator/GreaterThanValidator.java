package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.pojos.Jump;

public class GreaterThanValidator extends Validator {
    private final ValueStore data;

    public GreaterThanValidator(ValueStore data) {
        super();
        this.data = data;
    }

    public GreaterThanValidator(ValueStore data, Jump[] jumps) {
        super(jumps);
        this.data = data;
    }

    @Override
    public String getErrorStatement() {
        if(result != null && !result)
            return String.format("Value / Selection is not greater than %s", data.toString());
        else if(result == null)
            return RequiredValidator.NO_ANSWER_ERROR_STATEMENT;
        return null;
    }

    @Override
    public String getRuleStatement() {
        return String.format("Answer must be greater than %s", data.toString());
    }

    @Override
    protected boolean predicate(ValueStore valueStore) {
        return (valueStore != null) && (valueStore.toInt() > this.data.toInt());
    }
}
