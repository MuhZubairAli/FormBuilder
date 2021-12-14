package pk.gov.pbs.formbuilder.inputs.singular.adapters;

import java.util.ArrayList;
import java.util.List;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.validator.Validator;


public class SpinnerInputAdapter extends TouchInputAdapter {
    public SpinnerInputAdapter(SingularInput[] singularInputs) {
        super(R.layout.container_ll, singularInputs);
    }

    public SpinnerInputAdapter(SingularInput[] singularInputs, Validator validator) {
        super(R.layout.container_ll, singularInputs, validator);
    }

    @Override
    public ValueStore[][] getAnswers() {
        List<ValueStore[]> stores = new ArrayList<>();
        for(int i = 0; i < mSingularInputs.length; i++) {
            if(getAskables()[i].hasAnswer())
                stores.add(mSingularInputs[i].getAnswers());
        }

        if(stores.size() > 0) {
            ValueStore[][] answers = new ValueStore[stores.size()][];
            stores.toArray(answers);
            return answers;
        }
        return null;
    }
}
