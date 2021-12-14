package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;

public class PhoneNumberValidator extends Validator implements ExceptionalInputLength {
    private final static int ptcl_digits = 10; // 051 9106563
    private final static int mobile_digits = 11; // 0342 9597436
    private final static int intl_mobile_digits =  14; // 0092 342 9597436

    @Override
    protected boolean predicate(ValueStore valueStore) {
        return (valueStore != null) && (ptcl_digits == valueStore.toString().length() || mobile_digits == valueStore.toString().length() || intl_mobile_digits == valueStore.toString().length());
    }

    @Override
    public String getErrorStatement() {
        if(result != null && !result)
            return "Phone number does not consist of valid number of digits as "+ ptcl_digits +" (for PTCL Number), "+ mobile_digits +" (for Mobile Number) or " + intl_mobile_digits + " (for International Number)";
        else if(result == null)
            return RequiredValidator.NO_ANSWER_ERROR_STATEMENT;
        return null;
    }

    @Override
    public String getRuleStatement() {
        return "Number of digits must be "+ ptcl_digits +", "+ mobile_digits +" or " + intl_mobile_digits + " for Landline, Mobile or International formats respectively";
    }

    @Override
    public int getMaxLength() {
        return intl_mobile_digits;
    }
}
