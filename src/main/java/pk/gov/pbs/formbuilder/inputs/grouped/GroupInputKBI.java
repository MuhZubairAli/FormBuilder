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
import pk.gov.pbs.formbuilder.inputs.singular.KeyboardInput;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.validator.Validator;
import pk.gov.pbs.utils.ThemeUtils;

public class GroupInputKBI extends GroupInputLinearLayout {
    private final KeyboardInput mKeyboardInput;

    private GroupInputKBI(String index, String... extras) {
        super(index, R.layout.input_group_tv_kbi, extras);
        mAnswers = new ValueStore[1];
        mKeyboardInput = new KeyboardInput(index);
    }

    public GroupInputKBI(String index, int inputType, String... extras) {
        this(index, extras);
        mKeyboardInput.setInputType(inputType);
    }

    public GroupInputKBI(String index, Validator validator, String... extras) {
        this(index, extras);
        mValidator = validator;
        mKeyboardInput.setValidator(validator);
    }

    public GroupInputKBI(String index, int inputType, Validator validator, String... extras) {
        this(index, inputType, extras);
        mValidator = validator;
        mKeyboardInput.setValidator(validator);
    }

    @Override
    public boolean validateAnswer() {
        if(canSkipValidate())
            return true;

        if (hasAnswer())
            return mKeyboardInput.validateAnswer();

        return false;
    }

    @Override
    public boolean hasAnswer() {
        return mAnswers[0] != null;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        model.set(getIndex(), mAnswers[0]);
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if (model.get(getIndex()) != null) {
            mAnswers[0] = model.get(getIndex());
            return true;
        }
        return false;
    }

    @Override
    public boolean lock() {
        if(mKeyboardInput.getInputView() != null){
            if(!IS_UNLOCKED)
                return true;
            mKeyboardInput.lock();
            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if(mKeyboardInput.getInputView() != null){
            if(IS_UNLOCKED)
                return true;
            mKeyboardInput.unlock();
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if(mKeyboardInput.getInputView() != null) {
            if (!nse(mAnswers[0], mKeyboardInput.getAnswer())) {
                mKeyboardInput.setAnswer(mAnswers[0]);
                mKeyboardInput.loadAnswerIntoInputView(viewModel);
            }
            return true;
        }
        return false;
    }

    @Override
    public void bindListeners(ActivityFormSection context, @Nullable Runnable onAnswerEventLambda) {
        mKeyboardInput.bindListeners(context, ()->{
            mAnswers[0] = mKeyboardInput.getAnswer();
            if (onAnswerEventLambda != null)
                onAnswerEventLambda.run();
        });
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {
        mKeyboardInput.setupImeAction(toolkit);
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
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        LinearLayout item;
        if(getInputView() == null) {
            item = (LinearLayout) inflater.inflate(getResId(), parent, false);

            String qStmt;
            if (hasExtras())
                qStmt = "<b>["+getExtra(0).toString()+"] : </b>" + labels.getLabel(getIndex());
            else
                qStmt = labels.getLabel(getIndex());

            Spanned htm = Html.fromHtml(qStmt);
            TextView tvLabel = item.findViewById(R.id.tv);
            ThemeUtils.setupTextViewStylesByLocale(labels.getLocale(), tvLabel);
            tvLabel.setText(htm);

            LinearLayout c_kbi = item.findViewById(R.id.kbi);
            mKeyboardInput.inflate(inflater, labels, c_kbi);

            setInputView(item);
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }
}
