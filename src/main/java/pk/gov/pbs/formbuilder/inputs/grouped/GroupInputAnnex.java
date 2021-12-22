package pk.gov.pbs.formbuilder.inputs.grouped;

import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.core.ActivityCustom;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.pojos.Annex;
import pk.gov.pbs.formbuilder.pojos.DatumIdentifier;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.validator.Validator;
import pk.gov.pbs.utils.ThemeUtils;

public class GroupInputAnnex extends GroupInputLinearLayout {
    private DatumIdentifier mIdentifier;
    private HashMap<String, String> codes; //Annex Description -> Annex Code
    private List<String> options;
    private boolean crossEditInfiniteLoopHack = false;

    private TextWatcher annexCodeInputWatcher, annexDescInputWatcher;
    private AutoCompleteTextView descInput;
    private EditText codeInput;

    private GroupInputAnnex(String index) {
        super(index, R.layout.input_group_tv_kbi_kbi);
        mAnswers = new ValueStore[2];
        options = new ArrayList<>();
    }

    public GroupInputAnnex(String index, DatumIdentifier identifier) {
        this(index);
        mIdentifier = identifier;
    }

    public GroupInputAnnex(String index, DatumIdentifier identifier, Validator validator) {
        this(index, identifier);
        super.mValidator = validator;
    }

    @Override
    public boolean hasAnswer() {
        return mAnswers[0] != null && mAnswers[1] != null;
    }

    @Override
    public boolean hasIndex(String abIndex) {
        return getIndex().equalsIgnoreCase(abIndex);
    }

    @Override
    public boolean setAnswers(ValueStore... answers) {
        if (answers != null && answers.length == 2 && answers[0] != null && answers[1] != null) {
            return super.setAnswers(answers);
        }
        return false;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        model.set(getIndex()+"_desc", mAnswers[0]);
        model.set(getIndex(), mAnswers[1]);
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if (model.get(getIndex()) != null) {
            mAnswers[0] = model.get(getIndex() + "_desc");
            mAnswers[1] = model.get(getIndex());
            return true;
        }
        return false;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if(hasAnswer()){
            if(descInput != null) {
                descInput.setText(mAnswers[0].toString());
                //only set either desc or code other will be determined automatically
                //because at this point listeners have been added to inputs
                //codeInput.setText(mAnswers[1].toString());
            }
        }
        return false;
    }

    public ValueStore getAnnexDesc() {
        if(descInput != null)
            return mAnswers[0];
        return null;
    }

    public ValueStore getAnnexCode(){
        if (descInput != null)
            return mAnswers[1];
        return null;
    }

