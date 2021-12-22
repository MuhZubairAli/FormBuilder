package pk.gov.pbs.formbuilder.inputs.singular;

import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.utils.FormBuilderThemeHelper;

public abstract class Selectable extends SingularInput {
    protected Selectable.OnSetAnswerEvent onAnswersEvent;
    protected ValueStore value;

    protected Selectable(String index, int resId) {
        super(index, resId);
    }
    public Selectable(String index, @NonNull ValueStore value, int resId){
        this(index,resId);
        this.value = value;
    }

    @Override
    public abstract CompoundButton getInputView();

    public boolean setAnswerAsChecked(){
        return setAnswer(value);
    }

    public void setOnAnswerEventListener(OnSetAnswerEvent eventListener){
        this.onAnswersEvent = eventListener;
    }

    @Override
    public boolean setAnswers(ValueStore... answers) {
        if (onAnswersEvent != null)
            onAnswersEvent.onAnswer(mAnswers, answers);
        return super.setAnswers(answers);
    }

    @Override
    public boolean setAnswer(ValueStore answer) {
        if (onAnswersEvent != null)
            onAnswersEvent.onAnswer(mAnswers, new ValueStore[]{answer});

        return super.setAnswer(answer);
    }

    public ValueStore getValue() {
        return value;
    }

    @Override
    public boolean lock() {
        if (!IS_UNLOCKED)
            return true;

        if(getInputView() != null){
            if(!getInputView().isChecked()) {
                FormBuilderThemeHelper.applyThemedDrawableToView(getInputView(), R.attr.bgSelectableLockedUnanswered);
            }else {
                FormBuilderThemeHelper.applyThemedDrawableToView(getInputView(), R.attr.bgSelectableLocked);
            }
            getInputView().setClickable(false);
            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if (IS_UNLOCKED)
            return true;

        if(getInputView() != null){
            FormBuilderThemeHelper.applyThemedDrawableToView(getInputView(), R.attr.bgSelectableUnlocked);
            getInputView().setClickable(true);
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean requestFocus() {
        if(getInputView() != null)
            return getInputView().requestFocus();
        return false;
    }

    @Override
    public boolean hasFocus() {
        if (getInputView() != null)
            return getInputView().hasFocus();
        return false;
    }

    @Override
    public boolean hasAnswer() {
        return getAnswer() != null;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent){
        getInputView().setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setAnswer(this.value);
            } else {
                setAnswer(null);
            }

            if (onAnswerEvent != null)
                onAnswerEvent.run();
        });
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if(getInputView() != null && value.equalsIgnoreCase(getAnswer())) {
            if (!getInputView().isChecked())
                getInputView().setChecked(true);
            return true;
        }
        return false;
    }

    /**
     * This event will be fired when answer is set i,e set, update, deleted
     * This will only be called when answer is set via setAnswers method of selectable
     *
     */
    public interface OnSetAnswerEvent {
        void onAnswer(ValueStore[] oldAnswers, ValueStore[] newAnswers);
    }
}
