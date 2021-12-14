package pk.gov.pbs.formbuilder.inputs.grouped;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.utils.ThemeUtils;
import pk.gov.pbs.formbuilder.validator.Validator;

public class GroupInputKBI2x extends GroupInputLinearLayout {
    private final KeyboardInput[] mKeyboardInputs;

    public GroupInputKBI2x(String index, String... extras) {
        super(index, R.layout.input_group_kbi_2x, extras);
        mAnswers = new ValueStore[2];
        mKeyboardInputs = new KeyboardInput[]{
                new KeyboardInput(index + 'a'),
                new KeyboardInput(index + 'b')
        };
    }

    public GroupInputKBI2x(String index, int inputType, String... extras) {
        this(index, extras);
        setInputTypes(inputType);
    }


    public GroupInputKBI2x(String index, int[] inputTypes, String... extras) {
        this(index, extras);
        setInputTypes(inputTypes);
    }


    public GroupInputKBI2x(String index, int inputType, Validator validator, String... extras) {
        this(index, inputType, extras);
        if (validator != null) {
            super.mValidator = validator;
            setInputValidators(validator);
        }
    }


    public GroupInputKBI2x(String index, int inputType, Validator[] validator, String... extras) {
        this(index, inputType, extras);
        if (validator != null && validator.length > 0) {
            super.mValidator = validator[0];
            setInputValidators(validator);
        }
    }


    public GroupInputKBI2x(String index, int[] inputType, Validator validator, String... extras) {
        this(index, inputType, extras);
        if (validator != null) {
            super.mValidator = validator;
            setInputValidators(validator);
        }
    }

    public GroupInputKBI2x(String index, int[] inputType, Validator[] validator, String... extras) {
        this(index, inputType, extras);
        if (validator != null && validator.length > 0) {
            super.mValidator = validator[0];
            setInputValidators(validator);
        }
    }

    public void setInputTypes(int... inputTypes){
        if (inputTypes != null && inputTypes.length > 0) {
            if (inputTypes.length == 1) {
                mKeyboardInputs[0].setInputType(inputTypes[0]);
                mKeyboardInputs[1].setInputType(inputTypes[0]);
            } else if (inputTypes.length == 2) {
                mKeyboardInputs[0].setInputType(inputTypes[0]);
                mKeyboardInputs[1].setInputType(inputTypes[1]);
            }
        }
    }

    public void setInputValidators(Validator... validators){
        if (validators != null && validators.length > 0) {
            if (validators.length == 1) {
                mKeyboardInputs[0].setValidator(validators[0]);
                mKeyboardInputs[1].setValidator(validators[0]);
            } else if (validators.length == 2) {
                mKeyboardInputs[0].setValidator(validators[0]);
                mKeyboardInputs[1].setValidator(validators[1]);
            }
        }
    }

    public KeyboardInput[] getInternalInputs(){
        return mKeyboardInputs;
    }

    @Override
    public boolean hasAnswer() {
        return mAnswers[0] != null && mAnswers[1] != null;
    }

