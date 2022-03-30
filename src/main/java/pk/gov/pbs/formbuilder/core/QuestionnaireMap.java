package pk.gov.pbs.formbuilder.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.meta.QuestionStates;
import pk.gov.pbs.formbuilder.models.Section;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.utils.ExceptionReporter;

public abstract class QuestionnaireMap {
    private HashMap<String, Integer> indexes;
    private ArrayList<Question> map;
    private int mapIndex;
    private String o_start;
    private String o_end;
    private int start;
    private int end;
    private ValueStore[] mExtraData;
    private ImportStrategy mImportStrategy;
    private boolean mReadOnly = false;
    protected QuestionnaireBuilder __;
    //Todo: remove this temporary chapee when have time
    //Todo: remove it by invoking questionConditions here
    private boolean mSkipPendingQuestionsWhileResuming = true;

    protected QuestionnaireMap(){}

    public QuestionnaireMap(QuestionnaireBuilder qBuilder, ValueStore... extraData) {
        mExtraData = extraData;
        construct(qBuilder, extraData);
    }

    public QuestionnaireMap(String startFrom, QuestionnaireBuilder qBuilder, ValueStore... extraData) {
        mExtraData = extraData;
        construct(startFrom, qBuilder, extraData);
    }

    public QuestionnaireMap(String startFrom, String endTo, QuestionnaireBuilder qBuilder, ValueStore... extraData) {
        mExtraData = extraData;
        construct(startFrom, endTo, qBuilder, extraData);
    }

    protected void construct(QuestionnaireBuilder qBuilder, ValueStore... extraData) {
        this.map = new ArrayList<>();
        this.indexes = new HashMap<>();
        this.__ = qBuilder;
        generateMap(extraData);
        initIndexes();
    }

    protected void construct(String startFrom, QuestionnaireBuilder qBuilder, ValueStore... extraData) {
        this.__ = qBuilder;
        this.map = new ArrayList<>();
        this.indexes = new HashMap<>();
        this.o_start = startFrom;
        generateMap(extraData);
        initIndexes();
    }

    protected void construct(String startFrom, String endTo, QuestionnaireBuilder qBuilder, ValueStore... extraData) {
        construct(startFrom, qBuilder);
        this.o_end = endTo;
        initIndexes();
    }

    public void setNoSkippingWhileResuming(){
        mSkipPendingQuestionsWhileResuming = false;
    }
    public void setAsReadOnly(){
        mReadOnly = true;
    }

    public void setModel(Section model){
        __.setModel(model);
    }

    public QuestionnaireMap setTradeStrategy(ImportStrategy importStrategyListener){
        mImportStrategy = importStrategyListener;
        if (map.size() > 0)
            importStrategyListener.importStrategy(this);

        return this;
    }

    public QuestionnaireBuilder getQuestionBuilder() {
        return __;
    }

    public Question insertQuestion(Question question){
        indexes.put(question.getIndex(), map.size());
        map.add(question);
        return question;
    }

    public void insertHeader(String index){
        insertQuestion(new QuestionHeader(index));
    }

    public void markReadOnly(String qIndex){
        if(indexes.containsKey(qIndex) && indexes.get(qIndex) != null)
            map.get(indexes.get(qIndex)).setState(QuestionStates.READ_ONLY);
    }

    public void applyJumpToEnd(){
        mapIndex = map.size() - 1;
    }

    public boolean applyJump(String start, @Nullable String end){
        if(indexes.containsKey(start) && indexes.get(start) != null)
            mapIndex = indexes.get(start) - 1;
        else
            mapIndex = -1;

        if(end != null && indexes.containsKey(end) && indexes.get(end) != null)
            this.end = indexes.get(end);

        return true;
    }

    public boolean applyJump(String start){
        return applyJump(start, null);
    }

    /**
     * this question update mapIndex in a way that when
     * question will fetched it will return next question to the given Question Index
     * @param index index of the question to be set as current
     */
    public void setCurrentQuestion(String index){
        if(indexes.containsKey(index))
            mapIndex = indexes.get(index);
        else
            mapIndex = -1;

    }

    public List<Question> getQuestions(){
        return map;
    }

    public Question getQuestion(String qIndex){
        if (indexes.containsKey(qIndex))
            return map.get(indexes.get(qIndex));
        return null;
    }

