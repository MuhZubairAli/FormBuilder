package pk.gov.pbs.formbuilder.inputs.singular.adapters;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.inputs.abstracts.adapters.SingularInputAdapter;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.validator.Validator;

public class DateInputAdapter extends SingularInputAdapter {
    public DateInputAdapter(SingularInput[] singularInputs) {
        super(R.layout.container_ll, singularInputs);
    }

    public DateInputAdapter(SingularInput[] singularInputs, Validator validator) {
        super(R.layout.container_ll, singularInputs, validator);
    }

    @Override
    public ValueStore[][] getAnswers() {
        ValueStore[][] answers = new ValueStore[mSingularInputs.length][3];
        for (int i=0; i < mSingularInputs.length; i++)
            answers[i] = mSingularInputs[i].getAnswers();
        return answers;
    }
}
