package pk.gov.pbs.formbuilder.validator;

import java.util.regex.Pattern;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.pojos.Jump;

public class NaturalNumberValidator extends Validator {
    int max,min;
    public NaturalNumberValidator() {
        super();
        max = min = Constants.INVALID_NUMBER;
    }

    public NaturalNumberValidator(Jump[] jumps) {
        super(jumps);
        max = min = Constants.INVALID_NUMBER;
    }

    public NaturalNumberValidator(int max){
        super();
        this.max = max;
        this.min = Constants.INVALID_NUMBER;
    }
    
    public NaturalNumberValidator(int min, int max){
        super();
        this.max = max;
        this.min = min;
    }
    
    public NaturalNumberValidator(int max, Jump[] jumps){
        super(jumps);
        this.max = max;
        this.min = Constants.INVALID_NUMBER;
    }
    
    public NaturalNumberValidator(int min, int max, Jump[] jumps){
        super(jumps);
        this.max = max;
        this.min = min;
    }

    @Override
    protected boolean predicate(ValueStore valueStore) {
        boolean c1,c2,c3;
        c1 = c2 = c3 = false;
        c1 = (valueStore != null) && Pattern.matches("^[0-9]+$", valueStore.toString());
        if(c1) {
            c2 = (max == Constants.INVALID_NUMBER) || valueStore.toInt() <= max;
            c3 = (min == Constants.INVALID_NUMBER) || valueStore.toInt() >= min;
        }
        return c1 && c2 && c3;
    }

    @Override
    public String getErrorStatement() {
        if(result != null && !result) {
            String error = "Provided answer is not a valid Positive Number";
            if(min != Constants.INVALID_NUMBER && max != Constants.INVALID_NUMBER)
                error = error.concat(", and it is not between " + min + " and " + max + " inclusively.");
            else if(max != Constants.INVALID_NUMBER)
                error = error.concat(", and it is not less than " + max);
            else 
                error = error + ".";
            return error;
        } else if(result == null)
            return RequiredValidator.NO_ANSWER_ERROR_STATEMENT;
        return null;
    }

    @Override
    public String getRuleStatement() {
        String rules = "Given answer must be of type Number, Make sure given input is a Natural Number i.e positive and not a not a fraction";
        if(min != Constants.INVALID_NUMBER && max != Constants.INVALID_NUMBER)
            rules = rules.concat(". And it must be between " + min + " and " + max + " inclusively.");
        else if(max != Constants.INVALID_NUMBER)
            rules = rules.concat(". And it must be less than " + max);
        else
            rules = rules + ".";
        return rules;
    }
}
