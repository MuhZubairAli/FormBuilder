package pk.gov.pbs.formbuilder.core;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.inputs.abstracts.input.Askable;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.KeyboardInputAdapter;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.RadioInputAdapter;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.SpinnerInputAdapter;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.meta.QuestionNavigationStatus;
import pk.gov.pbs.formbuilder.meta.QuestionStates;
import pk.gov.pbs.formbuilder.models.HouseholdSection;
import pk.gov.pbs.formbuilder.pojos.QuestionNavigationResponse;
import pk.gov.pbs.utils.ExceptionReporter;
import pk.gov.pbs.utils.StaticUtils;
import pk.gov.pbs.utils.SystemUtils;

/**
 * Todo: #1 Merge NavigationToolkit into this class and expose navigation related methods via Navigation Interface
 * Todo: #2 expose data related method (like getters of questions, answers, viewModel, errorStatementProviders etc) via some Data Provider Interface
 * Todo: #3 update executeInNewThread method to use ExecutorService to avoid extra work of creating and tear down of thread
 * and use that data provider in OnAnswerEventListener#onAnswer (in AskableAdapter) instead of IQuestionnaireManager instance
 * while in Question#Condition (post and pre) provide both Navigation and Data provider Interfaces
 *
 * @param <T> Type of Primary Model (default model of section defined by MetaManifest)
 */
public abstract class QuestionnaireManager<T extends Section> implements IQuestionnaireManager {
    private static final HashMap<String, OnAnswerHandlerExecutor> mOnAnswerTasks = new HashMap<>();
    //private static final int mExecutorThreadCount = 1;
    //private static final ExecutorService mExecutorService = Executors.newFixedThreadPool(mExecutorThreadCount);

    protected final ActivityFormSection mContext;
    protected final ArrayList<Question> mQuestions;
    protected final HashMap<String, Integer> mQuestionIndexes;
    protected final HashMap<String, Runnable> mLabelPlaceholderFiller;
    protected OnAdvanceQuestion mOnAdvanceQuestion;
    protected int mQuestionCount;
    protected boolean mSectionEnded;
    protected boolean mSectionSkipped;

    public abstract T exportPrimaryModel();
    public T updatePrimaryModel() {
        T model = null;
        if(mContext.getViewModel().getResumeModel() != null){
            T rm = (T) mContext.getViewModel().getResumeModel();
            model = this.exportPrimaryModel();

            //not necessary yet
            //model.setFormContext(rm.getFormContext());

            model.aid = rm.aid;
            model.ts_created = rm.ts_created;
            model.ts_updated = SystemUtils.getUnixTs();
        }
        return model;
    }

    public QuestionnaireManager(ActivityFormSection context){
        mContext = context;
        mQuestions = new ArrayList<>();
        mQuestionIndexes = new HashMap<>();
        mLabelPlaceholderFiller = new HashMap<>();
        mQuestionCount = context.getMap().getQuestionCount();

        mSectionEnded = false;
        mSectionSkipped = false;
    }

    @Override
    public void handle(Runnable run) {
        StaticUtils.getHandler().post(run);
    }

    @Override
    public OnAnswerHandlerExecutor executeInNewThread(Question question, IOnAnswerHandler handler, @Nullable Runnable... postExecuteCallbacks) {
        OnAnswerHandlerExecutor executor = mOnAnswerTasks.get(question.getIndex());
        if (executor == null) {
            executor = new OnAnswerHandlerExecutor(question, handler, postExecuteCallbacks);
            mOnAnswerTasks.put(question.getIndex(), executor);
        }

        if (executor.getStatus() == AsyncTask.Status.RUNNING)
            return executor;

        executor.execute(this);
        return executor;
    }

    public ViewModelFormSection getViewModel(){
        return mContext.getViewModel();
    }

    @Override
    public ErrorStatementProvider getErrorStatement() {
        return mContext.getErrorStatementProvider();
    }

    public ActivityFormSection getContext(){
        return mContext;
    }

    public void changeQuestionRange(String start, @Nullable String end){
        mContext.getMap().applyJump(start,end);
    }

    //Todo: Remove ambiguity between changeQuestionRange and changeQuestionRangeNext
    public void changeQuestionRange(String start){
        mContext.getMap().applyJump(start);
    }

    public void changeQuestionRangeNext(String start){
        mContext.getMap().setCurrentQuestion(start);
    }

