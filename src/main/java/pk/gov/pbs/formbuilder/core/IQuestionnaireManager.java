package pk.gov.pbs.formbuilder.core;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import pk.gov.pbs.formbuilder.utils.ValueStore;

public interface IQuestionnaireManager {
    boolean isSectionEnded();
    ArrayList<Question> getQuestions();
    Question getQuestion(String qIndex);
    ValueStore[][] getAnswerFrom(String key);
    void changeQuestionRangeToEnd();
    void changeQuestionRangeNext(String start);
    void changeQuestionRange(String start);
    ErrorStatementProvider getErrorStatement();
    ActivityFormSection getContext();
    ViewModelFormSection getViewModel();
    void handle(Runnable run);
    QuestionnaireManager.OnAnswerHandlerExecutor executeInNewThread(
            Question question,
            IOnAnswerHandler handler,
            @Nullable Runnable... onPostExecuteCallback
    );
}
