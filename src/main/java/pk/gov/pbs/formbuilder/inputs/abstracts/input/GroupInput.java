package pk.gov.pbs.formbuilder.inputs.abstracts.input;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.validator.Validator;

public abstract class GroupInput extends Askable {
    protected final ValueStore[] mExtras;
    protected Validator mValidator;

    public GroupInput(String index, int resource, String... extras) {
        super(index, resource);

        if (extras != null && extras.length > 0 && extras[0] != null) {
            if (extras.length > 25)
                throw new IllegalArgumentException("Too many extras provided to the GroupInput, Maximum acceptable number of extras is 25");

            mExtras = new ValueStore[extras.length];
            for (int i=0; i<extras.length; i++)
                mExtras[i] = new ValueStore(extras[i]);

        } else
            mExtras = null;
    }

    public GroupInput(String index, int resource, Validator validator, String... extras) {
        this(index, resource, extras);
        mValidator = validator;
    }

    public final ValueStore[] getExtras(){
        return mExtras;
    }

    public final ValueStore getExtra(int index){
        if (hasExtras() && index < mExtras.length)
            return mExtras[index];

        throw new IllegalArgumentException("invalid index for extra index = "+index+", mExtras.length = "+ (hasExtras() ? mExtras.length : 0));
    }

    public final boolean hasExtras(){
        return mExtras != null;
    }

    protected boolean canSkipValidate(){
        return !isVisible() || !hasValidator();
    }

    public void hideUnanswered(){
        if (!hasAnswer())
            quickHide();
    }

    public void showAll(){
        quickShow();
    }
    /**
     * Validation methods
     */
    public boolean hasValidator(){
        return mValidator != null;
    }
    public Validator getValidator(){
        return mValidator;
    }

    public abstract boolean validateAnswer();
    public abstract boolean hasIndex(String abIndex);
}
