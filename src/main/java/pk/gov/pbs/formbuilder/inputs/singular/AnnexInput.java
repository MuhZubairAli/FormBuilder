package pk.gov.pbs.formbuilder.inputs.singular;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;

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
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.pojos.Annex;
import pk.gov.pbs.formbuilder.pojos.DatumIdentifier;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.validator.Validator;

public class AnnexInput extends SingularInput {
    private DatumIdentifier identifier;
    private boolean crossEditInfiniteLoopHack = false;

    private AutoCompleteTextView annexDescInput;
    private EditText annexCodeInput;

    private HashMap<String, String> codes; // desc -> code
    private List<String> options;

    private Validator mValidator;
    private TextWatcher annexCodeInputWatcher, inputElementWatcher;

    private AnnexInput(String index){
        super(index, R.layout.input_actv);
        mAnswers = new ValueStore[2];
    }

    public AnnexInput(String index, DatumIdentifier identifier){
        this(index);
        this.identifier = identifier;
        options = new ArrayList<>();
    }

    public AnnexInput(String index, DatumIdentifier identifier, Validator validator){
        this(index, identifier);
        mValidator = validator;
    }

    public DatumIdentifier getIdentifier(){
        return identifier;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel){
        if (hasAnswer()){
            annexDescInput.setText(getAnswer(1).toString());
            //only set either desc or code other will be determined automatically
            //because at this point listeners have been added to inputs
            //annexCodeInput.setText(getAnswer(0).toString());
            return true;
        }
        return false;
    }

    public boolean setupLoadedAnswer(String desc){
        if(annexDescInput != null){
            annexDescInput.setText(desc);
            annexCodeInput.setText(codes.get(desc));
            return true;
        }
        return false;
    }

    @Override
    public AutoCompleteTextView getInputView() {
        return annexDescInput;
    }

    public EditText getAnnexCodeInput(){
        return annexCodeInput;
    }

    @Override
    public boolean lock() {
        if(annexDescInput != null){
            if (!IS_UNLOCKED)
                return true;

            annexDescInput.setEnabled(false);
            annexCodeInput.setEnabled(false);
            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if(annexDescInput != null){
            if (IS_UNLOCKED)
                return true;

            annexDescInput.setEnabled(true);
            annexCodeInput.setEnabled(true);
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    public ValueStore getAnnexDesc() {
        if(annexDescInput != null)
            return new ValueStore(annexDescInput.getText().toString());
        return null;
    }

    public String getAnnexCode(@NonNull String annex){
        return codes.get(annex);
    }

    public String getAnnexDescFromCode(@NonNull String code){
        for (String desc : codes.keySet()){
            if (codes.get(desc).equalsIgnoreCase(code))
                return desc;
        }
        return null;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent){
        if(options.size() == 0) {
            List<Annex> annexes = context.getViewModel().getAnnexures(identifier);
            codes = new HashMap<>();
            if (annexes.size() > 0) {
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

            annexDescInput.setAdapter(adapter);
        }

        if(annexCodeInputWatcher == null) {
            annexCodeInputWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    if(s.toString().length() > Constants.INPUT_MAX_NUMBERS_LIMIT) {
                        context.getUXToolkit().showToast(R.string.max_digit_limit_exceeded_msg);
                        annexCodeInput.setText(s.toString().substring(0, Constants.INPUT_MAX_NUMBERS_LIMIT));
                        annexCodeInput.setSelection(Constants.INPUT_MAX_NUMBERS_LIMIT);
                        return;
                    }

                    if(crossEditInfiniteLoopHack)
                        return;
                    crossEditInfiniteLoopHack = true;

                    if(mValidator != null) {
                        if(!s.toString().isEmpty() && !mValidator.isValid(new ValueStore(s.toString())))
                            annexCodeInput.setError(mValidator.getRuleStatement());
                    }

                    String desc = null;
                    if(codes.containsValue(s.toString())){
                        for(String lbl : codes.keySet()){
                            if(codes.get(lbl).equalsIgnoreCase(s.toString())){
                                desc = lbl;
                                setAnswers(new ValueStore(s.toString()), new ValueStore(desc));
                                break;
                            }
                        }
                    }

                    if(desc != null){
                        annexDescInput.setText(desc);
                    } else {
                        annexDescInput.setText("");
                        setAnswers(null, null);
                    }

                    crossEditInfiniteLoopHack = false;
                }
            };

            annexCodeInput.addTextChangedListener(annexCodeInputWatcher);
        }

        if(inputElementWatcher == null){
            inputElementWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override
                public void afterTextChanged(Editable s) {
                    if(s.toString().length() > Constants.INPUT_MAX_CHARACTERS_LIMIT){
                        context.getUXToolkit().showToast(R.string.max_character_limit_exceeded_msg);
                        annexDescInput.setText(s.toString().substring(0, Constants.INPUT_MAX_CHARACTERS_LIMIT));
                        annexDescInput.setSelection(Constants.INPUT_MAX_CHARACTERS_LIMIT);
                        return;
                    }

                    if(crossEditInfiniteLoopHack)
                        return;
                    crossEditInfiniteLoopHack = true;

                    if(!s.toString().isEmpty()) {
                        if(codes != null && codes.containsKey(s.toString())) {
                            setAnswers(new ValueStore(getAnnexCode(s.toString())), new ValueStore(s.toString()));
                            annexCodeInput.setText(getAnnexCode(s.toString()));
                        }else {
                            setAnswers(null,null);
                            annexCodeInput.setText("");
                        }
                    }else {
                        setAnswers(null, null);
                        annexCodeInput.setText("");
                    }

                    crossEditInfiniteLoopHack = false;
                }
            };
            annexDescInput.addTextChangedListener(inputElementWatcher);
        }

        if(context.getLabelProvider().hasHint(getIndex()+"_kbi_1"))
            annexDescInput.setHint(context.getLabelProvider().getLabel(getIndex()+"_kbi_1"));

        if(context.getLabelProvider().hasHint(getIndex()+"_kbi_2"))
            annexCodeInput.setHint(context.getLabelProvider().getLabel(getIndex()+"_kbi_2"));

        annexDescInput.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        annexDescInput.setOnEditorActionListener((v, actionId, event) -> annexCodeInput.requestFocus());
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
        if(getInputView() != null){
            getInputView().setText("");
            if (!isVisible())
                quickShow();
        }
    }

    @Override
    public boolean hasAnswer() {
        return getAnswers() != null && getAnswer(0) != null && getAnswer(1) != null;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        model.set(getIndex(), getAnswer(0));
        model.set(getIndex()+"_desc", getAnswer(1));
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if (model.get(getIndex()) != null) {
            setAnswers(model.get(getIndex()), model.get(getIndex() + "_desc"));
            return true;
        }
        return false;
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {
        getAnnexCodeInput().setImeActionLabel("ASK NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
        getAnnexCodeInput().setOnEditorActionListener((v, actionId, event) -> {
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
        if(getInputView() == null) {
            annexDescInput = (AutoCompleteTextView) inflater.inflate(getResId(), parent, false);
            annexDescInput.setHint(inflater.getContext().getString(R.string.hint_search_annex));

            annexCodeInput = (EditText) inflater.inflate(R.layout.input_et, parent, false);
            annexCodeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            annexCodeInput.setHint(R.string.hint_annex_code);
        }else {
            ((ViewGroup) annexDescInput.getParent()).removeView(annexDescInput);
            ((ViewGroup) annexCodeInput.getParent()).removeView(annexCodeInput);
        }

        parent.addView(annexDescInput);
        parent.addView(annexCodeInput);
    }
}
