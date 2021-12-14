package pk.gov.pbs.formbuilder.exceptions;

public class InvalidQuestionStateException extends Exception{
    public InvalidQuestionStateException() {
        super("QuestionState of one or more questions is invalid, make sure only one question is unlocked and rest are locked.");
    }

    public InvalidQuestionStateException(String message) {
        super(message);
    }
}
