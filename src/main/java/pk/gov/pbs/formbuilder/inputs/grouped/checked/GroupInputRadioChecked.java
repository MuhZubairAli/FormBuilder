package pk.gov.pbs.formbuilder.inputs.grouped.checked;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.GroupInput;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputLinearLayout;
import pk.gov.pbs.formbuilder.inputs.grouped.GroupInputRBI;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.utils.ThemeUtils;
import pk.gov.pbs.formbuilder.validator.Validator;
import pk.gov.pbs.utils.ExceptionReporter;

/**
 * This input is not fully generalized yet in it's layout
 * But it will work with any group input as SubgroupInput
 * This class is now friendly with Specifiable GroupInputRBI -> mRadioInput
 */
public class GroupInputRadioChecked extends GroupInputLinearLayout {
    private boolean mDoSetupIME = false;
    private int mAnswersLength;
    protected LinearLayout mContainerInputGroup;
    protected TextView tvCode, tvUnit;
    protected final GroupInputRBI mRadioInput;
    protected final GroupInput[] mSubInputs;
    protected final GroupInputCheckedPredicate mPredicate;

    public GroupInputRadioChecked(String index, GroupInputRBI radioInput, GroupInputCheckedPredicate predicate, GroupInput[] subInputs, Validator validator, String... extras){
        super(index, R.layout.input_group_radio_checked, validator, extras);
        mRadioInput = radioInput;
        mSubInputs = subInputs;
        mPredicate = predicate;
        mAnswersLength = radioInput.getAnswers().length; //zeroth answer
        mAnswers = new ValueStore[mAnswersLength];
        for (GroupInput gi : subInputs)
            mAnswersLength += gi.getAnswers().length;
    }

    public GroupInputRadioChecked(String index, GroupInputRBI radioInput, GroupInput[] subInputs, Validator validator, String... extras){
        this(index, radioInput, null, subInputs, validator, extras);
    }

    /**
     * This method tells if this askable is completely being displayed
     * this takes mPrdicate into account which determines whether or not subInput to be shown
     * by default if predicate is not present then if checkInput is checked then it is considered
     * the GroupInputChecked is complete
     * @return true if displaying all subInputs
     */
    public boolean isComplete(){
        if (mAnswers[0] != null)
            return  (mPredicate == null) ? mAnswers[0].toInt() == 1 : mPredicate.predicate(mAnswers);
        return false;
    }

    public GroupInput[] getSubInputs(){
        return mSubInputs;
    }

    @Override
    public ValueStore[] getAnswers() {
        ValueStore[] combinedAnswers = new ValueStore[mAnswersLength];
        System.arraycopy(mAnswers, 0, combinedAnswers, 0, mAnswers.length);

        int startCopyPos = mAnswers.length;
        for (GroupInput gi : mSubInputs) {
            ValueStore[] subAnswers = gi.getAnswers();
            System.arraycopy(
                    subAnswers,
                    0,
                    combinedAnswers,
                    startCopyPos,
                    subAnswers.length
            );
            startCopyPos += subAnswers.length;
        }

        return combinedAnswers;
    }

    @Override
    public boolean setAnswers(ValueStore... answers) {
        if (answers.length == mAnswersLength) {
            System.arraycopy(answers,0, mAnswers, 0, mAnswers.length);

            int startCopyPos = mAnswers.length;
            for (GroupInput gi : mSubInputs) {
                ValueStore[] subInputAnswers = new ValueStore[gi.getAnswers().length];
                System.arraycopy(
                        answers,
                        startCopyPos,
                        subInputAnswers,
                        0,
                        subInputAnswers.length
                );
                gi.setAnswers(subInputAnswers);
                startCopyPos += gi.getAnswers().length;
            }
            return true;
        }
        return false;
    }

    @Override
    public ValueStore getAnswer(int index) {
        if (index < mAnswers.length)
            return super.getAnswer(index);
        else {
            int targetIndex = index - mAnswers.length;
            for (int i = 0; i < mSubInputs.length; i++){
                if ((targetIndex - mSubInputs[i].getAnswers().length) > -1)
                    targetIndex -= mSubInputs[i].getAnswers().length;
                else {
                    return mSubInputs[i-1].getAnswer(targetIndex);
                }
            }
        }

        throw new IllegalArgumentException("invalid index " + index + " provided to get answer");
    }

