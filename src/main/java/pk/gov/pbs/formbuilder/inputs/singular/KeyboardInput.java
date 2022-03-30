package pk.gov.pbs.formbuilder.inputs.singular;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.core.ActivityCustom;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.validator.ExceptionalInputLength;
import pk.gov.pbs.formbuilder.validator.OptionalValidator;
import pk.gov.pbs.formbuilder.validator.PhoneNumberValidator;
import pk.gov.pbs.formbuilder.validator.Validator;

public class KeyboardInput extends SingularInput {
    private boolean crossEditInfiniteLoopHack = false;
    protected int inputType = Constants.INVALID_NUMBER;
    protected EditText inputElement;
    protected Validator mValidator;

    public KeyboardInput(String index){
        super(index, R.layout.input_et);
        mAnswers = new ValueStore[1];
    }

    public KeyboardInput(String index, int inputType, int resId){
        super(index, resId);
        mAnswers = new ValueStore[1];
        this.inputType = inputType;
    }

    public KeyboardInput(String index, Validator validator){
        this(index);
        setValidator(validator);
    }

    public KeyboardInput(String index, int inputType){
        this(index);
        this.inputType = inputType;
    }

    public KeyboardInput(String index, int inputType, Validator validator){
        this(index, inputType);
        mValidator = validator;
    }

    public KeyboardInput(String index, int inputType, Validator validator, int resId){
        this(index, inputType, resId);
        mValidator = validator;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
        if(inputElement != null){
            inputElement.setInputType(inputType);
        }
    }

    public void setValidator(Validator validator){
        mValidator = validator;
        if (
                mValidator instanceof PhoneNumberValidator ||
                (
                        mValidator instanceof OptionalValidator
                        && ((OptionalValidator) mValidator).altValidator() != null
                        && ((OptionalValidator) mValidator).altValidator() instanceof PhoneNumberValidator
                )
        )
            inputType = InputType.TYPE_CLASS_PHONE;
        if (inputElement != null)
            inputElement.setInputType(inputType);
    }
    public Validator getValidator(){
        return mValidator;
    }

    public void setHintText(String hintText) {
        if(inputElement != null)
            inputElement.setHint(hintText);
    }


    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel){
        if(inputElement != null && !nse(mAnswers[0], inputElement.getText().toString())){
            inputElement.setText(
                    hasAnswer() ? getAnswer().toString() : ""
            );

            return true;
        }
        return false;
    }

    public EditText getInputView() {
        return inputElement;
    }

    @Override
    public boolean lock() {
        if(inputElement != null){
            if (!IS_UNLOCKED)
                return true;

            inputElement.setEnabled(false);
//            inputElement.setTextColor(Color.CYAN);
//            inputElement.setBackgroundColor(Color.parseColor("#FF1F272E"));
            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if(inputElement != null){
            if (IS_UNLOCKED)
                return true;

            inputElement.setEnabled(true);
//            inputElement.setTextColor(Color.WHITE);
//            inputElement.setBackgroundColor(Color.TRANSPARENT);
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent){
        if (inputType == Constants.INVALID_NUMBER)
            inputType = inputElement.getInputType();

        inputElement.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                LENGTH_CHECKS: {
                    if (mValidator instanceof ExceptionalInputLength) {
                        int maxLength = ((ExceptionalInputLength) mValidator).getMaxLength();
                        if (s.toString().length() > maxLength) {
                            inputElement.setText(s.toString().substring(0, maxLength));
                            inputElement.setSelection(maxLength);
                            return;
                        }

                        break LENGTH_CHECKS;
                    } else if (mValidator instanceof OptionalValidator) {
                        Validator altValidator = ((OptionalValidator) mValidator).altValidator();
                        if (altValidator instanceof ExceptionalInputLength) {
                            int maxLength = ((ExceptionalInputLength) altValidator).getMaxLength();
                            if (s.toString().length() > maxLength) {
                                inputElement.setText(s.toString().substring(0, maxLength));
                                inputElement.setSelection(maxLength);
                                return;
                            }

                            break LENGTH_CHECKS;
                        }
                    }

                    if (inputType == InputType.TYPE_CLASS_NUMBER && s.toString().length() > Constants.INPUT_MAX_NUMBERS_LIMIT) {
                        context.getUXToolkit().showToast(R.string.max_digit_limit_exceeded_msg);
                        inputElement.setText(s.toString().substring(0, Constants.INPUT_MAX_NUMBERS_LIMIT));
                        inputElement.setSelection(Constants.INPUT_MAX_NUMBERS_LIMIT);
                        return;
                    }
                    //It is derived here that input type is text (alphanumeric)
                    else if (s.toString().length() > Constants.INPUT_MAX_CHARACTERS_LIMIT) {
                        context.getUXToolkit().showToast(R.string.max_character_limit_exceeded_msg);
                        inputElement.setText(s.toString().substring(0, Constants.INPUT_MAX_CHARACTERS_LIMIT));
                        inputElement.setSelection(Constants.INPUT_MAX_CHARACTERS_LIMIT);
                        return;
                    }
                }

                if (crossEditInfiniteLoopHack)
                    return;

                crossEditInfiniteLoopHack = true;
                if(!s.toString().isEmpty() && mValidator != null && !mValidator.isValid(new ValueStore(s.toString())))
                    inputElement.setError(mValidator.getRuleStatement());
                else
                    inputElement.setError(null);

                if(!s.toString().isEmpty()) {
                    if (getAnswer(0) == null) {
                        setAnswer(
                            new ValueStore(s.toString())
                        );
                    } else {
                        getAnswer().setValue(s.toString());
                    }
                }else
                    setAnswer(null);

                if (onAnswerEvent != null)
                    onAnswerEvent.run();

                crossEditInfiniteLoopHack = false;
            }
        });
    }

    @Override
    public boolean requestFocus() {
        if(getInputView() != null) {
            boolean result =  getInputView().requestFocus();
            if (result) {
                getInputView().post(() -> {
                    ((ActivityCustom) getInputView().getContext())
                            .getUXToolkit().showKeyboardTo(getInputView());
                });
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasFocus() {
        if (getInputView() != null)
            return getInputView().hasFocus();
        return false;
    }

    @Override
    public void reset() {
        if(getInputView() != null) {
            super.setAnswer(null);
            getInputView().setText("");
            if (!isVisible())
                quickShow();
        }
    }

    @Override
    public boolean hasAnswer() {
        return getAnswer() != null;
    }

    public boolean validateAnswer(){
        if (mValidator != null)
            return mValidator.isValid(mAnswers[0]);
        return true;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        model.set(getIndex(), getAnswer());
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
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        EditText item;
        if(getInputView() == null) {
            item = (EditText) inflater.inflate(getResId(), parent, false);
            if (labels.hasHint(getIndex()+"_kbi"))
                item.setHint(labels.getHint(getIndex()+"_kbi"));
            if(inputType != 0)
                item.setInputType(inputType);
            inputElement = item;
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {
        getInputView().setImeActionLabel("ASK NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        getInputView().setOnEditorActionListener((v, actionId, event) -> {
            v.post(()-> {
                int id = toolkit.getQuestionIndexByAskableIndex(getIndex());
                if(id != Constants.INVALID_NUMBER)
                    toolkit.askNextQuestion(id);
                else
                    toolkit.askNextQuestion();
            });
            return true;
        });
    }
}
