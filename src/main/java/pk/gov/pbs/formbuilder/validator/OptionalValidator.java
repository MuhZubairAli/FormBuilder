package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.pojos.Jump;

public class OptionalValidator extends Validator {
    protected Validator mValidator;

    public OptionalValidator(Validator validator){
        mValidator = validator;
    }

    public OptionalValidator(Jump[] jumps) {
        super(jumps);
        mValidator = null;
    }

    public OptionalValidator(Jump[] jumps, Validator validator) {
        super(jumps);
        mValidator = validator;
    }

    @Override
    public String getErrorStatement() {
        if(mValidator == null)
            return null;
        else {
            return mValidator.getErrorStatement();
        }
    }

    @Override
    public String getRuleStatement() {
        if(mValidator == null)
            return "This is optional field";
        else {
            return "Either this field could be blank OR " + mValidator.getRuleStatement();
        }
    }

    @Override
    protected boolean predicate(ValueStore valueStore) {
        if(valueStore == null ||  valueStore.isEmpty() || mValidator == null)
            return true;
        else {
            return mValidator.predicate(valueStore);
        }
    }

    public Validator altValidator(){
        return mValidator;
    }
}
