package pk.gov.pbs.formbuilder.inputs.singular;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.utils.ThemeUtils;

public class ButtonInput extends SingularInput {
    private final View.OnClickListener handler;
    private Button inputElement;

    public ButtonInput(String index, View.OnClickListener callback) {
        super(index, R.layout.input_btn);
        this.handler = callback;
    }

    public ButtonInput(String index, int resId, View.OnClickListener callback){
        super(index, resId);
        this.handler = callback;
    }

    public Button getInputView() {
        return inputElement;
    }

    @Override
    public boolean requestFocus() {
        if (getInputView() != null)
            return getInputView().requestFocus();
        return false;
    }

    @Override
    public boolean hasFocus() {
        if (getInputView() != null)
            return getInputView().hasFocus();
        return false;
    }

    @Override
    public boolean lock() {
        if (!IS_UNLOCKED)
            return true;

        if (inputElement != null) {
            inputElement.setEnabled(false);
            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if (IS_UNLOCKED)
            return true;

        if (inputElement != null) {
            inputElement.setEnabled(true);
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean hasAnswer() {
        return false;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {

    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        return false;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        return false;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {}

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        if(getInputView() == null) {
            inputElement = (Button) inflater.inflate(getResId(), parent, false);
            ThemeUtils.setupTextViewStylesByLocale(labels.getLocale(), inputElement);
            Spanned text = Html.fromHtml(labels.getLabel(getIndex()));
            inputElement.setText(text);
            inputElement.setOnClickListener(handler);
        } else {
            ((ViewGroup) getInputView().getParent()).removeView(getInputView());
        }
        parent.addView(getInputView());
    }
}
