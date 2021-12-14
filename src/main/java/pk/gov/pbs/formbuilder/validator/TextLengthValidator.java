package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;

public class TextLengthValidator extends Validator {
    @Override
    protected boolean predicate(ValueStore valueStore) {
        //&& Pattern.matches("^([a-zA-Z]{2,16}[ ]?[\\.]?[ ]?)+$", valueStore.toString()) && valueStore.toString().length() > 2;
        return (valueStore != null) && !valueStore.isEmpty() && valueStore.toString().length() > 2;
    }

    @Override
    public String getErrorStatement() {
        if(result != null && !result)
            return "Entered text either contains some invalid characters or multiple spaces";
        else if(result == null)
            return RequiredValidator.NO_ANSWER_ERROR_STATEMENT;
        return null;
    }

    @Override
    public String getRuleStatement() {
        return "Entered text must consist of alphabets and spaces, Any non-alphabetical character (except full stop) or more than one spaces together in entered text is not allowed";
    }
}
