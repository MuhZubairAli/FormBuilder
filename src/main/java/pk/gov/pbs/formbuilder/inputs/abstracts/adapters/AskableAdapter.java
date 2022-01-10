package pk.gov.pbs.formbuilder.inputs.abstracts.adapters;

import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import pk.gov.pbs.formbuilder.core.ActivityCustom;
import pk.gov.pbs.formbuilder.core.IQuestionnaireManager;
import pk.gov.pbs.formbuilder.exceptions.InvalidIndexException;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.core.Question;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.Askable;
import pk.gov.pbs.formbuilder.meta.QuestionStates;
import pk.gov.pbs.utils.ExceptionReporter;

public abstract class AskableAdapter {
    protected int mContainerResID;
    protected IQuestionnaireManager mQuestionnaireManager;
    protected OnAnswerEvent mOnAnswerEvent;
    protected ViewGroup mAnswerContainer;

    public AskableAdapter(int resId){
        mContainerResID = resId;
    }

    protected void initQuestion(ActivityFormSection context, @Nullable Question question){
        if (question == null)
            return;

        if (question.getState() == QuestionStates.READ_ONLY || question.getState() == QuestionStates.ANSWERED)
            setupLoadedAnswers(context);

        /**
         * Execute onAnswerEvent listener so that the logic specified
         * in event would be applied at start like hiding any askable
         * until any other askable receives value and show it according to logic
         * specified in the event
         */
        if (mOnAnswerEvent != null)
            mOnAnswerEvent.onAnswer(context.getQuestionnaireManager(), getAskables());

        if (question.getState() == QuestionStates.PENDING || question.getState() == QuestionStates.UNLOCKED){
            question.setState(QuestionStates.UNLOCKED);
        } else if (question.getState() == QuestionStates.ANSWERED) {
            mAnswerContainer.post(question::lock);
        } else if(question.getState() == QuestionStates.LOCKED){
            question.setState(QuestionStates.UNLOCKED);
            mAnswerContainer.post(question::lock);
        }else if(question.getState() == QuestionStates.READ_ONLY){
            mAnswerContainer.post(question::rigidify);
            question.setState(QuestionStates.READ_ONLY);
        }
    }

    /**
     * this method call bindListeners on all askables
     * and if it has any onAnswer event listener it packs that listener into lambda runnable
     * @param context context of FormSectionActivity to get QuestionnaireManager
     */
    protected void bindListenersToAll(ActivityFormSection context){
        //Bind listener to all askables
        for (Askable ab : getAskables()) {
            if (mOnAnswerEvent == null)
                ab.bindListeners(context, null);
            else {
                if (mQuestionnaireManager == null)
                    mQuestionnaireManager = context.getQuestionnaireManager();
                ab.bindListeners(context, () ->
                        mOnAnswerEvent.onAnswer(
                                mQuestionnaireManager, getAskables()
                        )
                );
            }
        }
    }

    /**
     * get answers from all answered inputs as 2-D array
     * because answers from each askable would be array even from SingularInputs
     * so it returns all arrays in an array
     * @return ValueStore[][]
     */
    public abstract ValueStore[][] getAnswers();

    /**
     * This method perform all necessary tasks to get the askables going
     * some of its responsibilities are delegated to sub classes
     * it inflate the askables then
     * it binds event listeners to askables then
     * it loads answers into askable view (if answer has been set)
     * @param context current section context
     * @param container container of (ViewGroup) answerContainer (Question View)
     * @param question parent question
     */
    public void init(ActivityFormSection context, ViewGroup container, Question question) {
        mAnswerContainer = container;
        ViewGroup answerContainer = (ViewGroup) context.getLayoutInflater().inflate(mContainerResID,null);
        container.addView(answerContainer);

        for (Askable ab : getAskables()) {
            ab.inflate(context.getLayoutInflater(), context.getLabelProvider(), answerContainer);
        }
    }

    public void init(ActivityCustom context, ViewGroup container, Question question) {
        mAnswerContainer = container;
        ViewGroup answerContainer = (ViewGroup) context.getLayoutInflater().inflate(mContainerResID,null);
        container.addView(answerContainer);

        for (Askable ab : getAskables()) {
            ab.inflate(context.getLayoutInflater(), context.getLabelProvider(), answerContainer);
        }
    }

