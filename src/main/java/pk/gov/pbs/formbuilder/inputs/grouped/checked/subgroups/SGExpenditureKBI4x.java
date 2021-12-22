package pk.gov.pbs.formbuilder.inputs.grouped.checked.subgroups;

import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputLinearLayout;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.utils.FormBuilderThemeHelper;
import pk.gov.pbs.formbuilder.validator.OptionalValidator;
import pk.gov.pbs.formbuilder.validator.PhoneNumberValidator;
import pk.gov.pbs.formbuilder.validator.Validator;

public class SGExpenditureKBI4x extends GroupInputLinearLayout {
    protected EditText[] mEditTexts;
    protected TextView[] mLabelsInput;
    protected int mInputType;

    public SGExpenditureKBI4x(String index, int inputType, Validator validator) {
        super(index, R.layout.subgroup_input_kbi_4x, validator);
        mAnswers = new ValueStore[4];
        mInputType = inputType;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if (mEditTexts != null){
            for (int i = 0; i < mEditTexts.length; i++){
                if (mEditTexts[i]!=null && mAnswers[i]!=null)
                    mEditTexts[i].setText(mAnswers[i].toString());
            }
        }
        return false;
    }

    @Override
    public boolean hasAnswer() {
        if (mAnswers != null){
            boolean result = false;
            for (ValueStore vs : mAnswers){
                result |= vs != null && !vs.isEmpty();
            }
            return result;
        }
        return false;
    }