    public void changeQuestionRangeToEnd(){
        mContext.getMap().applyJumpToEnd();
    }

    public void setOnAdvanceQuestionEvent(OnAdvanceQuestion event){
        mOnAdvanceQuestion = event;
    }

    private void manageAnswer(Question question){
        if(mLabelPlaceholderFiller.containsKey(question.getIndex())){
            mLabelPlaceholderFiller.get(question.getIndex()).run();
        }
    }

    public void addQuestion(Question question){
        int index = mQuestions.size();
        mQuestions.add(index, question);
        mQuestionIndexes.put(
                question.getIndex()
                , index
        );

        if (mOnAdvanceQuestion != null) {
            int qNo = (mSectionEnded) ? mQuestionCount : mContext.getMap().getCurrentQuestionIndex();
            mOnAdvanceQuestion.onAdvance(
                    qNo,
                    mQuestionCount
            );
        }
    }

    public QuestionNavigationResponse advanceQuestion(){
        if(mSectionSkipped)
            return new QuestionNavigationResponse(QuestionNavigationStatus.SECTION_END_REACHED, null);

        int questionCount = mQuestions.size();
        if(mContext.getMap().hasNextQuestion()){
            if(questionCount > 0) {
                manageAnswer(mQuestions.get(questionCount - 1));
            }
            Question question = mContext.getMap().getNextQuestion();
            if(question != null) {
                String eCode = question.ExecPreCondition(this, question);
                if (eCode == null && question.getState() != QuestionStates.ANSWERED)
                    addQuestion(question);
                else {
                    if (question.getState() == QuestionStates.ANSWERED) {
                        manageAnswer(question);
                        addQuestion(question);
                        return new QuestionNavigationResponse(QuestionNavigationStatus.VALIDATION_OK, new ValueStore(questionCount));
                    } else if(!eCode.isEmpty()){
                        return new QuestionNavigationResponse(QuestionNavigationStatus.VALIDATION_FAILED, new ValueStore(eCode));
                    } else
                        return advanceQuestion();
                }
            }
        }
        if(mQuestions.size() > questionCount)
            return new QuestionNavigationResponse(QuestionNavigationStatus.VALIDATION_OK, new ValueStore(questionCount)); //after addition last count of questions becomes index of new last question
        return new QuestionNavigationResponse(QuestionNavigationStatus.SECTION_END_REACHED, null);
    }

    public void addQuestion(List<Question> questions){
        for(Question q : questions){
            addQuestion(q);
        }
    }

    public ArrayList<Question> getQuestions(){
        return mQuestions;
    }

    public int getQuestionIndex(@NonNull String qIndex){
        if (mQuestionIndexes.containsKey(qIndex))
            return mQuestionIndexes.get(qIndex);
        return Constants.INVALID_NUMBER;
    }

    public String getQuestionIndex(int qIndex){
        if (mQuestions.size() > qIndex)
            return mQuestions.get(qIndex).getIndex();
        return null;
    }

    public void skipSection(){
        mSectionSkipped = true;
    }

    public int endSection(QuestionActor question) {
        if(!mSectionEnded){
            mSectionEnded = true;
            addQuestion(question);
            return (mQuestions.size() -1);
        }
        return QuestionNavigationStatus.SECTION_END_REACHED;
    }

    public boolean isSectionEnded(){
        return this.mSectionEnded;
    }

    public void insertQuestion(Question question){
        int index = mQuestions.size();
        mQuestions.add(index, question);
        mQuestionIndexes.put(
                question.getIndex()
                , index
        );
    }

    public void addToQuestionsAll(List<Question> questions){
        for(Question q : questions){
            insertQuestion(q);
        }
    }

    public void reset() {
        //don't reset Map here if you want to sleep peacefully
        mQuestions.clear();
        mQuestionIndexes.clear();
        mContext.getLabelProvider().reset();
        mLabelPlaceholderFiller.clear();
        mSectionEnded = false;
        mSectionSkipped = false;
    }

    public void resetTill(String qIndex) {
        for (int i = mQuestions.size()-1; i > mQuestionIndexes.get(qIndex); i--){
            mQuestions.get(i).reset();
            mQuestions.remove(i);
        }
        mSectionEnded = false;
    }

