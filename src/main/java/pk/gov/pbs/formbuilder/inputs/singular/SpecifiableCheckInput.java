package pk.gov.pbs.formbuilder.inputs.singular;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.pojos.DatumIdentifier;
import pk.gov.pbs.utils.ThemeUtils;

public class SpecifiableCheckInput extends SpecifiableSelectable {
    protected CheckBox inputElement;

    public SpecifiableCheckInput(String index, DatumIdentifier identifier) {
        super(index, identifier, R.layout.input_cb);
    }

    public SpecifiableCheckInput(String index, DatumIdentifier identifier, int resId) {
        super(index, identifier, resId);
    }

    @Override
    public CheckBox getInputView() {
        return inputElement;
    }

    public void setInputElement(CheckBox inputElement) {
        this.inputElement = inputElement;
    }

    public boolean setupLoadedAnswer(String desc) {
        if(inputElement != null){
            inputElement.setChecked(true);
            Spanned html = Html.fromHtml(desc);
            inputElement.setText(html);
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        if(getInputView() != null && getAnswers() != null){
            setAnswers(null, null);
            if(getInputView().isChecked())
                getInputView().setChecked(false);

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
        CheckBox item;
        if(getInputView() == null) {
            item = (CheckBox) inflater.inflate(getResId(), parent, false);
            ThemeUtils.setupTextViewStylesByLocale(labels.getLocale(), item);
            String label = inflater
                    .getContext()
                    .getString(
                            R.string.specifiable_selectable_label_template
                            , appendValueToLabel(labels.getLabel(getIndex()))
                            , inflater.getContext().getString(R.string.s_s_l_2nd_param_placeholder)
                    );
            Spanned text = Html.fromHtml(label);
            item.setText(text);
            setInputElement(item);
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }
}
