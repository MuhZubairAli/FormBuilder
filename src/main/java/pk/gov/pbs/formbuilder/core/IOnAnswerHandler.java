package pk.gov.pbs.formbuilder.core;

public interface IOnAnswerHandler {
    boolean handle(IQuestionnaireManager manager, Question question);
}
