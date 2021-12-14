package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.pojos.Jump;

public class BetweenValidator extends Validator {
    private final ValueStore min;
    private final ValueStore max;

    public BetweenValidator(ValueStore min, ValueStore max) {
        this.min = min;
        this.max = max;
    }

    public BetweenValidator(ValueStore min, ValueStore max, Jump[] jumps) {
        super(jumps);
        this.min = min;
        this.max = max;
    }

    public ValueStore getMutableMax() {
        return max;
    }

    public ValueStore getMutableMin() {
        return min;
    }

    @Override
    protected boolean predicate(ValueStore valueStore) {
        Double value = (valueStore != null) ? valueStore.tryCastToDouble() : null;
        return (value != null)  && (min.toDouble() <= value && max.toDouble() >= value);
    }

    @Override
    public String getErrorStatement() {
        if(result != null && !result)
            return String.format("Given answer is not between %d and %d",min.toLong(),max.toLong());
        else if(result == null)
            return RequiredValidator.NO_ANSWER_ERROR_STATEMENT;
        return null;
    }

    @Override
    public String getRuleStatement() {
        return String.format("The answer must be between %s and %s inclusively",min.toString(),max.toString());
    }
}
