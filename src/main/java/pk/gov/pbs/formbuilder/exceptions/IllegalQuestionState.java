package pk.gov.pbs.formbuilder.exceptions;

import pk.gov.pbs.formbuilder.core.Question;

public class IllegalQuestionState extends Exception {
    Question question;

    public IllegalQuestionState(Question q){
        super("Questions is in illegal or un-processable state");
        this.question = q;
    }

    public Question getQuestion() {
        return question;
    }
}
