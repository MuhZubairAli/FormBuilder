package pk.gov.pbs.formbuilder.inputs.grouped;

import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.inputs.singular.KeyboardInput;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.utils.FormBuilderThemeHelper;
import pk.gov.pbs.formbuilder.validator.Validator;
import pk.gov.pbs.utils.ThemeUtils;

public class GroupInputCheckedKBI extends GroupInputTableRow {
    protected KeyboardInput mKeyboardInput;
    protected CheckBox cbi;
    protected TextView tvCode;

    public GroupInputCheckedKBI(String index, int inputType, Validator validator, String... extras) {
        super(index, R.layout.input_group_tv_cbi_kbi, extras);
        mAnswers = new ValueStore[2];
        mKeyboardInput = new KeyboardInput(index + 'b', inputType, validator);
        mValidator = validator;
    }

    public GroupInputCheckedKBI(String index, String... extras) {
        this(index, InputType.TYPE_CLASS_NUMBER, null, extras);
    }

    public GroupInputCheckedKBI(String index, Validator validator, String... extras) {
        this(index, InputType.TYPE_CLASS_NUMBER, validator, extras);
    }

    private void lockItem(CheckBox item){
        if(!item.isChecked())
            FormBuilderThemeHelper.applyThemedDrawableToView(item, R.attr.bgSelectableLockedUnanswered);
        else
            FormBuilderThemeHelper.applyThemedDrawableToView(item, R.attr.bgSelectableLocked);

        if (tvCode != null && tvCode.getVisibility() == View.VISIBLE)
            FormBuilderThemeHelper.applyThemedDrawableToView(tvCode, R.attr.bgSelectableLockedUnanswered);

        item.setClickable(false);
    }

    private void unlockItem(CheckBox item){
        FormBuilderThemeHelper.applyThemedDrawableToView(item, R.attr.bgSelectableUnlocked);
        if (tvCode != null && tvCode.getVisibility() == View.VISIBLE)
            FormBuilderThemeHelper.applyThemedDrawableToView(tvCode, R.attr.bgSelectableUnlocked);

        item.setClickable(true);
    }

    @Override
    public boolean validateAnswer() {
        if (canSkipValidate())
            return true;

        if (hasAnswer())
            return getValidator().isValid(mAnswers[1]);

        return false;
    }

    @Override
    public boolean lock() {
        if(inputElement != null){
            if(!IS_UNLOCKED)
                return true;

            if(cbi.isChecked())
                mKeyboardInput.lock();
            lockItem(cbi);
            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if(inputElement != null){
            if(IS_UNLOCKED)
                return true;

            if(cbi.isChecked())
                mKeyboardInput.unlock();
            unlockItem(cbi);
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean requestFocus() {
        if(cbi != null) {
            if (cbi.isChecked())
                return mKeyboardInput.requestFocus();
        }
        return false;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        model.set(getIndex()+"a", mAnswers[0]);

        if (hasExtras())
            model.set(getIndex()+"c", getExtra(0));

        if(mAnswers[0].toInt()==1){
            model.set(getIndex()+"b", mAnswers[1]);
        }
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if (model.get(getIndex()+"a") != null) {
            mAnswers[0] = model.get(getIndex() + "a");
            mAnswers[1] = model.get(getIndex() + "b");
            return true;
        }
        return false;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if(cbi != null){
            if(mAnswers[0] != null && mAnswers[0].toInt()==1) {
                cbi.setChecked(mAnswers[0].toInt() == 1);
                if (!nse(mAnswers[1], mKeyboardInput.getAnswer())) {
                    mKeyboardInput.setAnswer(mAnswers[0].toInt() == 1 ? mAnswers[1] : null);
                    mKeyboardInput.loadAnswerIntoInputView(viewModel);
                }

                return true;
            }
        }
        return false;
    }

    @Override
    public void bindListeners(ActivityFormSection context, @Nullable Runnable onAnswerEventLambda) {
        EditText kbi = mKeyboardInput.getInputView();
        cbi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                if(mAnswers[0] == null)
                    mAnswers[0] = new ValueStore(1);
                else
                    mAnswers[0].setValue(1);

                mKeyboardInput.unlock();
                mKeyboardInput.requestFocus();
                context.getUXToolkit().showKeyboardTo(kbi);
            } else {
                mKeyboardInput.setAnswer(null);
                if(mAnswers[0] == null)
                    mAnswers[0] = new ValueStore(2);
                else
                    mAnswers[0].setValue(2);

                context.getUXToolkit().hideKeyboardFrom(kbi);
                kbi.clearFocus();
                mKeyboardInput.reset();
                mKeyboardInput.lock();
            }

            if(onAnswerEventLambda != null)
                onAnswerEventLambda.run();
        });

        mKeyboardInput.bindListeners(context, () -> {
            if (!nse(mAnswers[1], mKeyboardInput.getAnswer()))
                mAnswers[1] = mKeyboardInput.getAnswer();
        });

        kbi.setImeActionLabel("DONE", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        kbi.setOnEditorActionListener((v, actionId, event) -> {
            context.getUXToolkit().hideKeyboardFrom(kbi);
            kbi.clearFocus();
            return true;
        });

        if(mAnswers[0] == null && !cbi.isChecked()) {
            mAnswers[0] = new ValueStore(2);
        }
    }

    @Override
    public boolean setAnswers(ValueStore[] answers) {
        if (answers != null && answers.length > 0 && (answers[0].toInt() == 2 || (answers[0].toInt() == 1 && answers[1] != null))){
            System.arraycopy(answers,0,mAnswers,0,answers.length);
            return true;
        }
        return false;
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {
        EditText kbi = mKeyboardInput.getInputView();
        kbi.setImeActionLabel("ASK NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        kbi.setOnEditorActionListener((v, actionId, event) -> {
            v.post(()->{
                int id = toolkit.getQuestionIndexByAskableIndex(getIndex());
                if(id != Constants.INVALID_NUMBER)
                    toolkit.askNextQuestion(id);
                else
                    toolkit.askNextQuestion();
            });
            return true;
        });
    }

    @Override
    public void reset() {
        if(getInputView() != null) {
            cbi.setChecked(false);
        }
    }

    @Override
    public boolean hasAnswer() {
        return (mAnswers[0].toInt() == 2) || (mAnswers[0].toInt() == 1 && mAnswers[1] != null);
    }

    @Override
    public boolean hasFocus() {
        return mKeyboardInput.hasFocus();
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        TableRow item;
        if(getInputView() == null) {
            item = (TableRow) inflater.inflate(getResId(), parent, false);

            Spanned htm = Html.fromHtml(labels.getLabel(getIndex()));
            cbi = item.findViewById(R.id.cbi);
            ThemeUtils.setupTextViewStylesByLocale(labels.getLocale(), cbi);
            cbi.setText(htm);

            ViewGroup container =  item.findViewById(R.id.container_kbi);
            mKeyboardInput.inflate(inflater, labels, container);

            if (labels.hasHint(getIndex()+"_kbi"))
                mKeyboardInput.setHintText(labels.getHint(getIndex()+"_kbi"));
            mKeyboardInput.lock();

            tvCode = item.findViewById(R.id.tv);
            if (hasExtras())
                tvCode.setText(getExtra(0).toString());
            else {
                tvCode.setVisibility(View.GONE);
                //no need to adjust weights for bigger screens
                //cbi.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 7f));
                //kbi.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3f));
            }

            setInputView(item);
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }
}
