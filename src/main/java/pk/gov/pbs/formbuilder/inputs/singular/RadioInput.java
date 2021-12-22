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
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.utils.ThemeUtils;

public class RadioInput extends Selectable {
    private RadioButton inputElement;

    private RadioInput(String index){
        super(index, R.layout.input_rb);
        mAnswers = new ValueStore[1];
    }

    public RadioInput(String index, ValueStore value){
        this(index);
        this.value = value;
    }

    public RadioButton getInputView() {
        return inputElement;
    }

    @Override
    public void reset() {
        if(getInputView() != null){
            if(getInputView().isChecked())
                ((RadioGroup) getInputView().getParent()).clearCheck();

            if (!isVisible())
                quickShow();
        }
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        RadioButton item;
        if(getInputView() == null) {
            item =(RadioButton) inflater.inflate(getResId(), parent, false);
            ThemeUtils.setupTextViewStylesByLocale(labels.getLocale(), item);
            Spanned text = Html.fromHtml(getValue().toString() + ". " + labels.getLabel(getIndex()));
            item.setText(text);
            inputElement = item;
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        String qIndex = getIndex().substring(0,getIndex().lastIndexOf('_'));
        if (hasAnswer()) model.set(qIndex, getAnswer());
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        String qIndex = getIndex().substring(0,getIndex().lastIndexOf('_'));
        if (model.get(qIndex) != null && model.get(qIndex).equalsIgnoreCase(getValue())){
            mAnswers[0] = model.get(qIndex);
            return true;
        }
        return false;
    }
}
