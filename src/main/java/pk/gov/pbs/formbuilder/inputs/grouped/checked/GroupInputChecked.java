package pk.gov.pbs.formbuilder.inputs.grouped.checked;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.utils.FormBuilderThemeHelper;
import pk.gov.pbs.formbuilder.validator.Validator;
import pk.gov.pbs.utils.ThemeUtils;

/**
 * This input is not fully generalized in it's layout
 * But it will work with any group input as SubgroupInput
 */
public class GroupInputChecked extends GroupInputLinearLayout {
    private boolean mDoSetupIME = false;
    private int mAnswersLength;
    protected LinearLayout mContainerInputGroup;
    protected CheckBox cbi;
    protected TextView tvCode, tvUnit;
    protected GroupInput[] mSubInputs;
    protected final GroupInputCheckedPredicate mPredicate;

    public GroupInputChecked(String index, GroupInputCheckedPredicate predicate, GroupInput[] subInputs, Validator validator, String... extras){
        super(index, R.layout.input_group_checked, validator, extras);
        mSubInputs = subInputs;
        mPredicate = predicate;
        mAnswers = new ValueStore[1];
        mAnswersLength = 1; //zeroth answer
        for (GroupInput gi : subInputs)
            mAnswersLength += gi.getAnswers().length;

        mAnswers[0] = new ValueStore(2); // by default it is 2=(Unchecked)
    }

    public GroupInputChecked(String index, GroupInput[] subInputs, Validator validator, String... extras){
        this(index, null, subInputs, validator, extras);
    }

    public GroupInputChecked(String index, GroupInput subInput, Validator validator, String... extras){
        this (index, new GroupInput[]{ subInput }, validator, extras);
    }