    public Question getQuestion(int index){
        if (index > -1 && index < map.size())
            return map.get(index);
        return null;
    }

    private List<Question> getQuestionsRange(int start, int end){
        return map.subList(start, end);
    }

    public List<Question> getQuestionsRange(@NonNull String startIndex, @NonNull String endIndex){
        if(indexes.containsKey(startIndex) && indexes.containsKey(endIndex))
            return getQuestionsRange(indexes.get(startIndex), indexes.get(endIndex));
        else
            return null;
    }

    //Todo: update this method to not use chapee, instead call post and pre conditions to determine which questions to be skipped
    public List<Question> getResumedQuestions(){
        mapIndex = -1;
        List<Question> questionList = new ArrayList<>();
        for(int i = map.size()-1; i >= 0; i--){
            if (mapIndex != -1 && map.get(i) instanceof QuestionHeader) {
                questionList.add(map.get(i));
                continue;
            }

            if(mSkipPendingQuestionsWhileResuming && map.get(i).getState() == QuestionStates.PENDING)
                continue;

            if(mapIndex == -1 && map.get(i).getState() == QuestionStates.ANSWERED)
                mapIndex = i;

            if (mapIndex == -1)
                continue;

            if(mReadOnly){
                Question question = map.get(i);
                question.setState(QuestionStates.READ_ONLY);
                questionList.add(question);
            } else {
                if (map.get(i).getState() == QuestionStates.PENDING)
                    map.get(i).setState(QuestionStates.LOCKED);
                questionList.add(map.get(i));
            }

        }
        return questionList;
    }

    public List<Question> getQuestionsAndReset(@NonNull String index){
        if(indexes.containsKey(index)) {
            List<Question> questionList = new ArrayList<>();
            int qIndex = indexes.get(index);
            for (int i = 0; i <= qIndex; i++){
                if(map.get(i).getState() != QuestionStates.PENDING && !map.get(i).isRemoved()){
                    questionList.add(map.get(i));
                }
            }
            //unlocking last question (which is critical and being reset)
            map.get(qIndex).unlock(true);
            //unlocking all future questions
            for (int i = qIndex+1; i < map.size(); i++){
                if(map.get(i).getState() != QuestionStates.PENDING){
                    map.get(i).unlock(true);
                    map.get(i).setRemoved(true);
                }
            }
            return questionList;
        }
        else
            return null;
    }

    public Question getNextQuestion(){
        if(hasNextQuestion()) {
            map.get(mapIndex+1).setRemoved(false);
            return map.get(++mapIndex);
        }
        return null;
    }

    public boolean hasNextQuestion(){
        if((mapIndex + 1) >= start)
            if(((mapIndex + 1) <= end) || end == Constants.INVALID_NUMBER)
                return ((mapIndex + 1) < map.size());
        return false;
    }

    public void initIndexes(){
        //Resetting boundary
        if(o_start != null)
            start = (indexes.containsKey(o_start)) ? indexes.get(o_start) : 0;
        else
            start = 0;
        mapIndex = start - 1;

        if(o_end != null)
            end = (indexes.containsKey(o_end)) ? indexes.get(o_end) : Constants.INVALID_NUMBER;
        else
            end = Constants.INVALID_NUMBER;
    }

    public void reset(){
        initIndexes();
        map.clear();
        indexes.clear();
        generateMap(mExtraData);
    }

    public int getQuestionCount(){
        return map.size();
    }

    public int getCurrentQuestionIndex(){
        return mapIndex;
    }

    public int getQuestionIndex(String qIndex){
        if (indexes.containsKey(qIndex))
            return indexes.get(qIndex);
        return Constants.INVALID_NUMBER;
    }

    private void generateMap(ValueStore... extraData) {
        try {
            initQuestions(extraData);

            if (mImportStrategy != null)
                mImportStrategy.importStrategy(this);
        } catch (Exception e) {
            ExceptionReporter.printStackTrace(e);
        }
    }

    protected abstract void initQuestions(ValueStore... extraData) throws Exception;

    /**
     * This interface is used to define trade of data (import of answers) map questions and model(s)
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
    public interface ImportStrategy {
        void importStrategy(QuestionnaireMap map);
    }
}
