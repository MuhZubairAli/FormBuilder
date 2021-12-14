package pk.gov.pbs.formbuilder.inputs.abstracts.adapters;

import android.view.ViewGroup;

import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.Question;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.GroupInput;

public class GroupInputAdapter extends AskableAdapter {
    protected final GroupInput[] mGroupInputs;

    public GroupInputAdapter(GroupInput[] inputs) {
        super(R.layout.container_tl_gi);
        mGroupInputs =  inputs;
    }

    public GroupInputAdapter(GroupInput[] inputs, OnAnswerEvent event) {
        this(inputs);
        mOnAnswerEvent = event;
    }

    public ValueStore[][] getAnswers() {
        ValueStore[][] answers = new ValueStore[mGroupInputs.length][];
        for(int i = 0; i < mGroupInputs.length; i++) {
            answers[i] = mGroupInputs[i].getAnswers();
        }
        return answers;
    }

    @Override
    public void init(ActivityFormSection context, ViewGroup container, Question question) {
        super.init(context, container, question);

        bindListenersToAll(context);

        /**
         * Apply ime action on last askable
         * which specify to goto next question on keyboard Enter key press or
         * any other means of navigation
         */
        mGroupInputs[mGroupInputs.length-1]
                .setupImeAction(context.getNavigationToolkit());

        //-------------------------
        // Process question state
        //-------------------------
        initQuestion(context, question);

        /*
          * Enabling animations on each group input
         */
//        for (GroupInput gi : getAskables()){
//            gi.setAnimateChanges(true);
//        }
    }

    @Override
    public GroupInput[] getAskables() {
        return mGroupInputs;
    }

    @Override
    public boolean unlockAskables() {
        boolean result = super.unlockAskables();
        if(mOnAnswerEvent != null && result) {
            if (mQuestionnaireManager == null)
                mQuestionnaireManager = ((ActivityFormSection) getAskables()[0]
                        .getInputView().getContext()).getQuestionnaireManager();
            mOnAnswerEvent.onAnswer(mQuestionnaireManager, mGroupInputs);
        }
        return result;
    }

    /**
     * This method only perform validation check iff the question have been initialized
     * and all askables are inflated in this way mOnAnswerEventListener have been called to confirm
     * visibility logic of askables, otherwise checks if has answer and returns true because
     * a question with ANSWERED state can not have invalid answer because it is loaded from model
     * and model is never saved into database if has any invalid answer
     * @return true if question has valid answer
     */
    @Override
    public boolean performValidationCheck() {
        boolean hasAnswer = false;

        for (GroupInput gi : mGroupInputs){
            if(gi.hasAnswer()){
                hasAnswer = true;
                break;
            }
        }

        if (mAnswerContainer == null && hasAnswer)
            return true;

        for (GroupInput gi : mGroupInputs){
            if(gi.isVisible() && gi.getValidator() != null){
                if(!gi.validateAnswer())
                    return false;
            }
        }

        return true;
    }

    public String getValidationErrorStatement() {
        StringBuilder sb = new StringBuilder();
        String questionSeparator = "";
        int questionNumberLabel = 1;

        for (GroupInput gi : getAskables()){
            if(gi.isVisible() && !gi.validateAnswer()){
                sb
                    .append(questionSeparator)
                    .append("<b>Sub-Question ")
                    .append(questionNumberLabel)
                    .append(":</b> \t")
                    .append(
                            gi.getValidator().getErrorStatement()
                    );

                if(questionSeparator.isEmpty())
                    questionSeparator = "<br />\n";
            }
            questionNumberLabel++;
        }
        if(sb.length()>0)
            return sb.toString();
        return null;
    }

    public String getValidationRulesStatement() {
        StringBuilder sb = new StringBuilder();
        String questionSeparator = "";
        int questionNumberLabel = 1;

        for (GroupInput gi : getAskables()){
            if(gi.isVisible() && !gi.validateAnswer()){
                sb.append(questionSeparator)
                    .append("<b>Sub-Question ")
                    .append(questionNumberLabel)
                    .append(":</b> \t")
                    .append(
                            gi.getValidator().getRuleStatement()
                    );

                if(questionSeparator.isEmpty())
                    questionSeparator = "<br />\n";
            }
            questionNumberLabel++;
        }

        if(sb.length()>0)
            return sb.toString();
        return null;
    }

    public void hideUnansweredAskables(){
        if (mGroupInputs != null){
            for (GroupInput ab : mGroupInputs){
                ab.hideUnanswered();
            }
        }
    }

    public void showAllAskables(){
        if (mGroupInputs != null){
            for (GroupInput ab : getAskables()){
                ab.setAnimateChanges(false);
                ab.showAll();
            }

            if (mOnAnswerEvent != null)
                mOnAnswerEvent.onAnswer(mQuestionnaireManager, mGroupInputs);

            for (GroupInput ab : getAskables())
                ab.setAnimateChanges(true);
        }
    }

    @Override
    public boolean hasAskableOfIndex(String abIndex) {
        for (GroupInput gi : getAskables()){
            if (gi.hasIndex(abIndex))
                return true;
        }
        return false;
    }
}
