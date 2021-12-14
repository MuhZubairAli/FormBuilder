package pk.gov.pbs.formbuilder.inputs.singular.adapters;

import android.view.ViewGroup;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.core.Question;
import pk.gov.pbs.formbuilder.inputs.abstracts.adapters.AskableAdapter;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.Askable;
import pk.gov.pbs.formbuilder.utils.ValueStore;

public class DataInputAdapter extends AskableAdapter {
    protected final Askable[] mAskables;

    public DataInputAdapter(Askable[] askables) {
        super(R.layout.container_ll);
        mAskables = askables;
    }

    @Override
    public void init(ActivityFormSection context, ViewGroup container, Question question) {
        super.init(context, container, question);

        bindListenersToAll(context);

        //-------------------------
        //Process question state
        //-------------------------
        initQuestion(context, question);
    }

    @Override
    public Askable[] getAskables() {
        return mAskables;
    }

    public String getValidationErrorStatement(){
        return null;
    }

    public String getValidationRulesStatement(){
        return null;
    }

    public boolean performValidationCheck(){
        return true;
    }

    public void hideUnansweredAskables(){
        if (getAskables() != null && getAskables().length > 2){
            for (Askable ab : getAskables()){
                if (!ab.hasAnswer())
                    ab.hide();
            }
        }
    }

    public void showAllAskables(){
        if (getAskables() != null && getAskables().length > 1){
            for (Askable ab : getAskables()){
                ab.show();
            }
        }
    }

    @Override
    public boolean hasAskableOfIndex(String abIndex) {
        for (Askable ab : getAskables()){
            if (ab.getIndex().equalsIgnoreCase(abIndex))
                return true;
        }
        return false;
    }

    @Override
    public ValueStore[][] getAnswers() {
        ValueStore[][] answers = new ValueStore[mAskables.length][];
        for (int i=0; i < mAskables.length; i++)
            answers[i] = mAskables[i].getAnswers();
        return answers;
    }
}
