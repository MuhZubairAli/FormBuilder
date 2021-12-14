package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.pojos.Jump;

public class LessThanValidator extends Validator {
    private final ValueStore data;

    public LessThanValidator(ValueStore data) {
        super();
        this.data = data;
    }

    public LessThanValidator(ValueStore data, Jump[] jumpList) {
        super(jumpList);
        this.data = data;
    }

    public ValueStore getMutableLimit(){
        return data;
    }

    @Override
    public String getErrorStatement() {
        if(result != null && !result)
            return String.format("Value is not less than %s", data.toString());
        else if(result == null)
            return RequiredValidator.NO_ANSWER_ERROR_STATEMENT;
        return null;
    }

    @Override
    public String getRuleStatement() {
        return String.format("Answer must be less than %s", data.toString());
    }

    @Override
    protected boolean predicate(ValueStore valueStore) {
        return (valueStore != null) && (data.toDouble() > valueStore.toDouble());
    }
}
