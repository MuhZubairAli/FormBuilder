package pk.gov.pbs.formbuilder.inputs.grouped.grid;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.inputs.singular.KeyboardInput;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.meta.ColumnCount;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.utils.FormBuilderThemeHelper;
import pk.gov.pbs.formbuilder.validator.Validator;

public class GroupInputGridKBI extends GroupInputGrid {
    public GroupInputGridKBI(String index, ColumnCount columnCount, KeyboardInput[] singularInput, Validator validator) {
        super(index, columnCount, singularInput, validator);
    }

    @Override
    public boolean validateAnswer() {
        if (canSkipValidate())
            return true;

        boolean result = true;
        for (SingularInput ab : mSingularInputs)
            result &= mValidator.isValid(ab.getAnswer());

        return result;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if (getInputView() != null){
            for (int i=0; i < mSingularInputs.length; i++) {
                if (!nse(mAnswers[i], mSingularInputs[i].getAnswer())) {
                    mSingularInputs[i].setAnswer(mAnswers[i]);
                    mSingularInputs[i].loadAnswerIntoInputView(viewModel);
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasAnswer() {
        boolean result = true;
        for (ValueStore ans : mAnswers){
            result &= ans != null;
        }
        return result;
    }

    public ValueStore[] getCompactAnswers(){
        List<ValueStore> ans = new ArrayList<>();
        for (ValueStore vs : mAnswers){
            if (vs != null)
                ans.add(vs);
        }
        ValueStore[] ca = new ValueStore[ans.size()];
        ca = ans.toArray(ca);
        return ca;
    }

    public SingularInput getInputByIndex(int index){
        return mSingularInputs[index];
    }

    public SingularInput getInputByIndex(String index){
        for (SingularInput ab : mSingularInputs){
            if (ab.getIndex().equalsIgnoreCase(index))
                return ab;
        }
        return null;
    }

    public int getInputArrayIndex(String abIndex){
        for (int i = 0; i < mSingularInputs.length; i++){
            if (mSingularInputs[i].getIndex().equalsIgnoreCase(abIndex))
                return i;
        }
        return Constants.INVALID_NUMBER;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {
        for (int i=0; i<mSingularInputs.length; i++){
            final int index = i;

            if (context.getLabelProvider().getLabel(mSingularInputs[index].getIndex()).equalsIgnoreCase("-")) {
                EditText input = (EditText) mSingularInputs[index].getInputView();
                input.setEnabled(false);
                input.setFocusable(false);
                input.setFocusableInTouchMode(false);
//                input.setText(context.getLabelProvider().getLabel(mSingularInputs[index].getIndex()));
//                input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                mLabelsInputs[index].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                continue;
            }

            mSingularInputs[i].bindListeners(context, ()->{
                mAnswers[index] = mSingularInputs[index].getAnswer();
                if (onAnswerEvent != null)
                    onAnswerEvent.run();
            });

            EditText input = (EditText) mSingularInputs[i].getInputView();
            if ((index + 1) < mSingularInputs.length) {
                input.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
                input.setOnEditorActionListener((v, actionId, event) -> {
                    if (index + 1 < mSingularInputs.length)
                        mSingularInputs[index + 1].requestFocus();
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

                if (mLabelsInputs[index] != null)
                    mLabelsInputs[index].setTextColor(colorCode);

                input.setHintTextColor(colorCode);
                input.setTextColor(colorCode);

            });

            if (i < mLabelsInputs.length && mLabelsInputs[i] != null){
                mLabelsInputs[i].setOnClickListener((v)->{
                    if (mSingularInputs[index].isUnlocked()) {
                        EditText kbi = ((KeyboardInput) mSingularInputs[index]).getInputView();
                        kbi.requestFocus();
                        context.getUXToolkit().showKeyboardTo(kbi);
                    }
                });
            }
        }
    }

    @Override
    public void setupImeAction(NavigationToolkit nToolkit) {
        EditText input = (EditText) mSingularInputs[mSingularInputs.length-1].getInputView();
        input.setImeActionLabel("ASK NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        input.setOnEditorActionListener((v, actionId, event) -> {
            v.post(()->{
                int id = nToolkit.getQuestionIndexByAskableIndex(getIndex());
                if(id != Constants.INVALID_NUMBER)
                    nToolkit.askNextQuestion(id);
                else
                    nToolkit.askNextQuestion();
            });
            return true;
        });
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        for (SingularInput ab : mSingularInputs){
            ab.exportAnswer(model);
        }
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        for (int i=0; i < mSingularInputs.length; i++){
            mAnswers[i] = model.get(mSingularInputs[i].getIndex());
        }
        return true;
    }
}
