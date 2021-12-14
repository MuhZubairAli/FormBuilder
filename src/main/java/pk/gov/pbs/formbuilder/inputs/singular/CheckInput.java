package pk.gov.pbs.formbuilder.inputs.singular;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;

public class CheckInput extends Selectable {
    protected CheckBox inputElement;

    private CheckInput(String index) {
        super(index, R.layout.input_cb);
        mAnswers = new ValueStore[1];
    }

    public CheckInput(String index, ValueStore value) {
        this(index);
        this.value = value;
    }

    private CheckInput(String index, int resId) {
        super(index, resId);
    }

    public CheckInput(String index, ValueStore value, int resId) {
        super(index, resId);
        this.value = value;
    }

    @Override
    public CheckBox getInputView() {
        return inputElement;
    }

    @Override
    public void reset() {
        if(getInputView() != null && getAnswers() != null){
            setAnswer(null);
            if(getInputView().isChecked())
                getInputView().setChecked(false);
            if (!isVisible())
                quickShow();
        }
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        model.set(getIndex(), getAnswer());
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if (model.get(getIndex()) != null) {
            setAnswer(model.get(getIndex()));
            return true;
        }
        return false;
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        CheckBox item;
        if(getInputView() == null) {
            item = (CheckBox) inflater.inflate(getResId(), parent, false);
            Spanned text = Html.fromHtml(getValue().toString() + ". " + labels.getLabel(getIndex()));
            item.setText(text);
            inputElement = item;
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }
}
