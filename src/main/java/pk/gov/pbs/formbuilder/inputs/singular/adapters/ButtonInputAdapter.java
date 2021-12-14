package pk.gov.pbs.formbuilder.inputs.singular.adapters;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.inputs.abstracts.adapters.SingularInputAdapter;

//Action Button Container
public class ButtonInputAdapter extends SingularInputAdapter {

    public ButtonInputAdapter(SingularInput[] singularInputs) {
        super(R.layout.container_ll, singularInputs);
    }

    @Override
    public ValueStore[][] getAnswers() {
        return null;
    }
}
