package pk.gov.pbs.formbuilder.inputs.abstracts.input;
import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.toolkits.NavigationToolkit;

public abstract class SingularInput extends Askable {
    public SingularInput(String index, int resId) {
        super(index, resId);
    }

    /**
     * Singular inputs are the one which use the zero index in mAnswers array in askable
     * whole question is single entity as opposed to GroupInput where length of mAnswer array
     * can not be relied upon. while in case case this class fore-mentioned index of mAnswer
     * is reliable and it is checked in validators. SingularInput could have multiple answers
     * but extra answers are dealt case by case, for example AnnexInout and SpecifiableInputs
     * @param answer answer to set at zero position
     * @return true always
     */
    public boolean setAnswer(ValueStore answer){
        return super.setAnswers(answer);
    }

    public ValueStore getAnswer(){
        if (mAnswers != null && mAnswers.length > 0)
            return mAnswers[0];
        return null;
    }

    @Override
    public void setupImeAction(NavigationToolkit toolkit) {}
}
