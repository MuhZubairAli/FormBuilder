package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;

public class AnnexValidator extends Validator {

    int lengthLimit;
    int minLengthLimit, maxLengthLimit;

    public AnnexValidator(int numOfDigits) {
        lengthLimit = numOfDigits;
        minLengthLimit = maxLengthLimit = 0;
    }

    public AnnexValidator(int min, int max){
        setLimits(min, max);
        lengthLimit = 0;
    }

    private void setLimits(int minLength, int maxLength){
        if(minLength > maxLength)
            throw new IllegalArgumentException("LimitLengthValidator]: Max limit must be greater than min limit, Provided min = " + minLength + " | max = " + maxLength);

        if(minLength > -1)
            minLengthLimit = minLength;
        else
            throw new IllegalArgumentException("LimitLengthValidator]: Min limit must be greater than -1, Provided = " + minLength);

        if(maxLength > 0)
            maxLengthLimit = maxLength;
        else
            throw new IllegalArgumentException("LimitLengthValidator]: Max limit must be greater than 0");
    }

    @Override
    protected boolean predicate(ValueStore valueStore) {
        if(lengthLimit > 0)
            return (valueStore != null) && valueStore.toString().length() == lengthLimit;
        else
            return (valueStore != null) && (minLengthLimit <= valueStore.toString().length() && maxLengthLimit >= valueStore.toString().length());
    }

    @Override
    public String getErrorStatement() {
        if(lengthLimit > 0 && result != null && !result)
            return "Invalid number of digits in annex code";
        else if((minLengthLimit > 0 && maxLengthLimit > 0) && result != null && !result)
            return "Number of digits in annex code are not within specified range as [" + minLengthLimit + " to " + maxLengthLimit + "]";
        else if(result == null)
            return RequiredValidator.NO_ANSWER_ERROR_STATEMENT;
        return null;
    }

    @Override
    public String getRuleStatement() {
        if(lengthLimit > 0)
            return "Number of digits must be equal to " + lengthLimit;
        else
            return "Number of digits in annex code must be in the range of " + minLengthLimit + " to " + maxLengthLimit;
    }
}
