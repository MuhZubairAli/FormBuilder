package pk.gov.pbs.formbuilder.core;

import pk.gov.pbs.formbuilder.inputs.singular.ButtonInput;
import pk.gov.pbs.formbuilder.inputs.singular.adapters.ButtonInputAdapter;
import pk.gov.pbs.formbuilder.meta.Constants;

public class QuestionButtonGroup extends Question {

    public QuestionButtonGroup(ButtonInput[] singularInputs){
        super(
            Constants.Index.QUESTION_SECTION_END,
            new ButtonInputAdapter(singularInputs)
        );
    }

    public QuestionButtonGroup(String questionIndex, ButtonInput[] singularInputs){
        super(
            questionIndex,
            new ButtonInputAdapter(singularInputs)
        );
    }
}
