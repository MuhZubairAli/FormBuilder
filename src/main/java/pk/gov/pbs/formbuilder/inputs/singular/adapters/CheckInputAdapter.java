package pk.gov.pbs.formbuilder.inputs.singular.adapters;

import java.util.ArrayList;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.meta.ColumnCount;
import pk.gov.pbs.formbuilder.validator.Validator;

public class CheckInputAdapter extends SelectableInputAdapter {
    public CheckInputAdapter(SingularInput[] singularInputs) {
        super(singularInputs);
    }

    public CheckInputAdapter(SingularInput[] singularInputs, Validator validator) {
        super(singularInputs, validator);
    }

    public CheckInputAdapter(SingularInput[] singularInputs, ColumnCount columnCount) {
        super(singularInputs, columnCount.getValue());
    }

    public CheckInputAdapter(SingularInput[] singularInputs, ColumnCount columnCount, Validator validator) {
        super(singularInputs, columnCount.getValue(), validator);
    }

    @Override
    public ValueStore[][] getAnswers() {
        ArrayList<ValueStore[]> selection = new ArrayList<>();
        for(SingularInput ab : getAskables()) {
            if(ab.hasAnswer()){
                selection.add(ab.getAnswers());
            }
        }
        
        if(selection.size() > 0) {
            ValueStore[][] stores = new ValueStore[selection.size()][];
            stores = selection.toArray(stores);
            return stores;
        }

        return null;
    }
}
