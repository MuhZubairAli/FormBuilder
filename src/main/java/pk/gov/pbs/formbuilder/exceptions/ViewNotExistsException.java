package pk.gov.pbs.formbuilder.exceptions;

public class ViewNotExistsException extends Exception {
    public ViewNotExistsException(){
        super("Required View is not inflated or reference is not set for inputElement");
    }
}
