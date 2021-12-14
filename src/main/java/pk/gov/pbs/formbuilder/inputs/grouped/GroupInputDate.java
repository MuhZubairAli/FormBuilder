package pk.gov.pbs.formbuilder.inputs.grouped;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.inputs.singular.DateInput;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.validator.Validator;

public class GroupInputDate extends GroupInputLinearLayout {
    protected DateInput mDateInput;

    public GroupInputDate(String index, Validator validator) {
        super(index, R.layout.input_group_date);
        mValidator = validator;
        mDateInput = new DateInput(index, validator);
        mAnswers = new ValueStore[3];
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        return mDateInput.loadAnswerIntoInputView(viewModel);
    }

    @Override
    public boolean hasAnswer() {
        return mDateInput.hasAnswer();
    }

    @Override
    public boolean lock() {
        return mDateInput.lock();
    }

    @Override
    public boolean unlock() {
        return mDateInput.unlock();
    }

    @Override
    public void reset() {
        mDateInput.reset();
    }

    @Override
    public boolean requestFocus() {
        return mDateInput.requestFocus();
    }

    @Override
    public boolean hasFocus() {
        return mDateInput.hasFocus();
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {
        mDateInput.bindListeners(context, () -> {
            for (int i = 0; i < mAnswers.length; i++)
                mAnswers = mDateInput.getAnswers();

            if (onAnswerEvent != null)
                onAnswerEvent.run();
        });
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        LinearLayout item;
        if(getInputView() == null) {
            item = (LinearLayout) inflater.inflate(getResId(), parent, false);

            Spanned htm = Html.fromHtml(labels.getLabel(getIndex()));
            ((TextView) item.findViewById(R.id.tv)).setText(htm);

            LinearLayout container = item.findViewById(R.id.container_date_input);
            mDateInput.inflate(inflater, labels, container);

            setInputView(item);
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {
        mDateInput.setupImeAction(toolkit);
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        mDateInput.exportAnswer(model);
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        return mDateInput.importAnswer(model);
    }

    @Override
    public boolean validateAnswer() {
        return mDateInput.validateAnswer();
    }
}
