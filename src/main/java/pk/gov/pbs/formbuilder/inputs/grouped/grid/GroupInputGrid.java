package pk.gov.pbs.formbuilder.inputs.grouped.grid;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.GroupInput;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.meta.ColumnCount;
import pk.gov.pbs.formbuilder.validator.Validator;
import pk.gov.pbs.utils.ThemeUtils;

public abstract class GroupInputGrid extends GroupInput {
    protected final int mColumnCount;
    protected SingularInput[] mSingularInputs;
    protected TextView[] mLabelsInputs;
    protected ViewGroup inputElement;

    public GroupInputGrid(String index, ColumnCount columnCount, SingularInput[] singularInput, Validator validator) {
        super(index, R.layout.input_group_multi_col_singular, validator);
        mColumnCount = columnCount.getValue();
        mSingularInputs = singularInput;
        mAnswers = new ValueStore[singularInput.length];
        mLabelsInputs = new TextView[singularInput.length];
    }

    @Override
    public boolean lock() {
        boolean result = true;
        if (inputElement != null){
            if(!IS_UNLOCKED)
                return true;

            for (SingularInput ab : mSingularInputs){
                result &= ab.lock();
            }

            if (result)
                IS_UNLOCKED = false;
        }
        return result;
    }

    @Override
    public boolean unlock() {
        boolean result = true;
        if (inputElement != null){
            if(IS_UNLOCKED)
                return true;

            for (SingularInput ab : mSingularInputs){
                result &= ab.unlock();
            }

            if (result)
                IS_UNLOCKED = true;
        }
        return result;
    }

    @Override
    public void reset() {
        if (inputElement != null){
            for (SingularInput ab : mSingularInputs){
                if (ab.hasAnswer())
                    ab.reset();
            }
        }
    }

    @Override
    public boolean requestFocus() {
        return mSingularInputs[0].requestFocus();
    }

    @Override
    public boolean hasFocus() {
        for (SingularInput ab : mSingularInputs)
            if (ab.hasFocus())
                return true;
        return false;
    }

    @Override
    public boolean hasIndex(String abIndex) {
        boolean result = getIndex().equalsIgnoreCase(abIndex);
        for (SingularInput gi : mSingularInputs)
            result |= gi.getIndex().equalsIgnoreCase(abIndex);
        return result;
    }

    @Override
    public ViewGroup getInputView() {
        return inputElement;
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        ViewGroup item;
        if(getInputView() == null) {
            item = (ViewGroup) inflater.inflate(getResId(), parent, false);
            if (labels.hasLabel(getIndex())){
                TextView qStatement = (TextView) inflater.inflate(R.layout.label_input_tv, item, false);
                ThemeUtils.setupTextViewStylesByLocale(labels.getLocale(), qStatement);
                qStatement.setText(Html.fromHtml(labels.getLabel(getIndex())));
                item.addView(qStatement);
            }

            int abCount = -1;
            for (int i = 0; i < mSingularInputs.length; i+=mColumnCount) {
                LinearLayout rowLabels = (LinearLayout) inflater.inflate(
                        R.layout.input_group_multi_col_singular_row, item, false
                );

                LinearLayout rowInputs = (LinearLayout) inflater.inflate(
                        R.layout.input_group_multi_col_singular_row, item, false
                );

                for (int j = 0; j < mColumnCount; j++) {
                    if (++abCount < mSingularInputs.length) {
                        TextView inputLabel = (TextView) inflater.inflate(R.layout.label_input_tv, rowLabels, false);
                        ThemeUtils.setupTextViewStylesByLocale(labels.getLocale(), inputLabel);
                        mLabelsInputs[i+j] = inputLabel;
                        Spanned label = Html.fromHtml(labels.getLabel(mSingularInputs[abCount].getIndex()));
                        inputLabel.setText(label);
                        rowLabels.addView(inputLabel);
                        mSingularInputs[abCount].inflate(
                                inflater,
                                labels,
                                rowInputs
                        );
                    }
                }
                item.addView(rowLabels);
                item.addView(rowInputs);
            }

            inputElement = item;
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }
}
