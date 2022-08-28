package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;

public class EmailValidator extends Validator{
    @Override
    protected boolean predicate(ValueStore valueStore) {
        return org.apache.commons.validator.routines.EmailValidator.getInstance().isValid(valueStore.toString());
    }

    @Override
    public String getErrorStatement() {
        return "Enter valid email address";
    }

    @Override
    public String getRuleStatement() {
        return "Invalid email address";
    }
}
