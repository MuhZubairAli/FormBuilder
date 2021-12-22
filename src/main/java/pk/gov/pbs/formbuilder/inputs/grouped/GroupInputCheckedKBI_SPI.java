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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.utils.FormBuilderThemeHelper;
import pk.gov.pbs.formbuilder.validator.Validator;
import pk.gov.pbs.utils.ThemeUtils;

public class GroupInputCheckedKBI_SPI extends GroupInputTableRow {
    protected EditText kbi;
    protected CheckBox cbi;
    protected Spinner spi;
    protected ArrayList<String> spinnerOptions;
    protected int mInputType;

    public GroupInputCheckedKBI_SPI(String index, String... extras) {
        super(index, R.layout.input_group_cbi_kbi_spi, extras);
        mAnswers = new ValueStore[3];
    }

    public GroupInputCheckedKBI_SPI(String index, int inputType, String... extras) {
        this(index, extras);
        mInputType = inputType;
    }

    public GroupInputCheckedKBI_SPI(String index, int inputType, ArrayList<String> spinnerOptions, String... extras) {
        this(index, inputType, extras);
        this.spinnerOptions = spinnerOptions;
    }

    public GroupInputCheckedKBI_SPI(String index, Validator validator, String... extras) {
        this(index, extras);
        super.mValidator = validator;
    }

    public GroupInputCheckedKBI_SPI(String index, int inputType, Validator validator, String... extras) {
        this(index, inputType, extras);
        super.mValidator = validator;
    }

    public GroupInputCheckedKBI_SPI(String index, int inputType, ArrayList<String> spinnerOptions, Validator validator, String... extras) {
        this(index, inputType, validator, extras);
        this.spinnerOptions = spinnerOptions;
    }

    @Override
    public boolean hasAnswer() {
        return mAnswers[0] != null && (mAnswers[0].toInt()==2 || (mAnswers[0].toInt()==1 && mAnswers[1]!=null && mAnswers[2]!=null));
    }

    @Override
    public boolean setAnswers(ValueStore... answers) {
        if (answers != null && answers.length > 0 && answers[0] != null){
            if (answers[0].toInt()==2 || (answers[0].toInt()==1 && answers[1]!=null && answers[2]!=null)) {
                if (answers[0].toInt()==2)
                    return super.setAnswers(answers[0],null,null);
                return super.setAnswers(answers);
            }
        }
        return false;
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
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if(mAnswers[0].toInt() == 1){
            model.set(getIndex()+"a", mAnswers[0]);
            model.set(getIndex()+"b", mAnswers[1]);
            model.set(getIndex()+"c", mAnswers[2]);
        } else
            model.set(getIndex()+"a", mAnswers[0]);
        model.set(getIndex()+"d", getExtra(0));
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if (model.get(getIndex() + "a") != null) {
            mAnswers[0] = model.get(getIndex() + "a");
            mAnswers[1] = model.get(getIndex() + "b");
            mAnswers[2] = model.get(getIndex() + "c");
            return true;
        }
        return false;
    }

