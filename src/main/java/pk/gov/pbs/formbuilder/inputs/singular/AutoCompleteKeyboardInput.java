package pk.gov.pbs.formbuilder.inputs.singular;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import pk.gov.pbs.database.DatabaseUtils;
import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.core.ActivityCustom;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.Option;
import pk.gov.pbs.formbuilder.pojos.DatumIdentifier;
import pk.gov.pbs.formbuilder.pojos.OptionTuple;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.validator.Validator;
import pk.gov.pbs.utils.ThemeUtils;

public class AutoCompleteKeyboardInput extends SingularInput {
    private final DatumIdentifier mDatumIdentifier;

    private AutoCompleteTextView inputElement;
    private ArrayAdapter<String> mInputAdapter;
    private HashMap<String,Long> mOptionsMap;
    private List<String> mSuggestionList;

    private final Validator mValidator;
    private TextWatcher mInputWatcher;

    public AutoCompleteKeyboardInput(String index, DatumIdentifier datumIdentifier, Validator validator) {
        super(index, R.layout.input_actv);
        mAnswers = new ValueStore[2];
        mDatumIdentifier = datumIdentifier;
        mValidator = validator;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if(getInputView() != null && hasAnswer()){
            inputElement.setText(getAnswer().toString());
            return true;
        }
        return false;
    }

    @Override
    public boolean hasAnswer() {
        return mAnswers[0] != null && mAnswers[1] != null;
    }

    @Override
    public boolean lock() {
        if (getInputView() != null) {
            if (!IS_UNLOCKED)
                return true;

            inputElement.setEnabled(false);
            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if (getInputView() != null) {
            if (IS_UNLOCKED)
                return true;

            inputElement.setEnabled(true);
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean requestFocus() {
        if(getInputView() != null) {
            getInputView().post(()->{
                ((ActivityCustom) getInputView().getContext())
                        .getUXToolkit().showKeyboardTo(getInputView());
            });
            return getInputView().requestFocus();
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
    public AutoCompleteTextView getInputView() {
        return inputElement;
    }

    @Override
    @SuppressLint("WrongConstant")
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {
        if (mOptionsMap == null || mSuggestionList == null){
            mOptionsMap = new HashMap<>();
            mSuggestionList = new ArrayList<>();

            List<OptionTuple> tuples = context.getViewModel()
                    .getFormBuilderRepository()
                    .getOptionsDao()
                    .getOptionsByIdentifier(mDatumIdentifier);

            if (tuples != null) {
                for (OptionTuple ot : tuples) {
                    if (ot.sid == null)
                        mOptionsMap.put(ot.desc, ot.aid);
                    else
                        mOptionsMap.put(ot.desc, ot.sid);

                    mSuggestionList.add(ot.desc);
                }
            }
        }

        if(mInputWatcher == null){
            mInputWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override
                public void afterTextChanged(Editable s) {
                    if(s.toString().length() > Constants.INPUT_MAX_CHARACTERS_LIMIT){
                        context.getUXToolkit().showToast(R.string.max_character_limit_exceeded_msg);
                        inputElement.setText(s.toString().substring(0, Constants.INPUT_MAX_CHARACTERS_LIMIT));
                        inputElement.setSelection(Constants.INPUT_MAX_CHARACTERS_LIMIT);
                        return;
                    }

                    if (mValidator != null){
                        if (!mValidator.isValid(new ValueStore(s.toString()))){
                            inputElement.setError(mValidator.getErrorStatement());
                            return;
                        }
                    }

                    if(!s.toString().isEmpty()) {
                        if(mOptionsMap != null && mOptionsMap.containsKey(s.toString()))
                            setAnswers(new ValueStore(s.toString()), new ValueStore(mOptionsMap.get(s.toString())));
                        else
                            setAnswers(new ValueStore(s.toString()), null);
                    }else {
                        setAnswers(null, null);
                    }

                    if (onAnswerEvent != null)
                        onAnswerEvent.run();
                }
            };

            inputElement.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && mAnswers[0] != null){
                    int strLength = mAnswers[0].toString().length();
                    if (strLength > 0) {
                        inputElement.post(()->{
                            if (strLength < 3) {
                                inputElement.setError("Text must be at least 3 characters long");
                                v.requestFocus();
                                context.getUXToolkit().showKeyboardTo(v);
                            } else if (mValidator != null && !mValidator.isValid(mAnswers[0])) {
                                inputElement.setError(mValidator.getErrorStatement());
                                v.requestFocus();
                                context.getUXToolkit().showKeyboardTo(v);
                            } else {
                                //If new option is entered store it to data dictionary w.r.t DatumIdentifier
                                if (mAnswers[0] != null && !mAnswers[0].isEmpty() && mAnswers[1] == null){
                                    Long selectedOptionId = mOptionsMap.get(mAnswers[0].toString());
                                    if(selectedOptionId != null) {
                                        setAnswer(1, new ValueStore(selectedOptionId));
                                    } else{
                                        Future<Long> insert = context.getViewModel().getFormBuilderRepository()
                                                .insert(new Option(mDatumIdentifier, mAnswers[0].toString()));
                                        long insertId = DatabaseUtils.getFutureValue(insert);
                                        mOptionsMap.put(mAnswers[0].toString(), insertId);
                                        mInputAdapter.add(mAnswers[0].toString());
                                        setAnswer(1, new ValueStore(insertId));
                                    }
                                }
                            }
                        });
                    }
                }
            });

            mInputAdapter = new ArrayAdapter<>(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    mSuggestionList
            );
            inputElement.setAdapter(mInputAdapter);
            inputElement.addTextChangedListener(mInputWatcher);

            getInputView().setImeActionLabel("NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
            getInputView().setOnEditorActionListener((v, actionId, event) -> {
                if (getInputView().focusSearch(View.FOCUS_FORWARD) != null)
                    getInputView().focusSearch(View.FOCUS_FORWARD).requestFocus();
                return true;
            });
        }

    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {
        getInputView().setImeActionLabel("ASK NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        getInputView().setOnEditorActionListener((v, actionId, event) -> {
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
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        AutoCompleteTextView item;
        if(getInputView() == null) {
            item = (AutoCompleteTextView) inflater.inflate(getResId(), parent, false);
            if (labels.hasHint(getIndex()+"_kbi")) {
                item.setHint(labels.getHint(getIndex() + "_kbi"));
                //ThemeUtils.setupTextViewStylesByLocale(labels.getLocale(), item);
            }

            item.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

            inputElement = item;
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        model.set(getIndex(), mAnswers[0]);
        model.set("__" + getIndex(), mAnswers[1]);
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if (model.get(getIndex()) != null) {
            mAnswers[0] = model.get(getIndex());
            mAnswers[1] = model.get("__" + getIndex());
            return true;
        }
        return false;
    }
}
