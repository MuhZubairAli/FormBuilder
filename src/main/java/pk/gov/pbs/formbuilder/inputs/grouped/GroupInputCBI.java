package pk.gov.pbs.formbuilder.inputs.grouped;

import java.util.ArrayList;
import java.util.List;

import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.inputs.singular.CheckInput;
import pk.gov.pbs.formbuilder.inputs.singular.Selectable;
import pk.gov.pbs.formbuilder.meta.ColumnCount;
import pk.gov.pbs.formbuilder.validator.Validator;

/**
 * This grouped input class uses CheckInput internally
 * it's mAnswers array is single dimensional so it do not
 * supports SpecifiableCheckInput, so all methods are implemented
 * to only use non specifiable CheckInputs
 */
public class GroupInputCBI extends GroupInputSelectable{
    protected List<ValueStore> mAnswerList;
    public GroupInputCBI(String index, CheckInput[] selectables, ColumnCount columnCount, Validator validator, String... extras) {
        super(index, selectables, columnCount, validator, extras);
        mAnswers = new ValueStore[selectables.length];
        mAnswerList = new ArrayList<>();
    }

    public GroupInputCBI(String index, CheckInput[] selectables, ColumnCount columnCount, String... extras) {
        this(index, selectables, columnCount, null, extras);
    }

    public ValueStore[] getCompactAnswers() {
        mAnswerList.clear();
        for (Selectable ci : mSelectables){
            if (ci.hasAnswer())
                mAnswerList.add(ci.getAnswer());
        }

        if (mAnswerList.size() > 0){
            ValueStore[] ans = new ValueStore[mAnswerList.size()];
            ans = mAnswerList.toArray(ans);
            return ans;
        }
        return null;
    }

    @Override
    public boolean hasAnswer() {
        boolean result = false;
        for (ValueStore vs : mAnswers)
            result |= vs != null && !vs.isEmpty();
        return result;
    }

    @Override
    public boolean validateAnswer() {
        if(canSkipValidate())
            return true;

        boolean result = false;
        for (ValueStore vs : mAnswers){
            if (vs != null)
                result |= getValidator().isValid(vs);
        }
        return result;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        boolean result = getInputView() != null; //optimized two checks into one
        if (result) {
            for (ValueStore vs : mAnswers) {
                if (vs != null) {
                    for (Selectable ab : mSelectables) {
                        if (ab.getValue().equalsIgnoreCase(vs)) {
                            ab.setAnswerAsChecked();
                            result &= ab.loadAnswerIntoInputView(viewModel);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {
        for (int i = 0; i < mSelectables.length; i++) {
            final int index = i;

            mSelectables[i].bindListeners(context, () -> {
                if (onAnswerEvent != null)
                    onAnswerEvent.run();
            });

            mSelectables[i].setOnAnswerEventListener((oldAnswers, newAnswers) -> {
                if (mAnswers == newAnswers)
                    return;

                /**
                 * Setting mAnswers directly to bypass the length check
                 * and it causes the onAnswerEvent to not fire
                 */
                mAnswers[index] = newAnswers[0];
            });
        }
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        for (Selectable ab : mSelectables){
            if (ab.hasAnswer())
                model.set(ab.getIndex(), ab.getValue());
        }
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        boolean result = false;
        for (int i=0; i < mSelectables.length; i++){
            ValueStore ans = model.get(mSelectables[i].getIndex());
            if (ans != null){
                mAnswers[i] = ans;
                result = true;
            }
        }
        return result;
    }
}