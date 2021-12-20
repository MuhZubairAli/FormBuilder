package pk.gov.pbs.formbuilder.toolkits;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Objects;

import pk.gov.pbs.formbuilder.core.ActivityFormSection;
import pk.gov.pbs.formbuilder.core.QuestionActor;
import pk.gov.pbs.formbuilder.core.QuestionHeader;
import pk.gov.pbs.formbuilder.core.Question;
import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.inputs.abstracts.adapters.SingularInputAdapter;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.meta.QuestionNavigationStatus;
import pk.gov.pbs.formbuilder.meta.QuestionStates;
import pk.gov.pbs.formbuilder.pojos.Jump;
import pk.gov.pbs.formbuilder.pojos.QuestionNavigationResponse;
import pk.gov.pbs.utils.StaticUtils;
import pk.gov.pbs.utils.UXToolkit;

public class NavigationToolkit {
    private final ActivityFormSection mContext;
    private final Handler mHandler;
    private final ArrayList<Question> mQuestions;
    private final UXToolkit mUXToolkit;

    public NavigationToolkit(ActivityFormSection context){
        this.mContext = context;
        this.mQuestions = context.getQuestionnaireManager().getQuestions();
        this.mUXToolkit = context.getUXToolkit();
        this.mHandler = StaticUtils.getHandler();
    }

    private boolean scrollToNextUnlockedQuestion(int startFrom){
        return scrollToNextUnlockedQuestion(startFrom, true);
    }

    private boolean scrollToNextUnlockedQuestion(int startFrom, boolean flash) {
        for(int j = startFrom; j < mQuestions.size(); j++) {
            QuestionStates state = mQuestions.get(j).getState();
            if (state == QuestionStates.READ_ONLY)
                continue;

            if(state != QuestionStates.LOCKED) {
                quickScrollTo(j, flash);
                return true;
            }
        }
        return false;
    }

    private boolean takeJump(int fromSection, Jump jump){
        if(jump == null || !jump.isActionable())
            return false; //continue execution of askNextQuestion();

        if(
                jump.getSection() != null
                //&& SectionsModels.containsKey(jump.getSection())
                //&& jump.getSection() > fromSection
        ) {
            mContext.getQuestionnaireManager().changeQuestionRangeToEnd ();
            return false;
        }

        if(jump.getStart() != null){

            if(jump.getEnd() != null)
                mContext.getQuestionnaireManager().changeQuestionRange(jump.getStart(),jump.getEnd());
            else
                mContext.getQuestionnaireManager().changeQuestionRange(jump.getStart());

            return false; //continue
        }
        return false;
    }
    public ActivityFormSection getContext(){
        return mContext;
    }
    public UXToolkit getUXToolkit(){
        return mUXToolkit;
    }

    public int getQuestionIndexByAskableIndex(String abIndex){
        for (int i = 0; i < mQuestions.size(); i++){
            if (
                    !(mQuestions.get(i) instanceof QuestionHeader)
                    && mQuestions.get(i).getAdapter().hasAskableOfIndex(abIndex)
            )
                return i;
        }
        return Constants.INVALID_NUMBER;
    }

    public int getQuestionIndex(Question question){
        for (int i = 0; i < mQuestions.size(); i++){
            if (mQuestions.get(i).equals(question))
                return i;
        }
        return Constants.INVALID_NUMBER;
    }

    public void scrollTo(int position){
        mContext
                .getFormContainer()
                .smoothScrollToPosition(position);
    }

    public void quickScrollTo(int position, boolean flash){
        mContext
                .getFormContainer()
                .scrollToPosition(position);
        if (flash)
            mQuestions.get(position).flash();
    }

    public void quickScrollTo(int position){
        quickScrollTo(position,false);
    }

    public void askNextQuestion(){
        askNextQuestion(false, (mQuestions.size() -1));
    }

    public void askNextQuestion(boolean force){
        askNextQuestion(force, (mQuestions.size() -1));
    }

    public void askNextQuestion(int questionNumber){
        askNextQuestion(false, questionNumber);
    }

