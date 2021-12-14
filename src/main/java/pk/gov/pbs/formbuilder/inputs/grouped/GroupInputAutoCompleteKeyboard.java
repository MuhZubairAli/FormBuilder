package pk.gov.pbs.formbuilder.inputs.grouped;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.inputs.singular.AutoCompleteKeyboardInput;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.validator.Validator;

public class GroupInputAutoCompleteKeyboard extends GroupInputLinearLayout {
    private final AutoCompleteKeyboardInput mKeyboardInput;

    public GroupInputAutoCompleteKeyboard(String index, AutoCompleteKeyboardInput keyboardInput, Validator validator) {
        super(index, R.layout.input_group_tv_actv);
        mAnswers = keyboardInput.getAnswers();
        this.mKeyboardInput = keyboardInput;
        this.mValidator = validator;
    }

    @Override
    public boolean validateAnswer() {
        if(canSkipValidate())
            return true;

        if (hasAnswer())
            return getValidator().isValid(mKeyboardInput.getAnswer());

        return false;
    }

    @Override
    public boolean hasAnswer() {
        return mKeyboardInput.hasAnswer();
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        mKeyboardInput.exportAnswer(model);
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        return mKeyboardInput.importAnswer(model);
    }

    @Override
    public boolean lock() {
        return mKeyboardInput.lock();
    }

    @Override
    public boolean unlock() {
        return mKeyboardInput.unlock();
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        return mKeyboardInput.loadAnswerIntoInputView(viewModel);
    }

    @Override
    public void bindListeners(ActivityFormSection context, @Nullable Runnable onAnswerEventLambda) {
        mKeyboardInput.bindListeners(context, onAnswerEventLambda);
    }

    @Override
    public void setupImeAction(NavigationToolkit nToolkit) {
        mKeyboardInput.setupImeAction(nToolkit);
    }

    @Override
    public void reset() {
        mKeyboardInput.reset();
    }

    @Override
    public boolean requestFocus() {
        return mKeyboardInput.requestFocus();
    }

    @Override
    public boolean hasFocus(){
        return mKeyboardInput.hasFocus();
    }

    @Override
    public boolean hasIndex(String abIndex) {
        return getIndex().equalsIgnoreCase(abIndex) || mKeyboardInput.getIndex().equalsIgnoreCase(abIndex);
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        LinearLayout item;
        if(getInputView() == null) {
            item = (LinearLayout) inflater.inflate(getResId(), parent, false);

            Spanned htm = Html.fromHtml(labels.getLabel(getIndex()));
            ((TextView) item.findViewById(R.id.tv)).setText(htm);

            LinearLayout container = item.findViewById(R.id.container_kbi);
            mKeyboardInput.inflate(inflater, labels, container);

            setInputView(item);
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }
}
