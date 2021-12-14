package pk.gov.pbs.formbuilder.inputs.grouped;

import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.validator.Validator;

public class GroupInputButtonedKBI extends GroupInputLinearLayout {
    private boolean crossEditInfiniteLoopHack = false;
    protected EditText kbi;
    protected Button btn_1, btn_2;
    protected OnButtonClick onButtonClick;
    protected int inputType;

    public GroupInputButtonedKBI(String index, OnButtonClick event) {
        super(index, R.layout.input_group_btn_kbi);
        mAnswers = new ValueStore[1];
        onButtonClick = event;
    }

    public GroupInputButtonedKBI(String index, int inputType, OnButtonClick event) {
        this(index, event);
        this.inputType = inputType;
    }

    public GroupInputButtonedKBI(String index, int inputType, Validator validator, OnButtonClick event) {
        this(index, inputType, event);
        super.mValidator = validator;
    }

    @Override
    public boolean hasAnswer() {
        return mAnswers != null && mAnswers.length > 0 && mAnswers[0] != null;
    }

    @Override
    public boolean validateAnswer() {
        if(canSkipValidate())
            return true;

        if (hasAnswer())
            return getValidator().isValid(mAnswers[0]);

        return false;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if(hasAnswer()){
            model.set(getIndex(), mAnswers[0]);
        }
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
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if(getInputView() != null){
            if(hasAnswer()) {
                kbi.setText(mAnswers[0].toString());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean lock() {
        if (kbi != null){
            if (!IS_UNLOCKED)
                return true;

            kbi.setEnabled(false);
            if (btn_1 != null)
                btn_1.setEnabled(false);
            if (btn_2 != null)
                btn_2.setEnabled(false);

            IS_UNLOCKED = false;

            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if (kbi != null){
            if (IS_UNLOCKED)
                return true;

            kbi.setEnabled(true);
            if (btn_1 != null)
                btn_1.setEnabled(true);
            if (btn_2 != null)
                btn_2.setEnabled(true);
            IS_UNLOCKED = true;

            return true;
        }
        return false;
    }

    @Override
    public boolean requestFocus() {
        if (getInputView() != null)
            return kbi.requestFocus();
        return false;
    }

    @Override
    public boolean hasFocus() {
        if (getInputView() != null)
            return kbi.hasFocus();

        return false;
    }

    @Override
    public boolean hasIndex(String abIndex) {
        return getIndex().equalsIgnoreCase(abIndex);
    }

    @Override
    public void bindListeners(ActivityFormSection context, @Nullable Runnable onAnswerEventLambda) {
        kbi.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {

                if(kbi.getInputType() == InputType.TYPE_CLASS_NUMBER && s.toString().length() > Constants.INPUT_MAX_NUMBERS_LIMIT) {
                    context.getUXToolkit().showToast(R.string.max_digit_limit_exceeded_msg);
                    kbi.setText(s.toString().substring(0, Constants.INPUT_MAX_NUMBERS_LIMIT));
                    kbi.setSelection(Constants.INPUT_MAX_NUMBERS_LIMIT);
                    return;
                }

                if(kbi.getInputType() == InputType.TYPE_CLASS_TEXT && s.toString().length() > Constants.INPUT_MAX_CHARACTERS_LIMIT) {
                    context.getUXToolkit().showToast(R.string.max_character_limit_exceeded_msg);
                    kbi.setText(s.toString().substring(0, Constants.INPUT_MAX_CHARACTERS_LIMIT));
                    kbi.setSelection(Constants.INPUT_MAX_CHARACTERS_LIMIT);
                    return;
                }

                if(crossEditInfiniteLoopHack)
                    return;

                crossEditInfiniteLoopHack = true;

                if (!s.toString().isEmpty()) {
                    if (mAnswers[0] == null) {
                        mAnswers[0] = new ValueStore(s.toString());
                    } else {
                        mAnswers[0].setValue(s.toString());
                    }
                }else
                    mAnswers[0] = null;

                if (onAnswerEventLambda != null)
                    onAnswerEventLambda.run();

                crossEditInfiniteLoopHack = false;
            }
        });

        btn_1.setOnClickListener((view) -> {
            onButtonClick.onClick(view, kbi, 1);
            kbi.onEditorAction(kbi.getImeActionId());
        });

        if (btn_2 != null) {
            btn_2.setOnClickListener((view) -> {
                onButtonClick.onClick(view, kbi, 2);
                kbi.onEditorAction(kbi.getImeActionId());
            });
        }
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {

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
        if (getInputView() != null){
            kbi.setText("");
        }
    }

    public void inflate(LayoutInflater inflater, LabelProvider labelProvider, ViewGroup container){
        LinearLayout item;
        if(getInputView() == null) {
            item = (LinearLayout) inflater.inflate(getResId(), container, false);

            ((TextView) item.findViewById(R.id.tv)).setText(
                    Html.fromHtml(labelProvider.getLabel(getIndex()))
            );

            kbi =  item.findViewById(R.id.kbi);
            kbi.setInputType(inputType);
            if (labelProvider.hasHint(getIndex()+"_kbi"))
                kbi.setHint(labelProvider.getHint(getIndex()+"_kbi"));

            btn_1 = item.findViewById(R.id.btn_1);
            btn_2 = item.findViewById(R.id.btn_2);

            String btnLabel1 = labelProvider.getLabel(getIndex()+"_btn");
            if (btnLabel1 == null)
                btnLabel1 = labelProvider.getLabel(getIndex()+"_btn_1");

            String btnLabel2 = labelProvider.getLabel(getIndex()+"_btn_2");

            if (btnLabel1 != null) {
                btn_1.setText(btnLabel1);
            }

            if (btnLabel2 != null) {
                    btn_2.setText(btnLabel2);
            } else {
                btn_2.setVisibility(View.GONE);
            }

            setInputView(item);
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        container.addView(item);
    }

    /**
     * Interface for button click event
     */
    public interface OnButtonClick {
        void onClick(View btn, EditText kbi, int which);
    }
}
