package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.pojos.Jump;

public class RequiredValidator extends Validator {
    public static final String NO_ANSWER_ERROR_STATEMENT = "Answer to the question is not provided or provided answer is invalid";
    public RequiredValidator() {
        super();
    }

    public RequiredValidator(Jump[] jumps) {
        super(jumps);
    }

    @Override
    public String getErrorStatement() {
        return RequiredValidator.NO_ANSWER_ERROR_STATEMENT;
    }

    @Override
    public String getRuleStatement() {
        return "The question must have any valid answer";
    }

    @Override
    protected boolean predicate(ValueStore valueStore) {
        return (valueStore != null && !(valueStore.isEmpty()));
    }
}
