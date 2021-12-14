package pk.gov.pbs.formbuilder.inputs.singular;

import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.exceptions.IllegalMethodCallException;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;

public class DataInput extends SingularInput {
    private static LabelProvider mLabelProvider;
    private LinearLayout inputElement;
    private TextView mtvLabel;
    private TextView[] mDataViews;
    private boolean useAccent = false;

    public DataInput(String index, ValueStore... data) {
        super(index, R.layout.input_data);
        mAnswers = data;
    }

    public DataInput highlight() throws IllegalMethodCallException {
        if (inputElement != null) {
            throw new IllegalMethodCallException("useAccentColor] - This method is only effective for un-inflated askable");
        }

        useAccent = true;
        return this;
    }

    @Override
    public boolean loadAnswerIntoInputView(ViewModelFormSection viewModel) {
        if (mDataViews != null) {
            if (mAnswers != null && mAnswers.length > 0) {
                for (int i = 0; i < mDataViews.length; i++) {
                    if (i < mAnswers.length && mAnswers[i] != null) {
                        Spanned label =  Html.fromHtml(
                                mAnswers[i].toString() == null ? "<i>Nill</i>" :
                                        mLabelProvider.hasLabel(getIndex()+"_"+mAnswers[i].toString()) ?
                                                mAnswers[i].toString() + ". " + mLabelProvider.getLabel(getIndex()+"_"+mAnswers[i].toString()) :
                                                mAnswers[i].toString()
                        );
                        mDataViews[i].setText(label);
                    } else
                        mDataViews[i].setVisibility(View.GONE);
                }

            } else {
                for (int i = 1; i < mDataViews.length; i++)
                    mDataViews[i].setVisibility(View.GONE);
            }

        }
        return false;
    }

    @Override
    public boolean hasAnswer() {
        return mAnswers != null && mAnswers.length > 0;
    }

    @Override
    public boolean lock() {
        if (!IS_UNLOCKED)
            return true;

        if (inputElement != null) {
            //ThemeUtils.applyThemedDrawableToView(mtvLabel, R.attr.bgSelectableLockedUnanswered);
//            for (TextView tv : mDataViews) {
//                if (tv.getVisibility() == View.VISIBLE)
//                    ThemeUtils.applyThemedDrawableToView(tv, R.attr.bgSelectableLocked);
//            }
            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if (IS_UNLOCKED)
            return true;

        if (inputElement != null) {
            //ThemeUtils.applyThemedDrawableToView(mtvLabel, R.attr.bgSelectableUnlocked);
//            for (TextView tv : mDataViews) {
//                if (tv.getVisibility() == View.VISIBLE)
//                    ThemeUtils.applyThemedDrawableToView(tv, R.attr.bgSelectableUnlocked);
//            }
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {}

    @Override
    public boolean requestFocus() {
        return false;
    }

    @Override
    public boolean hasFocus() {
        return false;
    }

    @Override
    public LinearLayout getInputView() {
        return inputElement;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {
        loadAnswerIntoInputView(context.getViewModel());
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        if (mLabelProvider == null)
            mLabelProvider = labels;

        LinearLayout item;
        if(getInputView() == null) {
            if (!useAccent)
                item = (LinearLayout) inflater.inflate(getResId(), parent, false);
            else
                item = (LinearLayout) inflater.inflate(R.layout.input_data_accent, parent, false);

            TextView tvLabel, tv1, tv2, tv3;
            tvLabel = item.findViewById(R.id.tv_label);
            tv1 = item.findViewById(R.id.tv_1);
            tv2 = item.findViewById(R.id.tv_2);
            tv3 = item.findViewById(R.id.tv_3);

            if (labels.hasLabel(getIndex()))
                tvLabel.setHint(Html.fromHtml(labels.getLabel(getIndex())));
            else
                tvLabel.setVisibility(View.GONE);

            mtvLabel = tvLabel;
            mDataViews = new TextView[] {
                    tv1, tv2, tv3
            };

            inputElement = item;
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }

        parent.addView(item);
    }

    @Override
    public boolean setAnswer(int index, ValueStore answer) {
        if(super.setAnswer(index, answer)) {
            if (inputElement != null) {
                if (index < mDataViews.length)
                    mDataViews[index].setText(
                            Html.fromHtml(answer.toString())
                    );
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean setAnswers(ValueStore... answers) {
        mAnswers = answers; //accept all answers w/o length check
        loadAnswerIntoInputView(null);
        return true;
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        if (mAnswers.length > 1) {
            char suffix = 'a';
            for (ValueStore vs : mAnswers) {
                if (vs != null)
                    model.set(getIndex() + suffix++, vs);
            }
        } else
            model.set(getIndex(), mAnswers[0]);
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        boolean result = false;
        if (mAnswers.length > 1) {
            char suffix = 'a';
            for (int i=0; i<mAnswers.length; i++){
                if (model.hasField(getIndex() + suffix++)) {
                    mAnswers[i] = model.get(getIndex() + suffix++);
                    result |= mAnswers[i] != null;
                }
            }
        } else {
            mAnswers[0] = model.get(getIndex());
            result = mAnswers[0] != null;
        }
        return result;
    }
}