    @Override
    public boolean setAnswer(int index, ValueStore answer) {
        if (index < mAnswersLength) {
            if (index < mAnswers.length)
                return super.setAnswer(index, answer);

            int targetIndex = index - mAnswers.length;
            for (int i = 0; i < mSubInputs.length; i++){
                if ((targetIndex - mSubInputs[i].getAnswers().length) > -1)
                    targetIndex -= mSubInputs[i].getAnswers().length;
                else {
                    return mSubInputs[i-1].setAnswer(targetIndex, answer);
                }
            }
        }

        throw new IllegalArgumentException("invalid index " + index + " provided to get answer");
    }

    @Override
    public boolean hasAnswer() {
        if (mAnswers[0] != null){
            ValueStore[] ans = mRadioInput.getAnswers();
            boolean proceed = (mPredicate == null) ?
                    ans != null && ans.length > 0 && ans[0] != null && ans[0].toInt() == 1
                    : mPredicate.predicate(mAnswers);

            if (proceed){
                boolean result = true;
                for (GroupInput gi : mSubInputs)
                    result &= gi.hasAnswer();
                return result;
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean validateAnswer() {
        // can't use canSkipValidate because it is possible that mSubInputs might be eligible
        // and this parent askable may not have validator, so only skip when not showing
        if(!isVisible())
            return true;

        boolean result = getValidator().isValid(mAnswers[0]);
        boolean predicate = (mPredicate == null) ? mAnswers[0] != null && mAnswers[0].toInt() == 1
                : mPredicate.predicate(mAnswers);
        if (predicate && result) {
            for (GroupInput gi : mSubInputs)
                result &= gi.validateAnswer();
            return result;
        }

        return result;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        mRadioInput.exportAnswer(model);

        if (hasExtras()) {
            char suffix = 'a';
            for (ValueStore vs : getExtras()){
                try {
                    model.set(getIndex()+ suffix++, vs);
                } catch (Exception e) {
                    ExceptionReporter.printStackTrace(e);
                }
            }

        }

        for (GroupInput gi : mSubInputs) {
            try {
                gi.exportAnswer(model);
            } catch (Exception e) {
                ExceptionReporter.printStackTrace(e);
            }
        }
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        mAnswers[0] = model.get(getIndex());
        boolean result = mAnswers[0] != null;

        if (mAnswers.length == 2) {
            mAnswers[1] = model.get("__" + getIndex());
            result &= mAnswers[1] != null;
        }

        if (result) {
            for (GroupInput gi : mSubInputs)
                result &= gi.importAnswer(model);
        }

        return result;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        GroupInputRBI.IGNORE_ASK_NEXT_QUESTION_CALL = true;
        boolean result = true;
        if(getInputView() != null){
            if (!nse(mAnswers[0], mRadioInput.getAnswer(0)))
                mRadioInput.setAnswer(0, mAnswers[0]);

            mRadioInput.loadAnswerIntoInputView(viewModel);
            boolean predicate = (mPredicate == null) ? mAnswers[0] != null && mAnswers[0].toInt() == 1
                    : mPredicate.predicate(mAnswers);

            if(predicate) {
                for (GroupInput gi : mSubInputs)
                    result &= gi.loadAnswerIntoInputView(viewModel);
            }
        }

        GroupInputRBI.IGNORE_ASK_NEXT_QUESTION_CALL = false;
        return result;
    }

    @Override
    public boolean lock() {
        if(getInputView() != null){
            if(!IS_UNLOCKED)
                return true;

            for (GroupInput gi : mSubInputs)
                gi.lock();

            mRadioInput.lock();

            if (tvCode != null && tvCode.getVisibility() == View.VISIBLE)
                ThemeUtils.applyThemedDrawableToView(tvCode, R.attr.bgSelectableLockedUnanswered);
            if (tvUnit != null && tvUnit.getVisibility() == View.VISIBLE)
                ThemeUtils.applyThemedDrawableToView(tvUnit, R.attr.bgSelectableLockedUnanswered);

            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if(getInputView() != null){
            if(IS_UNLOCKED)
                return true;

            for (GroupInput gi : mSubInputs)
                gi.unlock();

            mRadioInput.unlock();

            if (tvCode != null && tvCode.getVisibility() == View.VISIBLE)
                ThemeUtils.applyThemedDrawableToView(tvCode, R.attr.bgSelectableUnlocked);
            if (tvUnit != null && tvUnit.getVisibility() == View.VISIBLE)
                ThemeUtils.applyThemedDrawableToView(tvUnit, R.attr.bgSelectableUnlocked);

            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    @Override
    public void hideUnanswered() {
        super.hideUnanswered();
        ValueStore[] ans = mRadioInput.getAnswers();
        boolean proceed = (mPredicate == null) ?
                ans != null && ans.length > 0 && ans[0] != null && ans[0].toInt() == 1
                : mPredicate.predicate(mAnswers);

        if (proceed) {
            for (GroupInput gi : mSubInputs) {
                gi.hideUnanswered();
            }
        }
    }

    @Override
    public void showAll() {
        super.showAll();
        ValueStore[] ans = mRadioInput.getAnswers();
        boolean proceed = (mPredicate == null) ?
                ans != null && ans.length > 0 && ans[0] != null && ans[0].toInt() == 1
                : mPredicate.predicate(mAnswers);

        if (proceed) {
            for (GroupInput gi : mSubInputs) {
                gi.showAll();
            }
        }
    }

    @Override
    public boolean requestFocus() {
        if(mSubInputs.length > 0 && mSubInputs[0].getInputView() != null) {
            for (GroupInput gi : mSubInputs)
                if (gi.requestFocus())
                    return true;
        }
        return false;
    }

    @Override
    public void reset() {
        if (getInputView() != null){
            mRadioInput.reset();

            for (GroupInput gi : mSubInputs)
                gi.reset();
        }
    }

    @Override
    public boolean hasFocus() {
        if (mSubInputs.length > 0 && mSubInputs[0].getInputView() != null) {
            boolean result = false;
            for (GroupInput gi : mSubInputs)
                result |= gi.hasFocus();
            return result;
        }
        return false;
    }

    @Override
    public boolean hasIndex(String abIndex) {
        boolean result = getIndex().equalsIgnoreCase(abIndex) | mRadioInput.getIndex().equalsIgnoreCase(abIndex);
        for (GroupInput gi : mSubInputs) {
            if (result)
                return true;

            result = gi.hasIndex(abIndex);
        }
        return result;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {
        mRadioInput.bindListeners(context, ()->{
            mAnswers[0] = mRadioInput.getAnswer(0);
            ValueStore[] ans = mRadioInput.getAnswers();
            boolean proceedToShowSubInputs = (mPredicate == null) ?
                    ans != null && ans.length > 0 && ans[0] != null && ans[0].toInt() == 1
                    : mPredicate.predicate(mAnswers);
            if (proceedToShowSubInputs && mSubInputs.length > 0){
                mContainerInputGroup.setVisibility(View.VISIBLE);
                if (mContainerInputGroup.getChildCount() == 0){
                    for (GroupInput gi : mSubInputs) {
                        gi.inflate(
                                context.getLayoutInflater(),
                                context.getLabelProvider(),
                                mContainerInputGroup
                        );
                        gi.bindListeners(context, onAnswerEvent);

                        if (mDoSetupIME)
                            gi.setupImeAction(context.getNavigationToolkit());
                    }

                    mSubInputs[0].requestFocus();
                }
            } else {
                mContainerInputGroup.setVisibility(View.GONE);
                for (GroupInput gi : mSubInputs)
                    gi.reset();

                context.getFormContainer().post(()->{
                    context.getFormContainer().requestLayout();
                });
            }

            if (onAnswerEvent != null)
                onAnswerEvent.run();
        });
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {
        //do nothing because SubgroupInput not yet inflated
        //and calling this method on mSubgroupInput may cause NullPointerException
        //mSubgroupInput.setupImeAction(toolkit);
        mDoSetupIME = true;
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        LinearLayout item;
        if(getInputView() == null) {
            item = (LinearLayout) inflater.inflate(getResId(), parent, false);

            LinearLayout containerRadio = item.findViewById(R.id.container_rbi);
            tvCode = item.findViewById(R.id.tv_1);
            tvUnit = item.findViewById(R.id.tv_2);
            mContainerInputGroup = item.findViewById(R.id.container_input_group);

            mRadioInput.inflate(inflater, labels, containerRadio);

            if (hasExtras()) {
                if (mExtras.length > 0 && mExtras[0] != null)
                    tvCode.setText(mExtras[0].toString());
                else
                    tvCode.setVisibility(View.GONE);

                if (mExtras.length > 1 && mExtras[1] != null)
                    tvUnit.setText(mExtras[1].toString());
                else
                    tvUnit.setVisibility(View.GONE);
            } else {
                tvCode.setVisibility(View.GONE);
                tvUnit.setVisibility(View.GONE);
            }

            inputElement = item;
        } else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }

        parent.addView(item);
    }

}
