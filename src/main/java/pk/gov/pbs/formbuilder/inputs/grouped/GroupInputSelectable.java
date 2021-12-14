package pk.gov.pbs.formbuilder.inputs.grouped;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Arrays;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.LabelProvider;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.GroupInput;
import pk.gov.pbs.formbuilder.inputs.singular.Selectable;
import pk.gov.pbs.formbuilder.meta.ColumnCount;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.validator.Validator;

public abstract class GroupInputSelectable extends GroupInput {
    protected final int mColumnCount;
    protected Selectable[] mSelectables;
    protected ViewGroup inputElement;

    public GroupInputSelectable(String index, Selectable[] selectables, ColumnCount columnCount, Validator validator, String... extras){
        super(index, R.layout.input_group_tv_rbi, validator, extras);
        this.mSelectables = selectables;
        mColumnCount = columnCount.getValue();
    }

    public GroupInputSelectable(String index, Selectable[] selectables, ColumnCount columnCount, String... extras){
        this(index, selectables, columnCount, null, extras);
    }

    @Override
    public ValueStore[] getAnswers() {
        if (mExtras != null) {
            ValueStore[] answers = new ValueStore[mAnswers.length + mExtras.length];
            System.arraycopy(mAnswers,0,answers,0,mAnswers.length);
            System.arraycopy(mExtras,0,answers,mAnswers.length,mExtras.length);
            return answers;
        } else
            return super.getAnswers();
    }

    @Override
    public ValueStore getAnswer(int index) {
        if (index < mAnswers.length)
            return super.getAnswer(index);
        int ei = index - mAnswers.length;
        if (mExtras != null && ei < mExtras.length)
            return mExtras[index- mAnswers.length];
        return null;
    }

    @Override
    public boolean lock() {
        if(inputElement != null){
            if(!IS_UNLOCKED)
                return true;

            for (Selectable ab : mSelectables){
                ab.lock();
            }
            IS_UNLOCKED = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if(inputElement != null){
            if(IS_UNLOCKED)
                return true;

            for (Selectable ab : mSelectables){
                ab.unlock();
            }
            IS_UNLOCKED = true;
            return true;
        }
        return false;
    }

    @Override
    public ViewGroup getInputView() {
        return inputElement;
    }

    @Override
    public boolean hasIndex(String abIndex) {
        boolean result = getIndex().equalsIgnoreCase(abIndex);
        for (Selectable ab : mSelectables)
            result |= ab.getIndex().equalsIgnoreCase(abIndex);
        return result;
    }

    @Override
    public boolean validateAnswer() {
        if(canSkipValidate())
            return true;

        boolean result = false;

//        for (ValueStore vs : mAnswers){
//            if (vs != null)
//                result |= getValidator().isValid(vs);
//        }

        for (Selectable ab : mSelectables){
            if (ab.hasAnswer())
                result |= getValidator().isValid(ab.getAnswer());
        }
        return result;
    }

    @Override
    public void hideUnanswered() {
        if (mSelectables != null && mSelectables.length > 2){
            for (Selectable ab : mSelectables){
                if (!ab.hasAnswer())
                    ab.hide();
            }
        }
    }

    @Override
    public void showAll() {
        if (mSelectables != null && mSelectables.length > 1){
            for (Selectable ab : mSelectables){
                if (!ab.isVisible())
                    ab.show();
            }
        }
    }

    @Override
    public void reset() {
        //this was added as workaround, not needed now
        //because GroupInputSelectable won't store answers
        //instead use storage of internal askables
        Arrays.fill(mAnswers, null);

        if(inputElement != null){
            for (Selectable ri : mSelectables){
                ri.reset();
            }
        }
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {}

    @Override
    public boolean requestFocus() {
        return false;
    }

    @Override
    public boolean hasFocus() {
        return false;
    }

    @Override
    public void inflate(LayoutInflater inflater, LabelProvider labels, ViewGroup parent) {
        ViewGroup item;
        if(getInputView() == null) {
            item = (ViewGroup) inflater.inflate(getResId(), parent, false);
            TextView label = item.findViewById(R.id.tv);
            if(labels.hasLabel(getIndex())) {
                StringBuilder qLabel = new StringBuilder();
                if (hasExtras())
                    qLabel.append("<b>[").append(getExtra(0).toString()).append("] : </b>");
                qLabel.append(labels.getLabel(getIndex()));
                if (labels.hasHint(getIndex()))
                    qLabel.append("<br /><small>").append(labels.getHint(getIndex())).append("</small>");
                Spanned htm = Html.fromHtml(qLabel.toString());
                label.setText(htm);
            }else
                label.setVisibility(ViewGroup.GONE);

            ViewGroup container = item.findViewById(R.id.container);

            final int itemCount = mSelectables.length;
            final int rowCount = (int) Math.ceil((float) itemCount / mColumnCount);
            int abCount = -1;
            for (int i = 0; i < rowCount; i++) {
                RadioGroup answerContainer = (RadioGroup) inflater.inflate(
                        R.layout.container_rg_col, container, false);
                for (int j = 0; j < mColumnCount; j++) {
                    if (++abCount < itemCount) {
                        mSelectables[abCount].inflate(
                                inflater,
                                labels,
                                answerContainer
                        );
                    }
                }
                container.addView(answerContainer);
            }


            inputElement = item;
        }else {
            item = getInputView();
            ((ViewGroup) item.getParent()).removeView(item);
        }
        parent.addView(item);
    }
}