    public GroupInputChecked(String index, GroupInput mSubInput){
        this(index, mSubInput, null);
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
            System.arraycopy(
                    gi.getAnswers(),
                    0,
                    combinedAnswers,
                    startCopyPos,
                    gi.getAnswers().length
            );
            startCopyPos += gi.getAnswers().length;
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

    /**
     * This method tells if this askable is completely being displayed
     * this takes mPrdicate into account which determines whether or not subInput to be shown
     * by default if predicate is not present then if checkInput is checked then it is considered
     * the GroupInputChecked is complete
     * @return true if displaying all subInputs
     */
    public boolean isComplete(){
        if (mAnswers[0] != null)
            return (mPredicate == null) ?
                    mAnswers[0].toInt() == 1 : mPredicate.predicate(mAnswers);
        return false;
    }

    @Override
    public boolean hasAnswer() {
        if (mAnswers[0] != null){
            boolean proceed = (mPredicate == null) ?
                    mAnswers[0].toInt() == 1 : mPredicate.predicate(mAnswers);

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

        if ( mAnswers[0] != null && mAnswers[0].toInt() == 1) {
            boolean result = true;
            for (GroupInput gi : mSubInputs)
                result &= gi.validateAnswer();
            return result;
        }

        return getValidator().isValid(mAnswers[0]);
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        model.set(getIndex(), mAnswers[0]);
        if (hasExtras()) {
            char suffix = 'a';
            for (ValueStore vs : getExtras()){
                model.set(getIndex()+ suffix++, vs);
            }
        } else

        for (GroupInput gi : mSubInputs)
            gi.exportAnswer(model);
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        mAnswers[0] = model.get(getIndex());
        boolean result = mAnswers[0] != null;

        if (result) {
            for (GroupInput gi : mSubInputs)
                gi.importAnswer(model);
        }

        return result;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if(cbi != null){
            if(isComplete()) {
                if (!cbi.isChecked())
                    cbi.setChecked(true);
                for (GroupInput gi : mSubInputs)
                    gi.loadAnswerIntoInputView(viewModel);
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

            for (GroupInput gi : mSubInputs)
                gi.lock();

            lockItem();

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

            for (GroupInput gi : mSubInputs)
                gi.unlock();

            unlockItem();

            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    @Override
    public void hideUnanswered() {
        super.hideUnanswered();
        boolean proceed = (mPredicate == null) ?
                cbi.isChecked() : mPredicate.predicate(mAnswers);

        if (proceed) {
            for (GroupInput gi : mSubInputs) {
                gi.hideUnanswered();
            }
        }
    }

    @Override
    public void showAll() {
        super.showAll();
        boolean proceed = (mPredicate == null) ?
                cbi.isChecked() : mPredicate.predicate(mAnswers);

        if (proceed) {
            for (GroupInput gi : mSubInputs) {
                gi.showAll();
            }
        }
    }

    private void lockItem(){
        CheckBox item = cbi;
        if(!item.isChecked()) {
            FormBuilderThemeHelper.applyThemedDrawableToView(item, R.attr.bgSelectableLockedUnanswered);
        }else {
            FormBuilderThemeHelper.applyThemedDrawableToView(item, R.attr.bgSelectableLocked);
        }
        if (tvCode != null && tvCode.getVisibility() == View.VISIBLE)
            FormBuilderThemeHelper.applyThemedDrawableToView(tvCode, R.attr.bgSelectableLockedUnanswered);
        if (tvUnit != null && tvUnit.getVisibility() == View.VISIBLE)
            FormBuilderThemeHelper.applyThemedDrawableToView(tvUnit, R.attr.bgSelectableLockedUnanswered);
        item.setClickable(false);
    }

    private void unlockItem(){
        CheckBox item = cbi;
        FormBuilderThemeHelper.applyThemedDrawableToView(item, R.attr.bgSelectableUnlocked);
        if (tvCode != null && tvCode.getVisibility() == View.VISIBLE)
            FormBuilderThemeHelper.applyThemedDrawableToView(tvCode, R.attr.bgSelectableUnlocked);
        if (tvUnit != null && tvUnit.getVisibility() == View.VISIBLE)
            FormBuilderThemeHelper.applyThemedDrawableToView(tvUnit, R.attr.bgSelectableUnlocked);
        item.setClickable(true);
    }

    @Override
    public boolean requestFocus() {
        if(mSubInputs[0].getInputView() != null) {
            for (GroupInput gi : mSubInputs)
                if(gi.requestFocus())
                    return true;
        }
        return false;
    }

    @Override
    public void reset() {
        if (cbi != null){
            cbi.setChecked(false);

            for (GroupInput gi : mSubInputs)
                gi.reset();
        }
    }

    @Override
    public boolean hasFocus() {
        if (mSubInputs[0].getInputView() != null) {
            boolean result = false;
            for (GroupInput gi : mSubInputs)
                result |= gi.hasFocus();
            return result;
        }
        return false;
    }

    @Override
    public boolean hasIndex(String abIndex) {
        boolean result = getIndex().equalsIgnoreCase(abIndex);
        for (GroupInput gi : mSubInputs)
            result |= gi.hasIndex(abIndex);
        return result;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {
        cbi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean proceedToShowSubInputs = (mPredicate == null) ?
                    isChecked : mPredicate.predicate(mAnswers);

            if (proceedToShowSubInputs){
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
                mAnswers[0].setValue(1);
            } else {
                mContainerInputGroup.setVisibility(View.GONE);
                for (GroupInput gi : mSubInputs)
                    gi.reset();

                mAnswers[0].setValue(2);
            }

            if (onAnswerEvent != null)
                onAnswerEvent.run();

            context.getFormContainer().post(()->{
                context.getFormContainer().requestLayout();
            });
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
            cbi = item.findViewById(R.id.cbi);
            tvCode = item.findViewById(R.id.tv_1);
            tvUnit = item.findViewById(R.id.tv_2);
            ThemeUtils.setupTextViewStylesByLocale(labels.getLocale(), cbi);
            mContainerInputGroup = item.findViewById(R.id.container_input_group);

            if (mExtras != null) {
                if (mExtras.length > 0 && mAnswers[0] != null)
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

            cbi.setText(
                    Html.fromHtml(labels.getLabel(getIndex()))
            );

            inputElement = item;
        } else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }

        parent.addView(item);
    }

}
