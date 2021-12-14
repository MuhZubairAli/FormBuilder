package pk.gov.pbs.formbuilder.validator;


import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.pojos.Jump;

public class LimitLengthValidator extends Validator {
    int min, max;

    public LimitLengthValidator(int maxLength){
        setLimits(0,maxLength);
    }
    public LimitLengthValidator(int maxLength, Jump[] jumps){
        super(jumps);
        setLimits(0, maxLength);
    }

    public LimitLengthValidator(int minLength, int maxLength){
        setLimits(minLength, maxLength);
    }

    public LimitLengthValidator(int minLength, int maxLength, Jump[] jumps){
        super(jumps);
        setLimits(minLength, maxLength);
    }

    private void setLimits(int minLength, int maxLength){
        if(minLength > maxLength)
            throw new IllegalArgumentException("LimitLengthValidator]: Max limit must be greater than min limit, Provided min = " + minLength + " | max = " + maxLength);

        if(minLength > -1)
            min = minLength;
        else
            throw new IllegalArgumentException("LimitLengthValidator]: Min limit must be greater than -1, Provided = " + minLength);

        if(maxLength > 0)
            max = maxLength;
        else
            throw new IllegalArgumentException("LimitLengthValidator]: Max limit must be greater than 0");
    }

    @Override
    protected boolean predicate(ValueStore valueStore) {
        return (valueStore != null) && (valueStore.toString().length() >= min && valueStore.toString().length() <= max);
    }

    @Override
    public String getErrorStatement() {
        if(result != null && !result)
            return "Number of characters in given answer exceed the limit of ("+min+"-"+max+") inclusively";
        else if(result == null)
            return RequiredValidator.NO_ANSWER_ERROR_STATEMENT;
        return null;
    }

    @Override
    public String getRuleStatement() {
        return "The number of characters must be be in the limit of ("+min+"-"+max+") inclusively";
    }
}
