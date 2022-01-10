package pk.gov.pbs.formbuilder.core;

import pk.gov.pbs.formbuilder.inputs.singular.ButtonInput;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.ButtonInputAdapter;
import pk.gov.pbs.formbuilder.meta.Constants;

public class QuestionActor extends Question {

    public QuestionActor(ButtonInput[] singularInputs){
        super(
            Constants.Index.QUESTION_SECTION_END,
            new ButtonInputAdapter(singularInputs)
        );
    }

    public QuestionActor(String questionIndex, ButtonInput[] singularInputs){
        super(
            questionIndex,
            new ButtonInputAdapter(singularInputs)
        );
    }
}