    @Override
    public boolean validateAnswer() {
        if(canSkipValidate())
            return true;

        if (hasAnswer())
            return mKeyboardInputs[0].validateAnswer()
                    && mKeyboardInputs[1].validateAnswer();

        return false;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if(hasAnswer()){
            model.set(getIndex()+'a', mAnswers[0]);
            model.set(getIndex()+'b', mAnswers[1]);
        }
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if (model.get(getIndex() + 'a') != null || model.get(getIndex() + 'b') != null) {
            mAnswers[0] = model.get(getIndex() + 'a');
            mAnswers[1] = model.get(getIndex() + 'b');
            return true;
        }
        return false;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if(getInputView() != null){
            if (mAnswers[0] == null || !mAnswers[0].equalsIgnoreCase(mKeyboardInputs[0].getAnswer())) {
                mKeyboardInputs[0].setAnswer(mAnswers[0]);
                mKeyboardInputs[0].loadAnswerIntoInputView(viewModel);
            }

            if (mAnswers[1] == null || !mAnswers[1].equalsIgnoreCase(mKeyboardInputs[1].getAnswer())) {
                mKeyboardInputs[1].setAnswer(mAnswers[1]);
                mKeyboardInputs[1].loadAnswerIntoInputView(viewModel);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean lock() {
        if (mKeyboardInputs[0].getInputView() != null){
            if (!IS_UNLOCKED)
                return true;

            mKeyboardInputs[0].lock();
            mKeyboardInputs[1].lock();
            IS_UNLOCKED = false;

            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if (mKeyboardInputs[0].getInputView() != null){
            if (IS_UNLOCKED)
                return true;

            mKeyboardInputs[0].unlock();
            mKeyboardInputs[1].unlock();
            IS_UNLOCKED = true;

            return true;
        }
        return false;
    }

    @Override
    public boolean requestFocus() {
        if (getInputView() != null)
            return mKeyboardInputs[0].requestFocus();
        return false;
    }

    @Override
    public boolean hasFocus() {
        if (getInputView() != null)
            return mKeyboardInputs[0].hasFocus() || mKeyboardInputs[1].hasFocus();

        return false;
    }

    @Override
    public void bindListeners(ActivityFormSection context, @Nullable Runnable onAnswerEventLambda) {
        mKeyboardInputs[0].bindListeners(context, () -> {
            setAnswer(0, mKeyboardInputs[0].getAnswer());
            if (onAnswerEventLambda != null)
                onAnswerEventLambda.run();
        });

        mKeyboardInputs[1].bindListeners(context, () -> {
            setAnswer(1, mKeyboardInputs[1].getAnswer());
            if (onAnswerEventLambda != null)
                onAnswerEventLambda.run();
        });

        EditText kbi_1 = mKeyboardInputs[0].getInputView();
        kbi_1.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        kbi_1.setOnEditorActionListener((v, actionId, event)->{
            if(kbi_1.focusSearch(View.FOCUS_RIGHT) != null)
                kbi_1.focusSearch(View.FOCUS_RIGHT).requestFocus();
            return true;
        });

        EditText kbi_2 = mKeyboardInputs[1].getInputView();
        kbi_2.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        kbi_2.setOnEditorActionListener((v, actionId, event)->{
            if(kbi_2.focusSearch(View.FOCUS_DOWN) != null)
                kbi_2.focusSearch(View.FOCUS_DOWN).requestFocus();
            else
                context.getUXToolkit().hideKeyboardFrom(kbi_2);

            return true;
        });
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {
        EditText kbi_2 = mKeyboardInputs[1].getInputView();
        kbi_2.setImeActionLabel("ASK NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        kbi_2.setOnEditorActionListener((v, actionId, event) -> {
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
        if (getInputView() != null){
            mKeyboardInputs[0].reset();
            mKeyboardInputs[1].reset();
        }
    }

    public void inflate(LayoutInflater inflater, LabelProvider labelProvider, ViewGroup container){
        LinearLayout item;
        if(getInputView() == null) {
            item = (LinearLayout) inflater.inflate(getResId(), container, false);

            TextView label = item.findViewById(R.id.tv);
            if (labelProvider.hasLabel(getIndex()))
                label.setText(Html.fromHtml(labelProvider.getLabel(getIndex())));
            else
                label.setVisibility(View.GONE);

            LinearLayout c_kbi_1, c_kbi_2;
            c_kbi_1 =  item.findViewById(R.id.kbi_1);
            c_kbi_2 =  item.findViewById(R.id.kbi_2);

            mKeyboardInputs[0].inflate(inflater, labelProvider, c_kbi_1);
            mKeyboardInputs[1].inflate(inflater, labelProvider, c_kbi_2);

            TextView tv_1 = item.findViewById(R.id.tv_1);
            TextView tv_2 = item.findViewById(R.id.tv_2);

            String lblA = labelProvider.getLabel(getIndex()+"a");
            String lblB = labelProvider.getLabel(getIndex()+"b");

            if (lblA != null) {
                tv_1.setText(Html.fromHtml(lblA));
                EditText kbi_1 = mKeyboardInputs[0].getInputView();
                kbi_1.setOnFocusChangeListener((v, hasFocus) -> {
                    int colorCode = (hasFocus) ? ThemeUtils.getColorByTheme(inflater.getContext(), R.attr.colorAccent)
                            : ThemeUtils.getColorByTheme(inflater.getContext(), R.attr.colorTextDim);

                    tv_1.setTextColor(colorCode);
                    kbi_1.setHintTextColor(colorCode);
                    kbi_1.setTextColor(colorCode);

                });
            }

            if (lblB != null) {
                tv_2.setText(Html.fromHtml(lblB));
                EditText kbi_2 = mKeyboardInputs[1].getInputView();
                kbi_2.setOnFocusChangeListener((v, hasFocus) -> {
                    int colorCode = (hasFocus) ? ThemeUtils.getColorByTheme(inflater.getContext(), R.attr.colorAccent)
                            : ThemeUtils.getColorByTheme(inflater.getContext(), R.attr.colorTextDim);

                    tv_2.setTextColor(colorCode);
                    kbi_2.setHintTextColor(colorCode);
                    kbi_2.setTextColor(colorCode);

                });
            }

            setInputView(item);
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        container.addView(item);
    }
}