    public void askNextQuestion(boolean force, int questionNumber){
        for (int i = 0; i < mQuestions.size() -1; i++) {
            Question question = mQuestions.get(i);

            if(question.getState() == QuestionStates.READ_ONLY)
                continue;

            if (i < questionNumber) {
                if(question.getState() != QuestionStates.LOCKED && question.getState() != QuestionStates.ANSWERED) {
                    quickScrollTo(i,true);
                    question.requestInputFocus();
                    return;
                }
            } else if (i == questionNumber) {
                if ((questionNumber + 1) < mQuestions.size() && (question.getState() != QuestionStates.LOCKED)) {
                    if (question.isCritical() && !question.hasAnswers()) {
                        mContext.restartFrom(question.getIndex());
                        return;
                    } else {
                        if(question.validateAnswer()) {
                            String eCode = question.ExecPostCondition(mContext.getQuestionnaireManager(), question);
                            if (eCode != null && !eCode.isEmpty()) {
                                scrollTo(i);
                                mUXToolkit.showAlertDialogue(
                                        mContext.getString(R.string.validation_error_title)
                                        , mContext.getString(
                                                R.string.question_condition_error_message
                                                ,mContext.getErrorStatementProvider().getStatement(eCode)
                                                , "13"
                                        )
                                );
                                return;
                            } else {
                                question.lock();
                                if (scrollToNextUnlockedQuestion(i + 1))
                                    return;
                            }
                        } else {
                            scrollTo(i);
                            mUXToolkit.showAlertDialogue(
                                    mContext.getString(R.string.validation_error_title)
                                    , mContext.getString(
                                            R.string.validation_error_message,
                                            question.getValidationErrorStatement()
                                            , question.getValidationRuleStatement()
                                            , "13"
                                    )
                            );
                            return;
                        }

                    }
                } else {
                    if(
                        (questionNumber + 2) == mQuestions.size()
                        || (questionNumber + 1) == mQuestions.size()
                    ) {
                        break;
                    }else {
                        if(scrollToNextUnlockedQuestion(i+1)) return;
                    }
                }
            } else {
                if((i+1) < mQuestions.size() && question.getState() != QuestionStates.LOCKED) {
                    quickScrollTo(i,true);
                    question.requestInputFocus();
                    return;
                }
            }
        }

        askNextQuestion(force, false, questionNumber);
    }

