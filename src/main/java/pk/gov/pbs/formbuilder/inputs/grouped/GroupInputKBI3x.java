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
import pk.gov.pbs.formbuilder.utils.FormBuilderThemeHelper;
import pk.gov.pbs.formbuilder.validator.Validator;
import pk.gov.pbs.utils.ThemeUtils;

public class GroupInputKBI3x extends GroupInputLinearLayout {
    private final KeyboardInput[] mKeyboardInputs;

    public GroupInputKBI3x(String index, String... extras) {
        super(index, R.layout.input_group_kbi_3x, extras);
        mAnswers = new ValueStore[3];
        mKeyboardInputs = new KeyboardInput[]{
                new KeyboardInput(index + 'a'),
                new KeyboardInput(index + 'b'),
                new KeyboardInput(index + 'c')
        };
    }

    public GroupInputKBI3x(String index, int inputType, String... extras) {
        this(index, extras);
        setInputTypes(inputType);
    }


    public GroupInputKBI3x(String index, int[] inputTypes, String... extras) {
        this(index, extras);
       setInputTypes(inputTypes);
    }
    public GroupInputKBI3x(String index, int inputType, Validator validator, String... extras) {
        this(index, inputType, extras);

        if (validator != null) {
            super.mValidator = validator;
            setInputValidators(validator);
        }
    }

    public GroupInputKBI3x(String index, int inputType, Validator[] validator, String... extras) {
        this(index, inputType, extras);

        if (validator != null && validator.length > 0) {
            super.mValidator = validator[0];
            setInputValidators(validator);
        }
    }

    public GroupInputKBI3x(String index, int[] inputType, Validator validator, String... extras) {
        this(index, inputType, extras);

        if (validator != null) {
            super.mValidator = validator;
            setInputValidators(validator);
        }
    }