    /**
     * This method will setAnswers of all askables from model
     * copying of relevant values is delegated to each askable
     * @param model section model to be imported
     * @return true if successful
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public boolean loadModel(Section model) {
        boolean result = false;
        for (Askable ab : getAskables()){
            try {
                result |= ab.importAnswer(model);
            } catch (NoSuchFieldException e) {
                if (Constants.DEBUG_MODE)
                    Log.d(
                            AskableAdapter.class.getName(),
                            "loadModel] Askable with index '" + ab.getIndex() + "' don't exist in provided model"
                    );
            } catch (IllegalAccessException e) {
                ExceptionReporter.printStackTrace(e);
            }
        }
        return result;
    }

    public boolean loadAnswer(String abIndex, ValueStore... answers) throws InvalidIndexException {
        for (Askable ab : getAskables()){
            if (ab.getIndex().equalsIgnoreCase(abIndex)) {
                return ab.setAnswers(answers);
            }
        }
        throw new InvalidIndexException("there is no Askable with index as : " + abIndex);
    }

    public boolean loadAnswer(int abNumericIndex, ValueStore... answers) throws InvalidIndexException {
        if (abNumericIndex < 0 || abNumericIndex >= getAskables().length)
            throw new InvalidIndexException(abNumericIndex, "it must be between 0 and " + getAskables().length);
        if (getAskables()[abNumericIndex] != null) {
            return getAskables()[abNumericIndex].setAnswers(answers);
        }
        return false;
    }

    public void setupLoadedAnswers(ActivityFormSection context){
        for (Askable gi : getAskables()){
            if (gi.hasAnswer())
                gi.loadAnswerIntoInputView(context.getViewModel());
        }
    }

    /**
     * Askable getter required from every AskableAdapter
     * concrete classes would return more specific type
     * @return Askable[]
     */
    public abstract Askable[] getAskables();
    public Askable[] getAnsweredAskables() {
        List<Askable> inputs = new ArrayList<>();
        if (getAskables() != null && getAskables().length > 0){
            for (Askable input : getAskables()){
                if (input.hasAnswer())
                    inputs.add(input);
            }
        }
        Askable[] result = new Askable[inputs.size()];
        return inputs.toArray(result);
    }

    /**
     * Validation method to access relevant function
     * GroupedInput shall compile message from all askables having validator
     * and perform validation check on said GroupInput
     */
    public abstract boolean performValidationCheck();
    public abstract String getValidationErrorStatement();
    public abstract String getValidationRulesStatement();
    public abstract boolean hasAskableOfIndex(String abIndex);

    /**
     * Concrete methods for basic askable behaviour
     */
    public boolean lockAskables(){
        if(getAskables() != null && getAskables().length > 0){
            for (Askable ab : getAskables()){
                if(!ab.lock())
                    return false;
            }
            return true;
        }
        return false;
    }

    public boolean unlockAskables(){
        if(getAskables() != null && getAskables().length > 0){
            for (Askable ab : getAskables()){
                if(!ab.unlock())
                    return false;
            }
            return true;
        }
        return false;
    }

    public void resetAskables() {
        if(getAskables() != null && getAskables().length > 0) {
            for (Askable askable : getAskables()) {
                if (!askable.isUnlocked())
                    askable.unlock();
                askable.reset();
            }
        }
    }

    public abstract void hideUnansweredAskables();
    public abstract void showAllAskables();

    public ViewGroup getAnswerContainerView(){
        return mAnswerContainer;
    }
    public void setOnAnswerEventListener(OnAnswerEvent eventListener){
        mOnAnswerEvent = eventListener;
    }

    /**
     * This interface is used for all Adapters for said propose
     * This event will be called when an answer is provided to (i.e when setAnswers is called)
     * in this event all Askables would would be provided to event as well as viewModel
     */
    public interface OnAnswerEvent {
        void onAnswer(IQuestionnaireManager manager, Askable[] askables);
    }
}