    private void askNextQuestion(boolean force, boolean skipJump, int questionNumber) {
        Question CQ;
        if(questionNumber > -1)
            CQ = mQuestions.get(mQuestions.size()-1);
        else CQ = null;

        if(CQ != null && CQ.validateAnswer() && !force) {
            String eCode = CQ.ExecPostCondition(mContext.getQuestionnaireManager(), CQ);
            if(eCode != null && !eCode.isEmpty()){
                mUXToolkit.showAlertDialogue(
                        mContext.getString(R.string.validation_error_title)
                        , mContext.getString(
                                R.string.question_condition_error_message
                                ,mContext.getErrorStatementProvider().getStatement(eCode)
                                , "13"
                        )
                );
                return;
            }

            if (CQ.getAdapter() instanceof SingularInputAdapter) {
                SingularInputAdapter adapter = (SingularInputAdapter) CQ.getAdapter();
                if (adapter.getJumps() != null && !skipJump) {
                    int fromSection = mContext.getSectionNumber();
                    if (adapter.getJumps().size() == 1) {
                        if (takeJump(fromSection, adapter.getJumps().get(0)))
                            return;
                    } else if (adapter.getJumps().size() > 1) {
                        mUXToolkit.showToast("Multiple Jumps received from Validator, Took action on first one");
                        if (takeJump(fromSection, adapter.getJumps().get(0)))
                            return;
                    }
                }
            }
        } else {
            //Either no question or Invalid answer
            if(questionNumber == mQuestions.size()-1) { // if askNextQuestion()'s caller is last one
                if (CQ != null && !CQ.validateAnswer() && !force) {
                    mUXToolkit.showAlertDialogue(
                            mContext.getString(R.string.validation_error_title)
                            , mContext.getString(
                                    R.string.validation_error_message,
                                    CQ.getValidationErrorStatement()
                                    , CQ.getValidationRuleStatement()
                                    , "13"
                            )
                    );
                    return;
                }
            } else {
                if((questionNumber + 2) == mQuestions.size() && mQuestions.get(questionNumber + 1).getState() == QuestionStates.UNLOCKED){
                    quickScrollTo(questionNumber + 1, true);
                    return;
                }

                //Todo: remove below chaipee
                //Chaipee
//                if (mQuestions.get(questionNumber).getState() == QuestionStates.LOCKED){
//                    scrollToNextUnlockedQuestion(questionNumber, false);
//                    return;
//                }

                quickScrollTo(questionNumber,true);
                return;
            }
        }

        QuestionNavigationResponse response = mContext.getQuestionnaireManager().advanceQuestion();
        if (response.getStatusCode() == QuestionNavigationStatus.VALIDATION_OK) {
            int newItemPos = response.getDataCode().toInt();
            Objects.requireNonNull(mContext   //for RecyclerView
                    .getFormContainer()
                    .getAdapter())
                    .notifyItemInserted(newItemPos);

            mHandler.post(()-> {
                if(newItemPos > 0) {
                    mQuestions.get(newItemPos - 1).lock();
                }

                mHandler.post(()->{
                    quickScrollTo(newItemPos + 1);
                    mHandler.post(()->{
                        if (newItemPos < mQuestions.size() && mQuestions.get(newItemPos) instanceof QuestionHeader) {
                            askNextQuestion();
                        } else if (newItemPos < mQuestions.size()){
                            mQuestions.get(newItemPos).requestFocus();
                            mHandler.postDelayed(() -> {
                                if (mQuestions.get(newItemPos).getState() != QuestionStates.LOCKED) {
                                    mQuestions.get(newItemPos).requestInputFocus();
                                }
                            }, 50);
                        }
                    });
                });
            });

        } else if(response.getStatusCode() == QuestionNavigationStatus.VALIDATION_FAILED){
            mUXToolkit.showAlertDialogue(
                    mContext.getString(R.string.validation_error_title)
                    , mContext.getString(
                            R.string.question_condition_error_message
                            , mContext.getErrorStatementProvider().getStatement(response.getDataCode().toString())
                            , "13"
                    )
            );
        } else {
            if (!mContext.getQuestionnaireManager().isSectionEnded()) {
                if(!verifyQuestionsStatuses())
                    return;

                int newItemPos = mContext
                        .getQuestionnaireManager()
                        .endSection(mContext.getActionQuestion());

                Objects.requireNonNull(mContext  //for RecyclerView
                        .getFormContainer()
                        .getAdapter())
                        .notifyItemInserted(newItemPos);

                mContext.getFormContainer().post(()-> {
                    if(newItemPos > 0) {
                        mQuestions.get(newItemPos - 1).lock();
                    }

                    mHandler.post(()->{
                        scrollTo(newItemPos+1);
                        mHandler.post(() -> {
                            mContext.getUXToolkit().hideKeyboardFrom(null);
                            mQuestions.get(newItemPos).requestFocus();
                        });
                    });
                });
            }
        }
    }

    public boolean verifyQuestionsStatuses(){
        //Verify if any question need to be checked before fetching Action Question
        for (int i = 0; i < mQuestions.size() - 1; i++){
            if(mQuestions.get(i).getState() == QuestionStates.READ_ONLY)
                continue;

            QuestionStates states = mQuestions.get(i).getState();
            if(states != QuestionStates.LOCKED && states != QuestionStates.ANSWERED){
                quickScrollTo(i,true);
                mQuestions.get(i).requestInputFocus();
                return false;
            }
        }
        return true;
    }

    /**
     * This method returns the number of questions which require answer from enumerator
     * and answer is stored in database. In other words it excludes Header and ActionQuestion
     *
     * @return number of interactive questions
     */
    public int getQuestionsOnlyCount(){
        int count = 0;
        for (int i = 0; i < mQuestions.size(); i++){
            if(mQuestions.get(i) instanceof QuestionHeader || mQuestions.get(i) instanceof QuestionActor)
                continue;

            count++;
        }

        return count;
    }
}