    public Question getQuestion(String qIndex){
        if(mQuestionIndexes.containsKey(qIndex))
            return mQuestions.get(mQuestionIndexes.get(qIndex));
        return null;
    }

    public Question getQuestion(int qIndex){
        if (qIndex < mQuestions.size())
            return mQuestions.get(qIndex);
        return null;
    }

    public ValueStore[][] getAnswerFrom(String key){
        if(
                mQuestionIndexes.containsKey(key)
                && mQuestionIndexes.get(key) != null
                && mQuestionIndexes.get(key) < mQuestions.size()
                && mQuestions.get(mQuestionIndexes.get(key)) != null
        )
            return mQuestions.get(mQuestionIndexes.get(key)).getAnswers();
        return null;
    }

    public void addLabelPlaceholder(String key, String as, String defaultValue){
        if(mLabelPlaceholderFiller.containsKey(key))
            return;

        mLabelPlaceholderFiller.put(key, () -> {
            try {
                if (mQuestionIndexes.containsKey(key)) {
                    if (mQuestions.get(mQuestionIndexes.get(key)) != null) {
                        if (
                                mQuestions.get(mQuestionIndexes.get(key)).getAnswers() != null
                                && mQuestions.get(mQuestionIndexes.get(key)).getAnswers().length > 0
                                && mQuestions.get(mQuestionIndexes.get(key)).getAnswers()[0] != null
                        ) {
                            mContext.getLabelProvider().addPlaceholder(
                                as
                                , mQuestions
                                    .get(mQuestionIndexes.get(key))
                                    .getAnswers()[0][0]
                                    .toString()
                            );
                        } else if (defaultValue != null)
                            mContext.getLabelProvider().addPlaceholder(as, defaultValue);
                        else
                            mContext.getLabelProvider().addPlaceholder(as, key);
                        //mLabelPlaceholderFiller.remove(key); // commented in case we need re-associate labels from model in resuming
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void addLabelPlaceholder(String key, String as){
        addLabelPlaceholder(key, as, null);
    }

    public void initLabelsPlaceholders(){
        if(mLabelPlaceholderFiller.size() > 0){
            for (Runnable r : mLabelPlaceholderFiller.values()){
                r.run();
            }
        }
    }

    protected T fillModel(T section) {
        if (mContext.getViewModel().getSectionContext() != null)
            section.setSectionContext(mContext.getViewModel().getSectionContext());
        for(int i = 0; i < mQuestions.size(); i++){
            if(
                mQuestions.get(i) instanceof QuestionActor ||
                mQuestions.get(i) instanceof QuestionHeader ||
                mQuestions.get(i).getState() == QuestionStates.UNLOCKED
            )
                continue;

            if(mQuestions.get(i).getAdapter() instanceof KeyboardInputAdapter){
                if(mQuestions.get(i).getAnswers() == null)
                    continue;

                try {
                    if (mQuestions.get(i).getAdapter().getAskables().length > 1) {
                        for (int j = 0; j < mQuestions.get(i).getAdapter().getAskables().length; j++) {
                            section.set(
                                    mQuestions.get(i)
                                            .getAdapter()
                                            .getAskables()[j]
                                            .getIndex()
                                    , mQuestions.get(i)
                                            .getAdapter()
                                            .getAskables()[j]
                                            .getAnswer(0)
                            );
                        }
                    } else {
                        section.set(mQuestions.get(i).getIndex(), mQuestions.get(i).getAnswers()[0][0]);
                    }
                }catch (NoSuchFieldException e) {
                    ExceptionReporter.printStackTrace(e);
                }catch (IllegalAccessException e){
                    ExceptionReporter.printStackTrace(e);
                }
            } else if(mQuestions.get(i).getAdapter() instanceof RadioInputAdapter){
                try {
                    ValueStore[][] answer = mQuestions.get(i).getAnswers();
                    if (answer != null && answer.length > 0 && answer[0] != null && answer[0].length > 0) {
                        section.set(
                                mQuestions.get(i).getIndex()
                                , answer[0][0]
                        );

                        if (answer[0].length == 2) {
                            section.set(
                                    "__".concat(mQuestions.get(i).getIndex())
                                    , answer[0][1]
                            );
                        }
                    }
                }catch (Exception e) {
                    ExceptionReporter.printStackTrace(e);
                }
            }
            else if(mQuestions.get(i).getAdapter() instanceof SpinnerInputAdapter){
                try {
                    if(mQuestions.get(i).getAdapter().getAskables().length >  1){
                        for(int j = 0; j < mQuestions.get(i).getAdapter().getAskables().length; j++) {
                            section.set(
                                    mQuestions.get(i).getAdapter().getAskables()[j].getIndex()
                                    , mQuestions.get(i).getAdapter().getAskables()[j].getAnswer(0)
                            );
                        }
                    }else
                        section.set(mQuestions.get(i).getIndex(),mQuestions.get(i).getAnswers()[0][0]);
                } catch (NoSuchFieldException e) {
                    ExceptionReporter.printStackTrace(e);
                }catch (IllegalAccessException e){
                    ExceptionReporter.printStackTrace(e);
                }
            }
            else {
                for (Askable gi : mQuestions.get(i).getAdapter().getAskables()) {
                    try {
                        gi.exportAnswer(section);
                    } catch (Exception e) {
                        ExceptionReporter.printStackTrace(e);
                    }
                }
            }
        }
        return section;
    }

    //Currently only used for CBI
    protected void overfit(HouseholdSection model, String qIndex, ValueStore[] answers) {
        if (answers == null || answers.length == 0)
            return;

        for (int count = 1; true; count ++){
            try {
                ValueStore ans = null;
                try {
                    ans = answers[count -1];
                } catch (IndexOutOfBoundsException ignore) {}

                model.set(
                        qIndex + count
                        , ans
                );

            } catch (Exception e) {
                ExceptionReporter.printStackTrace(e);
                break;
            }
        }
    }

    protected void overfit(HouseholdSection model, String qIndex, ValueStore[][] answers) {
        if (answers == null || answers.length == 0)
            return;

        if (answers.length == 1) {
            overfit(model, qIndex, answers[0]);
            return;
        }

        for (int count = 1; true; count ++){
            try {
                ValueStore ans = null;
                try {
                    ans = answers[count -1][0];
                } catch (IndexOutOfBoundsException ignore) {}

                model.set(
                        qIndex + count
                        , ans
                );

                if (answers[count-1].length == 2) {
                    try {
                        model.set(
                                "__" + qIndex + count
                                , answers[count-1][1]
                        );
                    } catch (Exception e){
                        ExceptionReporter.printStackTrace(e);
                    }
                }

            } catch (Exception e) {
                ExceptionReporter.printStackTrace(e);
                break;
            }
        }
    }

    /**
     * Being used to track form progress, after every insertion of new question
     * this handler is called irrespective of question type and state
     *
     */
    public interface OnAdvanceQuestion {
        void onAdvance(int currentQuestion, int totalQuestions);
    }

    /**
     * This interface is used to define trade of data (import & export of answers) map questions and model(s)
     * after initialization,
     * Data Trade means loading answers into question(s) with custom model(s)
     * or custom logic. Suppose there is section where any (or all) questions (or any askable) use custom model(s)
     * and after construction of map we have fetched those models from db and now need to load those into question's
     * askables, for this propose activity will provide it's implementation to map when constructed
     *
     * Normally activity don't need to implement it because resume model is automatically
     * loaded to into map if present in QuestionnaireBuilder during initialization of map where the
     * section use only single instance of the model
     */
    public interface ExportStrategy {
        List<?> exportModels(ArrayList<Question> questions);
    }

    static class OnAnswerHandlerExecutor extends AsyncTask<IQuestionnaireManager, Void, Boolean> {
        private Runnable[] onPostExecuteCallback;
        private final IOnAnswerHandler handler;
        private final Question question;

        public OnAnswerHandlerExecutor(Question question, IOnAnswerHandler handler){
            this.handler = handler;
            this.question = question;
        }

        public OnAnswerHandlerExecutor(Question question, IOnAnswerHandler handler, Runnable... onPostExecuteCallback){
            this(question, handler);
            this.onPostExecuteCallback = onPostExecuteCallback;
        }

        @Override
        protected Boolean doInBackground(IQuestionnaireManager... params) {
            return handler.handle(params[0], question);
        }

        @Override
        protected void onPostExecute(Boolean successful) {
            if (successful && onPostExecuteCallback != null && onPostExecuteCallback.length > 0) {
                for (Runnable callback : onPostExecuteCallback)
                    if (callback != null) callback.run();
            }

            mOnAnswerTasks.remove(question.getIndex());
        }
    }
}