    public GroupInputKBI3x(String index, int[] inputType, Validator[] validator, String... extras) {
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
                mKeyboardInputs[2].setInputType(inputTypes[0]);
            } else if (inputTypes.length == 2) {
                mKeyboardInputs[0].setInputType(inputTypes[0]);
                mKeyboardInputs[1].setInputType(inputTypes[1]);
            } else if (inputTypes.length == 3) {
                mKeyboardInputs[0].setInputType(inputTypes[0]);
                mKeyboardInputs[1].setInputType(inputTypes[1]);
                mKeyboardInputs[2].setInputType(inputTypes[2]);
            }
        }
    }
    
    public void setInputValidators(Validator... validators){
        if (validators != null && validators.length > 0) {
            if (validators.length == 1) {
                mKeyboardInputs[0].setValidator(validators[0]);
                mKeyboardInputs[1].setValidator(validators[0]);
                mKeyboardInputs[2].setValidator(validators[0]);
            } else if (validators.length == 2) {
                mKeyboardInputs[0].setValidator(validators[0]);
                mKeyboardInputs[1].setValidator(validators[1]);
            } else if (validators.length == 3) {
                mKeyboardInputs[0].setValidator(validators[0]);
                mKeyboardInputs[1].setValidator(validators[1]);
                mKeyboardInputs[2].setValidator(validators[2]);
            }
        }
    }

    public KeyboardInput[] getInternalInputs(){
        return mKeyboardInputs;
    }

    @Override
    public boolean hasAnswer() {
        return mAnswers[0] != null || mAnswers[1] != null || mAnswers[2] != null;
    }

    public boolean hasAllAnswers(){
        return mAnswers[0] != null && mAnswers[1] != null && mAnswers[2] != null;
    }
    @Override
    public boolean validateAnswer() {
        if(canSkipValidate())
            return true;

        if (hasAnswer()) {
            return mKeyboardInputs[0].validateAnswer()
                    && mKeyboardInputs[1].validateAnswer()
                    && mKeyboardInputs[2].validateAnswer();
        }

        return false;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if(hasAnswer()){
            model.set(getIndex()+'a', mAnswers[0]);
            model.set(getIndex()+'b', mAnswers[1]);
            model.set(getIndex()+'c', mAnswers[2]);
        }
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        boolean result = model.get(getIndex() + 'a') != null || model.get(getIndex() + 'b') != null || model.get(getIndex() + 'c') != null;
        if (result) {
            mAnswers[0] = model.get(getIndex() + 'a');
            mAnswers[1] = model.get(getIndex() + 'b');
            mAnswers[2] = model.get(getIndex() + 'c');
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

            if (mAnswers[2] == null || !mAnswers[2].equalsIgnoreCase(mKeyboardInputs[2].getAnswer())) {
                mKeyboardInputs[2].setAnswer(mAnswers[2]);
                mKeyboardInputs[2].loadAnswerIntoInputView(viewModel);
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
            mKeyboardInputs[2].lock();
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
            mKeyboardInputs[2].unlock();
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
            return mKeyboardInputs[0].hasFocus() || mKeyboardInputs[1].hasFocus() || mKeyboardInputs[2].hasFocus();

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

        mKeyboardInputs[2].bindListeners(context, () -> {
            setAnswer(2, mKeyboardInputs[2].getAnswer());
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
            if(kbi_2.focusSearch(View.FOCUS_RIGHT) != null)
                kbi_2.focusSearch(View.FOCUS_RIGHT).requestFocus();
            return true;
        });

        EditText kbi_3 = mKeyboardInputs[2].getInputView();
        kbi_3.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        kbi_3.setOnEditorActionListener((v, actionId, event)->{
            if(kbi_3.focusSearch(View.FOCUS_DOWN) != null)
                kbi_3.focusSearch(View.FOCUS_DOWN).requestFocus();
            else
                context.getUXToolkit().hideKeyboardFrom(kbi_3);
            return true;
        });
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {
        EditText kbi_3 = mKeyboardInputs[2].getInputView();
        kbi_3.setImeActionLabel("ASK NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        kbi_3.setOnEditorActionListener((v, actionId, event) -> {
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
            for (KeyboardInput input : mKeyboardInputs)
                input.reset();
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

            LinearLayout c_kbi_1, c_kbi_2, c_kbi_3;
            c_kbi_1 =  item.findViewById(R.id.kbi_1);
            c_kbi_2 =  item.findViewById(R.id.kbi_2);
            c_kbi_3 =  item.findViewById(R.id.kbi_3);

            mKeyboardInputs[0].inflate(inflater, labelProvider, c_kbi_1);
            mKeyboardInputs[1].inflate(inflater, labelProvider, c_kbi_2);
            mKeyboardInputs[2].inflate(inflater, labelProvider, c_kbi_3);

            TextView tv_1 = item.findViewById(R.id.tv_1);
            TextView tv_2 = item.findViewById(R.id.tv_2);
            TextView tv_3 = item.findViewById(R.id.tv_3);
            ThemeUtils.setupTextViewStylesByLocale(labelProvider.getLocale(), label, tv_1, tv_2, tv_3);

            String lblA = labelProvider.getLabel(getIndex()+'a');
            String lblB = labelProvider.getLabel(getIndex()+'b');
            String lblC = labelProvider.getLabel(getIndex()+'c');

            if (lblA != null) {
                tv_1.setText(Html.fromHtml(lblA));
                EditText kbi_1 = mKeyboardInputs[0].getInputView();
                kbi_1.setOnFocusChangeListener((v, hasFocus) -> {
                    int colorCode = (hasFocus) ? FormBuilderThemeHelper.getColorByTheme(inflater.getContext(), R.attr.colorAccent)
                            : FormBuilderThemeHelper.getColorByTheme(inflater.getContext(), R.attr.colorTextDim);

                    tv_1.setTextColor(colorCode);
                    kbi_1.setHintTextColor(colorCode);
                    kbi_1.setTextColor(colorCode);

                });
            }

            if (lblB != null) {
                tv_2.setText(Html.fromHtml(lblB));
                EditText kbi_2 = mKeyboardInputs[1].getInputView();
                kbi_2.setOnFocusChangeListener((v, hasFocus) -> {
                    int colorCode = (hasFocus) ? FormBuilderThemeHelper.getColorByTheme(inflater.getContext(), R.attr.colorAccent)
                            : FormBuilderThemeHelper.getColorByTheme(inflater.getContext(), R.attr.colorTextDim);

                    tv_2.setTextColor(colorCode);
                    kbi_2.setHintTextColor(colorCode);
                    kbi_2.setTextColor(colorCode);

                });
            }

            if (lblC != null) {
                tv_3.setText(Html.fromHtml(lblC));
                EditText kbi_3 = mKeyboardInputs[2].getInputView();
                kbi_3.setOnFocusChangeListener((v, hasFocus) -> {
                    int colorCode = (hasFocus) ? FormBuilderThemeHelper.getColorByTheme(inflater.getContext(), R.attr.colorAccent)
                            : FormBuilderThemeHelper.getColorByTheme(inflater.getContext(), R.attr.colorTextDim);

                    tv_3.setTextColor(colorCode);
                    kbi_3.setHintTextColor(colorCode);
                    kbi_3.setTextColor(colorCode);

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