    @Override
    public boolean lock() {
        if (!IS_UNLOCKED)
            return true;

        if (mEditTexts != null){
            for (EditText editText : mEditTexts){
                if (editText != null)
                    editText.setEnabled(false);
            }
            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if (IS_UNLOCKED)
            return true;
        if (mEditTexts != null){
            for (EditText editText : mEditTexts){
                if (editText != null)
                    editText.setEnabled(true);
            }
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        if (mEditTexts != null) {
            for (EditText editText : mEditTexts) {
                if (editText != null)
                    editText.setText("");
            }
        }
    }

    @Override
    public boolean requestFocus() {
        if (mEditTexts != null) {
            for (EditText editText : mEditTexts) {
                if (editText != null)
                    return editText.requestFocus();
            }
        }
        return false;
    }

    @Override
    public boolean hasFocus() {
        if (mEditTexts != null) {
            for (EditText editText : mEditTexts) {
                if (editText != null && editText.hasFocus())
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasIndex(String abIndex) {
        return getIndex().equalsIgnoreCase(abIndex);
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {

    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if (mAnswers != null) {
            for (int i = 0; i < mAnswers.length; i++) {
                if (mAnswers[i] != null){
                    model.set(getIndex() + ((int)i+1), mAnswers[i]);
                }
            }
        }
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        boolean result = false;
        if (mAnswers != null) {
            for (int i = 0; i < mAnswers.length; i++) {
                mAnswers[i] = model.get(getIndex() + ((int)i+1));
                result |= mAnswers[i] != null;
            }
            return result;
        }
        return false;
    }

    @Override
    public boolean validateAnswer() {
        if (canSkipValidate())
            return true;
        boolean result = true;
        if (mAnswers != null) {
            for (ValueStore mAnswer : mAnswers) {
                if (mAnswer != null) {
                    result &= getValidator().isValid(mAnswer);
                }
            }
            return result;
        }
        return false;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {
        for (int i=0; i<mEditTexts.length; i++){
            final int index = i;
            EditText input = mEditTexts[i];
            if (input != null) {
                input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }
                    @Override
                    public void afterTextChanged(Editable s) {
                        if(mInputType == InputType.TYPE_CLASS_NUMBER && s.toString().length() > Constants.INPUT_MAX_NUMBERS_LIMIT) {
                            if(
                                    mValidator != null
                                    && mValidator instanceof OptionalValidator
                                    && ((OptionalValidator) mValidator).altValidator() != null
                                    && ((OptionalValidator) mValidator).altValidator() instanceof PhoneNumberValidator
                            ) {
                                // do nothing, because phone number will be validated next
                            }else {
                                context.getUXToolkit().showToast(R.string.max_digit_limit_exceeded_msg);
                                input.setText(s.toString().substring(0, Constants.INPUT_MAX_NUMBERS_LIMIT));
                                input.setSelection(Constants.INPUT_MAX_NUMBERS_LIMIT);
                                return;
                            }
                        } else if(mInputType == InputType.TYPE_CLASS_TEXT && s.toString().length() > Constants.INPUT_MAX_CHARACTERS_LIMIT){
                            context.getUXToolkit().showToast(R.string.max_character_limit_exceeded_msg);
                            input.setText(s.toString().substring(0, Constants.INPUT_MAX_CHARACTERS_LIMIT));
                            input.setSelection(Constants.INPUT_MAX_CHARACTERS_LIMIT);
                            return;
                        }

                        if(!s.toString().isEmpty() && mValidator != null && !mValidator.isValid(new ValueStore(s.toString())))
                            input.setError(mValidator.getRuleStatement());

                        if(!s.toString().isEmpty()) {
                            if (getAnswer(index) == null) {
                                setAnswer(
                                        index,
                                        new ValueStore(s.toString())
                                );
                            } else {
                                getAnswer(index).setValue(s.toString());
                            }
                        }else
                            setAnswer(index,null);
                        if (onAnswerEvent != null)
                            onAnswerEvent.run();
                    }
                });

                if ((index + 1) < mEditTexts.length) {
                    input.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
                    input.setOnEditorActionListener((v, actionId, event) -> {
                        if (index + 1 < mEditTexts.length && mEditTexts[index + 1] != null)
                            mEditTexts[index + 1].requestFocus();
                        else if (index + 2 < mEditTexts.length && mEditTexts[index + 2] != null)
                            mEditTexts[index + 2].requestFocus();
                        return true;
                    });
                } else {
                    input.setImeActionLabel("DONE", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
                    input.setOnEditorActionListener((v, actionId, event) -> {
                        context.getUXToolkit().hideKeyboardFrom(input);
                        input.clearFocus();
                        return true;
                    });
                }

                input.setOnFocusChangeListener((v, hasFocus) -> {
                    int colorCode = (hasFocus) ? FormBuilderThemeHelper.getColorByTheme(context, R.attr.colorAccent)
                            : FormBuilderThemeHelper.getColorByTheme(context, R.attr.colorTextDim);

                    if (mLabelsInput[index] != null)
                        mLabelsInput[index].setTextColor(colorCode);

                    input.setHintTextColor(colorCode);
                    input.setTextColor(colorCode);

                });
            }

        }
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        LinearLayout item;
        if(getInputView() == null) {
            item = (LinearLayout) inflater.inflate(getResId(), parent, false);
            ((TextView) item.findViewById(R.id.tv_header_1)).setText(Html.fromHtml(
                    inflater.getContext().getString(R.string.heading_paid_consumed)
            ));
            ((TextView) item.findViewById(R.id.tv_header_2)).setText(Html.fromHtml(
                    inflater.getContext().getString(R.string.heading_unpaid_consumed)
            ));
            mEditTexts = new EditText[4];
            mLabelsInput = new TextView[4];
            char suffix = 'a';
            for (int i=1; i<=4; i++){
                int lblResId = inflater
                        .getContext()
                        .getResources()
                        .getIdentifier("tv_" + i, "id", inflater.getContext().getPackageName());
                mLabelsInput[i-1] = item.findViewById(lblResId);
                mLabelsInput[i-1].setText(Html.fromHtml(labels.getLabel(getIndex()+'_'+suffix++)));

                int kbiResId = inflater
                        .getContext()
                        .getResources()
                        .getIdentifier("kbi_" + i, "id", inflater.getContext().getPackageName());
                mEditTexts[i-1] = item.findViewById(kbiResId);
                mEditTexts[i-1].setHint(labels.getLabel(getIndex()+'_'+i));
                mEditTexts[i-1].setInputType(mInputType);

                if (labels.getLabel(getIndex()+'_'+i).equalsIgnoreCase("-")){
                    mEditTexts[i-1].setText("-");
                    mEditTexts[i-1].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    mEditTexts[i-1].setEnabled(false);
                    mLabelsInput[i-1] = null;
                    mEditTexts[i-1] = null;
                }
            }

            inputElement = item;
        } else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }

        parent.addView(item);
    }

}
