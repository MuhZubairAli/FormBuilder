package pk.gov.pbs.formbuilder.inputs.singular;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.pojos.DatumIdentifier;

public class SpecifiableRadioInput extends SpecifiableSelectable {
    private RadioButton inputElement;

    public SpecifiableRadioInput(String index, DatumIdentifier identifier) {
        super(index, identifier, R.layout.input_rb);
    }

    public SpecifiableRadioInput(String index, DatumIdentifier identifier, int resId) {
        super(index, identifier, resId);
    }

    @Override
    public RadioButton getInputView() {
        return inputElement;
    }

    @Override
    public void reset() {
        if(getInputView() != null && hasAnswer()){
            setAnswers(null, null);
            if(getInputView().isChecked())
                ((RadioGroup) getInputView().getParent()).clearCheck();

            if (!isVisible())
                quickShow();
        }
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        model.set(getIndex(), getAnswer(0));
        model.set("__"+getIndex(), getAnswer(1));
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if (model.get(getIndex()) != null) {
            setAnswers(model.get(getIndex()), model.get("__" + getIndex()));
            return true;
        }
        return false;
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        RadioButton item;
        if(getInputView() == null) {
            item =(RadioButton) inflater.inflate(getResId(), parent, false);
            String label = inflater
                    .getContext()
                    .getString(
                            R.string.specifiable_selectable_label_template
                            , appendValueToLabel(labels.getLabel(getIndex()))
                            , inflater.getContext().getString(R.string.s_s_l_2nd_param_placeholder)
                    );
            Spanned text = Html.fromHtml(label);
            item.setText(text);
            inputElement = item;
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }
}
