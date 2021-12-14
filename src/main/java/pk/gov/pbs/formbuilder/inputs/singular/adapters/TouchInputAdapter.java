package pk.gov.pbs.formbuilder.inputs.singular.adapters;

import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.inputs.abstracts.adapters.SingularInputAdapter;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.validator.Validator;

public abstract class TouchInputAdapter extends SingularInputAdapter {
    public TouchInputAdapter(int containerResId, SingularInput[] singularInputs) {
        super(containerResId, singularInputs);
    }

    public TouchInputAdapter(int containerResId, SingularInput[] singularInputs, Validator validator) {
        super(containerResId, singularInputs, validator);
    }
}
