package pk.gov.pbs.formbuilder.exceptions;

import pk.gov.pbs.formbuilder.inputs.abstracts.input.Askable;

public class UnknownAskableTypeException extends Exception {
    public Askable singularInput;

    public UnknownAskableTypeException(Askable ab){
        super("The implementation of Askable is Unknown");
        singularInput = ab;
    }
}
