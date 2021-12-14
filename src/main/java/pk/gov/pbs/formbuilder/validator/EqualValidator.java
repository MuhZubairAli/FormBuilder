package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.pojos.Jump;

public class EqualValidator extends Validator {
    private ValueStore data;

    public EqualValidator(ValueStore data){
        super();
        this.data = data;
    }

    public EqualValidator(ValueStore data, Jump[] jumpList){
        super(jumpList);
        this.data = data;
    }

    @Override
    protected boolean predicate(ValueStore valueStore) {
        return (valueStore != null) && this.data.equalsIgnoreCase(valueStore);
    }

    @Override
    public String getErrorStatement() {
        if(result != null && !result)
            return String.format("Value is not equal to %s", data.toString());
        else if(result == null)
            return RequiredValidator.NO_ANSWER_ERROR_STATEMENT;
        return null;
    }

    @Override
    public String getRuleStatement() {
        return String.format("Answer must be equal to %s", data.toString());
    }
}
