package pk.gov.pbs.formbuilder.inputs.singular.adapters;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.meta.ColumnCount;
import pk.gov.pbs.formbuilder.validator.Validator;

public class RadioInputAdapter extends SelectableInputAdapter {
    public RadioInputAdapter(SingularInput[] singularInputs) {
        super(singularInputs);
    }

    public RadioInputAdapter(SingularInput[] singularInputs, Validator validator) {
        super(singularInputs, validator);
    }

    public RadioInputAdapter(SingularInput[] singularInputs, ColumnCount columnCount, Validator validator) {
        super(singularInputs, columnCount.getValue(), validator);
    }

    @Override
    public ValueStore[][] getAnswers() {
        ValueStore[][] stores = new ValueStore[1][];
        for(int i = 0; i < getAskables().length; i++) {
            if(getAskables()[i].hasAnswer()) {
                stores[0] = getAskables()[i].getAnswers();
                return stores;
            }
        }
        return null;
    }
}
