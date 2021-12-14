package pk.gov.pbs.formbuilder.inputs.abstracts.adapters;

import android.view.ViewGroup;

import java.util.ArrayList;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.core.Question;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.Askable;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.SingularInput;
import pk.gov.pbs.formbuilder.pojos.Jump;
import pk.gov.pbs.formbuilder.validator.Validator;

public abstract class SingularInputAdapter extends AskableAdapter {
    protected final SingularInput[] mSingularInputs;
    protected Validator mValidator;
    protected ArrayList<Jump> mJumps;
    protected String[] mExtras;

    public SingularInputAdapter(int containerResId, SingularInput[] singularInputs, String... extras) {
        super(containerResId);
        mSingularInputs = singularInputs;
        if (extras != null && extras.length > 0)
            mExtras = extras;
    }

    public SingularInputAdapter(int containerResId, SingularInput[] singularInputs, Validator validator, String... extras) {
        this(containerResId, singularInputs);
        mValidator = validator;
        if (extras != null && extras.length > 0)
            mExtras = extras;
    }

    public boolean hasExtras(){
        return mExtras != null && mExtras.length > 0;
    }

    public String getExtra(int index){
        return mExtras[index];
    }

    public String[] getExtras(){
        return mExtras;
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
    public SingularInput[] getAskables() {
        return mSingularInputs;
    }

    public Validator getValidator() {
        return mValidator;
    }
    public ArrayList<Jump> getJumps() {
        if(mJumps !=null && mJumps.size() > 0)
            return mJumps;
        return null;
    }

    public String getValidationErrorStatement(){
        if(mValidator != null)
            return mValidator.getErrorStatement();
        return null;
    }
    public String getValidationRulesStatement(){
        if(mValidator != null)
            return mValidator.getRuleStatement();
        return null;
    }
    public boolean performValidationCheck(){
        if(mValidator == null)
            return true;

        mJumps = new ArrayList<>();
        ValueStore[][] answers = getAnswers();
        if(answers != null) {
            for (ValueStore[] vsArr : answers) {
                Jump action = mValidator.validate(vsArr[0]);
                if (action == null) {
                    return false;
                } else if (action.isActionable()) {
                    mJumps.add(action);
                }
            }
        }else {
            Jump action = mValidator.validate(null);
            if (action == null) {
                return false;
            } else if (action.isActionable()) {
                mJumps.add(action);
            }
        }
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
}