    public void setKeyboardAnswer(String value){
        if(kbi != null){
            kbi.setText(value);
            kbi.setSelection(value.length());
            if(mAnswers[1] == null)
                mAnswers[1] = new ValueStore(value);
            else
                mAnswers[1].setValue(value);
        }
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if(cbi != null){
            if(mAnswers[0] != null) {
                cbi.setChecked(mAnswers[0].toInt() == 1);
                if (mAnswers[1] != null && !mAnswers[1].isEmpty())
                    kbi.setText(mAnswers[1].toString());

                if (mAnswers[2] != null && mAnswers[2].toInt() != 0)
                    spi.setSelection(mAnswers[2].toInt());

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean lock() {
        if(cbi != null){
            if(!IS_UNLOCKED)
                return true;

            if(cbi.isChecked()) {
                kbi.setEnabled(false);
                spi.setEnabled(false);
            }

            lockItem(cbi);
            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if(cbi != null){
            if(IS_UNLOCKED)
                return true;

            if(cbi.isChecked()) {
                kbi.setEnabled(true);
                spi.setEnabled(true);
            }
            unlockItem(cbi);
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    private void lockItem(CheckBox item){
        if(!item.isChecked()) {
            FormBuilderThemeHelper.applyThemedDrawableToView(item, R.attr.bgSelectableLockedUnanswered);
        }else {
            FormBuilderThemeHelper.applyThemedDrawableToView(item, R.attr.bgSelectableLocked);
        }
        item.setClickable(false);
    }

    private void unlockItem(CheckBox item){
        FormBuilderThemeHelper.applyThemedDrawableToView(item, R.attr.bgSelectableUnlocked);
        item.setClickable(true);
    }

    @Override
    public boolean requestFocus() {
        if(cbi.isChecked())
            return kbi.requestFocus();
        return false;
    }

    @Override
    public void bindListeners(ActivityFormSection context, @Nullable Runnable onAnswerEventLambda) {
        cbi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                spi.setEnabled(true);
                kbi.setEnabled(true);
                kbi.requestFocus();

                if (mAnswers[0] == null)
                    mAnswers[0] = new ValueStore(1);
                else
                    mAnswers[0].setValue(1);

            } else {
                kbi.setText("");
                kbi.setEnabled(false);
                spi.setSelection(0);
                spi.setEnabled(false);

                if (mAnswers[0] == null)
                    mAnswers[0] = new ValueStore(2);
                else
                    mAnswers[0].setValue(2);
            }

            if(onAnswerEventLambda != null)
                onAnswerEventLambda.run();
        });

        kbi.setEnabled(false);
        kbi.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (kbi.getInputType() == InputType.TYPE_CLASS_NUMBER && s.toString().length() > Constants.INPUT_MAX_NUMBERS_LIMIT) {
                    context.getUXToolkit().showToast(R.string.max_digit_limit_exceeded_msg);
                    kbi.setText(s.toString().substring(0, Constants.INPUT_MAX_NUMBERS_LIMIT));
                    kbi.setSelection(Constants.INPUT_MAX_NUMBERS_LIMIT);
                    return;

                } else if (kbi.getInputType() == InputType.TYPE_CLASS_TEXT && s.toString().length() > Constants.INPUT_MAX_CHARACTERS_LIMIT) {
                    context.getUXToolkit().showToast(R.string.max_character_limit_exceeded_msg);
                    kbi.setText(s.toString().substring(0, Constants.INPUT_MAX_CHARACTERS_LIMIT));
                    kbi.setSelection(Constants.INPUT_MAX_CHARACTERS_LIMIT);
                    return;
                }

                if(!s.toString().isEmpty() && mValidator != null && !mValidator.isValid(new ValueStore(s.toString())))
                    kbi.setError(mValidator.getRuleStatement());

                if(!s.toString().isEmpty()) {
                    if (mAnswers[1] == null) {
                        mAnswers[1] = new ValueStore(s.toString());
                    } else {
                        mAnswers[1].setValue(s.toString());
                    }
                }else
                    mAnswers[1] = null;

                if(onAnswerEventLambda != null)
                    onAnswerEventLambda.run();
            }
        });

        spi.setEnabled(false);
        spi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mAnswers[2]==null)
                    mAnswers[2]=new ValueStore(id);
                else
                    mAnswers[2].setValue(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setupImeAction(context.getNavigationToolkit());
        if(mAnswers[0] == null) {
            mAnswers[0] = new ValueStore(2);
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
        if(getInputView() != null){
            cbi.setChecked(false);
            spi.setSelection(0);
        }
    }

    @Override
    public boolean hasFocus() {
        if(getInputView() != null)
            return kbi.hasFocus();
        return false;
    }

    public void inflate(LayoutInflater inflater, LabelProvider labelProvider, ViewGroup container){
        TableRow item;
        if(getInputView() == null) {
            item = (TableRow) inflater.inflate(getResId(), container, false);

            Spanned htm = Html.fromHtml(labelProvider.getLabel(getIndex()));
            cbi = item.findViewById(R.id.cbi);
            ThemeUtils.setupTextViewStylesByLocale(labelProvider.getLocale(), cbi);
            cbi.setText(htm);


            if(spinnerOptions == null || spinnerOptions.size() == 0) {
                spinnerOptions = new ArrayList<>();
                spinnerOptions.add("No Option Provided");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(inflater.getContext(), R.layout.item_list_sp, spinnerOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spi = item.findViewById(R.id.spi);
            spi.setAdapter(adapter);

            kbi =  item.findViewById(R.id.kbi);
            if (mInputType != 0)
                kbi.setInputType(mInputType);
            if (labelProvider.hasHint(getIndex()+"_kbi"))
                kbi.setHint(labelProvider.getHint(getIndex()+"_kbi"));
            setInputView(item);
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        container.addView(item);
    }
}
