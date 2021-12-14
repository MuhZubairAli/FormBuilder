package pk.gov.pbs.formbuilder.inputs.grouped.grid;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.core.ViewModelFormSection;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.exceptions.IllegalMethodCallException;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.inputs.singular.DataInput;
import pk.gov.pbs.formbuilder.meta.ColumnCount;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;

public class GroupInputGridData extends GroupInputGrid {
    public GroupInputGridData(String index, ColumnCount columnCount, DataInput[] singularInput) {
        super(index, columnCount, singularInput, null);
    }

    public GroupInputGridData highlight() throws IllegalMethodCallException {
        for (SingularInput input :
                mSingularInputs) {
            ((DataInput) input).highlight();
        }
        return this;
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) { }

    @Override
    public boolean validateAnswer() {
        return true; //no need to validation because no user input is involved here
    }

    @Override
    public boolean hasAnswer() {
        boolean result = true;
        for (SingularInput input : mSingularInputs){
            result &= input.hasAnswer();
        }
        return result;
    }

    @Override
    public boolean loadAnswerIntoInputView(@Nullable ViewModelFormSection viewModel) {
        if (getInputView() != null && hasAnswer()){
            for (int i=0; i < mSingularInputs.length; i++) {
                if (!nse(mAnswers[i], mSingularInputs[i].getAnswer())) {
                    mSingularInputs[i].setAnswer(mAnswers[i]);
                    mSingularInputs[i].loadAnswerIntoInputView(viewModel);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void bindListeners(ActivityFormSection context, Runnable onAnswerEvent) {
        for (int i=0; i < mSingularInputs.length; i++) {
            mSingularInputs[i].bindListeners(context, onAnswerEvent);
            mAnswers[i] = mSingularInputs[i].getAnswer(0);
        }
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        ViewGroup item;
        if(getInputView() == null) {
            item = (ViewGroup) inflater.inflate(getResId(), parent, false);
            if (labels.hasLabel(getIndex())){
                TextView qStatement = (TextView) inflater.inflate(R.layout.label_input_tv, item, false);
                String label;
                if (labels.hasHint(getIndex()))
                    label = labels.getLabel(getIndex())
                            .concat("<br /><small>").
                                    concat(labels.getHint(getIndex())).
                                    concat("</small>");
                else
                    label = labels.getLabel(getIndex());
                qStatement.setText(Html.fromHtml(label));
                item.addView(qStatement);
            }

            int abCount = -1;
            for (int i = 0; i < mSingularInputs.length; i+=mColumnCount) {
                LinearLayout rowInputs = (LinearLayout) inflater.inflate(
                        R.layout.input_group_multi_col_singular_row, item, false
                );

                for (int j = 0; j < mColumnCount; j++) {
                    if (++abCount < mSingularInputs.length) {
                        mSingularInputs[abCount].inflate(
                                inflater,
                                labels,
                                rowInputs
                        );
                    }
                }

                item.addView(rowInputs);
            }

            inputElement = item;
        } else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }

        parent.addView(item);
    }

    @Override
    public void exportAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        for (SingularInput input : mSingularInputs)
            input.exportAnswer(model);
    }

    @Override
    public boolean importAnswer(Section model) throws NoSuchFieldException, IllegalAccessException {
        boolean result = true;
        for (SingularInput input : mSingularInputs)
            result &= input.importAnswer(model);
        return result;
    }

}
