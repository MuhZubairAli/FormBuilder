package pk.gov.pbs.formbuilder.inputs.singular;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.pojos.DatumIdentifier;

public class CheckKeyboardInput extends SpecifiableSelectable{
    private int inputType;
    public CheckKeyboardInput(String index, DatumIdentifier identifier, int inputType) {
        super(index, identifier, R.layout.input_cb);
        this.inputType = inputType;
    }

    @Override
    public CompoundButton getInputView() {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {

    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        return false;
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {

    }
}
