package pk.gov.pbs.formbuilder.inputs.singular;

import android.app.DatePickerDialog;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.utils.ThemeUtils;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.validator.BetweenValidator;
import pk.gov.pbs.formbuilder.validator.Validator;
import pk.gov.pbs.utils.DateFormatter;

public class DateInput extends SingularInput {
    protected static DatePickerDialog datePickerDialog;
    private final static KeyboardInput[] mCandidateAskables = new KeyboardInput[3];
    protected LinearLayout inputElement;
    protected KeyboardInput[] mKeyboardInputs;
    protected View mBtnCalendar;
    protected Validator mValidator;

    public DateInput(String index) {
        super(index, R.layout.input_date_picker);
        mKeyboardInputs = new KeyboardInput[3];
        mKeyboardInputs[0] = new KeyboardInput(index + 'a', InputType.TYPE_CLASS_NUMBER, new BetweenValidator(new ValueStore(1), new ValueStore(31)));
        mKeyboardInputs[1] = new KeyboardInput(index + 'b', InputType.TYPE_CLASS_NUMBER, new BetweenValidator(new ValueStore(1), new ValueStore(12)));
        mKeyboardInputs[2] = new KeyboardInput(index + 'c', InputType.TYPE_CLASS_NUMBER, new BetweenValidator(new ValueStore(1900), new ValueStore(2100)));
        mAnswers = new ValueStore[3];
    }

    public DateInput(String index, Validator validator){
        this(index);
        mValidator = validator;
    }

    @Override
    public boolean hasAnswer(){
        return mAnswers[0] != null && mAnswers[1] != null && mAnswers[2] != null;
    }

    public boolean validateAnswer() {
        if(mValidator != null)
            return true;

        if (hasAnswer()) {
            return mKeyboardInputs[0].validateAnswer()
                    && mKeyboardInputs[1].validateAnswer()
                    && mKeyboardInputs[2].validateAnswer();
        }

        return false;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if(getInputView() != null){

            if (mAnswers[0] == null || !nse(mAnswers[0], mKeyboardInputs[0].getAnswer())) {
                mKeyboardInputs[0].setAnswer(mAnswers[0]);
                mKeyboardInputs[0].loadAnswerIntoInputView(viewModel);
            }

            if (mAnswers[1] == null || !nse(mAnswers[1], mKeyboardInputs[1].getAnswer())) {
                mKeyboardInputs[1].setAnswer(mAnswers[1]);
                mKeyboardInputs[1].loadAnswerIntoInputView(viewModel);
            }

            if (mAnswers[2] == null || !nse(mAnswers[2], mKeyboardInputs[2].getAnswer())) {
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
            mBtnCalendar.setEnabled(false);
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
            mBtnCalendar.setEnabled(true);
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
    public LinearLayout getInputView() {
        return inputElement;
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

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEventLambda) {
        for (int i=0; i < mKeyboardInputs.length; i++) {
            int index = i;
            mKeyboardInputs[i].bindListeners(context, () -> {
                setAnswer(index, mKeyboardInputs[index].getAnswer());
                if (onAnswerEventLambda != null)
                    onAnswerEventLambda.run();
            });

            EditText kbi = mKeyboardInputs[index].getInputView();
            kbi.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_DONE | EditorInfo.IME_ACTION_GO | EditorInfo.IME_ACTION_NEXT);
            if (i==2) {
                kbi.setOnEditorActionListener((v, actionId, event)->{
                    if(kbi.focusSearch(View.FOCUS_DOWN) != null)
                        kbi.focusSearch(View.FOCUS_DOWN).requestFocus();
                    else
                        context.getUXToolkit().hideKeyboardFrom(kbi);

                    return true;
                });
            } else {
                kbi.setOnEditorActionListener((v, actionId, event) -> {
                    if (kbi.focusSearch(View.FOCUS_RIGHT) != null)
                        kbi.focusSearch(View.FOCUS_RIGHT).requestFocus();
                    return true;
                });
            }
        }

        if (datePickerDialog == null) {
            int year, month, day;
            year = DateFormatter.calendar.get(Calendar.YEAR);
            month = DateFormatter.calendar.get(Calendar.MONTH);
            day = DateFormatter.calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog.OnDateSetListener listener = (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                mCandidateAskables[0].loadAnswerIntoInputView(context.getViewModel(), new ValueStore(selectedDayOfMonth));
                mCandidateAskables[1].loadAnswerIntoInputView(context.getViewModel(), new ValueStore(selectedMonth + 1));
                mCandidateAskables[2].loadAnswerIntoInputView(context.getViewModel(), new ValueStore(selectedYear));
            };

            datePickerDialog = new DatePickerDialog(context, listener, year, month, day);
            datePickerDialog.setCanceledOnTouchOutside(false);
        }

        mBtnCalendar.setOnClickListener(v -> {
            mKeyboardInputs[0].getInputView().requestFocus();
            context.getUXToolkit().hideKeyboardFrom(mKeyboardInputs[0].getInputView());
            for (int i = 0; i < mKeyboardInputs.length; i++) {
                mKeyboardInputs[i].getInputView().clearFocus();
                mCandidateAskables[i] = mKeyboardInputs[i];
            }
            datePickerDialog.show();
        });
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labelProvider, ViewGroup parent) {
        LinearLayout item;
        if(getInputView() == null) {
            item = (LinearLayout) inflater.inflate(getResId(), parent, false);

            mBtnCalendar = item.findViewById(R.id.btn_calendar);
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

            String lblA = labelProvider.getLabel(getIndex()+'a');
            String lblB = labelProvider.getLabel(getIndex()+'b');
            String lblC = labelProvider.getLabel(getIndex()+'c');

            if (lblA != null)
                tv_1.setText(Html.fromHtml(lblA));

            EditText kbi_1 = mKeyboardInputs[0].getInputView();
            kbi_1.setOnFocusChangeListener((v, hasFocus) -> {
                int colorCode = (hasFocus) ? ThemeUtils.getColorByTheme(inflater.getContext(), R.attr.colorAccent)
                        : ThemeUtils.getColorByTheme(inflater.getContext(), R.attr.colorTextDim);

                tv_1.setTextColor(colorCode);
                kbi_1.setHintTextColor(colorCode);
                kbi_1.setTextColor(colorCode);

            });

            if (lblB != null)
                tv_2.setText(Html.fromHtml(lblB));

            EditText kbi_2 = mKeyboardInputs[1].getInputView();
            kbi_2.setOnFocusChangeListener((v, hasFocus) -> {
                int colorCode = (hasFocus) ? ThemeUtils.getColorByTheme(inflater.getContext(), R.attr.colorAccent)
                        : ThemeUtils.getColorByTheme(inflater.getContext(), R.attr.colorTextDim);

                tv_2.setTextColor(colorCode);
                kbi_2.setHintTextColor(colorCode);
                kbi_2.setTextColor(colorCode);
            });

            if (lblC != null)
                tv_3.setText(Html.fromHtml(lblC));

            EditText kbi_3 = mKeyboardInputs[2].getInputView();
            kbi_3.setOnFocusChangeListener((v, hasFocus) -> {
                int colorCode = (hasFocus) ? ThemeUtils.getColorByTheme(inflater.getContext(), R.attr.colorAccent)
                        : ThemeUtils.getColorByTheme(inflater.getContext(), R.attr.colorTextDim);

                tv_3.setTextColor(colorCode);
                kbi_3.setHintTextColor(colorCode);
                kbi_3.setTextColor(colorCode);
            });

            inputElement = item;
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
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

}