    @Override
    public boolean lock() {
        if(inputElement != null){
            if(!IS_UNLOCKED)
                return true;

            descInput.setEnabled(false);
            codeInput.setEnabled(false);
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

            descInput.setEnabled(true);
            codeInput.setEnabled(true);
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    public List<String> getOptions() {
        return options;
    }

    public DatumIdentifier getIdentifier(){
        return mIdentifier;
    }

    public String getCode(@NonNull String annex){
        return codes.get(annex);
    }

    @Override
    public boolean validateAnswer() {
        if(canSkipValidate())
            return true;

        if (hasAnswer())
            return getValidator().isValid(mAnswers[1]);

        return false;
    }

    @Override
    public void bindListeners(ActivityFormSection context, @Nullable Runnable onAnswerEventLambda) {
        if(options.size() == 0) {
            List<Annex> annexes = context.getViewModel().getAnnexures(mIdentifier);
            if (annexes.size() > 0) {
                codes = new HashMap<>();
                for (int i = 0; i < annexes.size(); i++) {
                    options.add(annexes.get(i).desc);
                    codes.put(annexes.get(i).desc, annexes.get(i).code);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    context
                    , android.R.layout.simple_spinner_dropdown_item
                    , options
            );

            descInput.setAdapter(adapter);
        }

        if(annexCodeInputWatcher == null) {
            annexCodeInputWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().length() > Constants.INPUT_MAX_NUMBERS_LIMIT) {
                        codeInput.setError(codeInput.getContext().getString(R.string.max_digit_limit_exceeded_msg));
                        codeInput.setText(s.toString().substring(0, Constants.INPUT_MAX_NUMBERS_LIMIT));
                        codeInput.setSelection(Constants.INPUT_MAX_NUMBERS_LIMIT);
                        return;
                    }

                    if(mValidator != null && !s.toString().isEmpty() && !mValidator.isValid(new ValueStore(s.toString()))) {
                        codeInput.setError(mValidator.getRuleStatement());
                        return;
                    }

                    if(crossEditInfiniteLoopHack)
                        return;

                    crossEditInfiniteLoopHack = true;

                    String desc = null;

                    if(codes.containsValue(s.toString())){
                        for(String lbl : codes.keySet()){
                            if(codes.get(lbl).equalsIgnoreCase(s.toString())){
                                desc = lbl;
                                if(mAnswers[1] == null)
                                    mAnswers[1] = new ValueStore(s.toString());
                                else
                                    mAnswers[1].setValue(s.toString());

                                if(mAnswers[0] == null)
                                    mAnswers[0] = new ValueStore(desc);
                                else
                                    mAnswers[0].setValue(desc);

                                if(onAnswerEventLambda != null)
                                    onAnswerEventLambda.run();

                                break;
                            }
                        }
                    }

                    if(desc != null){
                        descInput.setText(desc);
                    } else {
                        descInput.setText("");
                        mAnswers[0] = null;
                        mAnswers[1] = null;
                    }

                    crossEditInfiniteLoopHack = false;
                }
            };

            codeInput.addTextChangedListener(annexCodeInputWatcher);
            descInput.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
            codeInput.setOnEditorActionListener((v, actionId, onImeEvent) -> {
                if(codeInput.focusSearch(View.FOCUS_DOWN) != null)
                    codeInput.focusSearch(View.FOCUS_DOWN).requestFocus();
                return true;
            });
        }

        if(annexDescInputWatcher == null){
            annexDescInputWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override
                public void afterTextChanged(Editable s) {
                    if(s.toString().length() > Constants.INPUT_MAX_CHARACTERS_LIMIT){
                        context.getUXToolkit().showToast(R.string.max_character_limit_exceeded_msg);
                        descInput.setText(s.toString().substring(0, Constants.INPUT_MAX_CHARACTERS_LIMIT));
                        descInput.setSelection(Constants.INPUT_MAX_CHARACTERS_LIMIT);
                        return;
                    }

                    if(crossEditInfiniteLoopHack)
                        return;

                    crossEditInfiniteLoopHack = true;

                    if(!s.toString().isEmpty()) {
                        if(codes != null && codes.containsKey(s.toString())) {
                            codeInput.setText(getCode(s.toString()));
                            if(mAnswers[1] == null)
                                mAnswers[1] = new ValueStore(getCode(s.toString()));
                            else
                                mAnswers[1].setValue(getCode(s.toString()));

                            if(mAnswers[0] == null)
                                mAnswers[0] = new ValueStore(s.toString());
                            else
                                mAnswers[0].setValue(s.toString());

                            if(onAnswerEventLambda != null)
                                onAnswerEventLambda.run();
                        }else {
                            mAnswers[0] = null;
                            mAnswers[1] = null;
                            codeInput.setText("");
                        }
                    }else {
                        mAnswers[0] = null;
                        mAnswers[1] = null;
                        codeInput.setText("");
                    }

                    crossEditInfiniteLoopHack = false;
                }
            };
            descInput.addTextChangedListener(annexDescInputWatcher);
            descInput.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
            descInput.setOnEditorActionListener((v, actionId, onImeEvent) -> {
                if(descInput.focusSearch(View.FOCUS_RIGHT) != null)
                    descInput.focusSearch(View.FOCUS_RIGHT).requestFocus();
                return true;
            });
        }

        if(context.getLabelProvider().hasHint(getIndex()))
            descInput.setHint(context.getLabelProvider().getHint(getIndex()));
    }

    @Override
    public void setupImeAction(NavigationToolkit nToolkit) {
        codeInput.setImeActionLabel("ASK NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        codeInput.setOnEditorActionListener((v, actionId, event) -> {
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
    public void reset() {
        if(codeInput != null){
            codeInput.setText("");
        }
    }

    @Override
    public boolean requestFocus() {
        if (descInput != null) {
            getInputView().post(()->{
                ((ActivityCustom) getInputView().getContext())
                        .getUXToolkit().showKeyboardTo(descInput);
            });
            return descInput.requestFocus();
        }
        return false;
    }

    @Override
    public boolean hasFocus() {
        if(descInput != null)
            return descInput.hasFocus() || codeInput.hasFocus();
        return false;
    }


    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        LinearLayout item;
        if(getInputView() == null) {
            item = (LinearLayout) inflater.inflate(getResId(), parent, false);

            Spanned htm = Html.fromHtml(labels.getLabel(getIndex()));
            TextView tvLabel = item.findViewById(R.id.tv);
            ThemeUtils.setupTextViewStylesByLocale(labels.getLocale(), tvLabel);
            tvLabel.setText(htm);

            codeInput = item.findViewById(R.id.kbi_2);
            codeInput.setHint(R.string.hint_annex_code);
            codeInput.setInputType(InputType.TYPE_CLASS_NUMBER);

            descInput = item.findViewById(R.id.kbi_1);
            descInput.setHint(R.string.hint_search_annex);
            setInputView(item);
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }
}
