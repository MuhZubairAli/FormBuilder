package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;

public class AddressValidator extends Validator {
    @Override
    protected boolean predicate(ValueStore valueStore) {
        //Pattern.matches("^([a-zA-Z0-9 ]*[\\#\\,\\.\\-\\/]?[a-zA-Z1-9 ]+)+$", valueStore.toString());
        return (valueStore != null) && !valueStore.isEmpty() && valueStore.toString().length() > 2;
    }

    @Override
    public String getErrorStatement() {
    if(result != null && !result)
            return "Entered address is either in invalid format or contains some invalid characters";
    else if(result == null)
        return RequiredValidator.NO_ANSWER_ERROR_STATEMENT;
        return null;
    }

    @Override
    public String getRuleStatement() {
        return "Address must consist of Alphabets, Numbers and following special characters may also be used (# , . - /) and do not repeat special characters consecutively";
    }
}
