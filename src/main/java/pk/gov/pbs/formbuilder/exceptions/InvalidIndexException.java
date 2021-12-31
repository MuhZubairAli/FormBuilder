package pk.gov.pbs.formbuilder.exceptions;

public class InvalidIndexException extends Exception {
    public InvalidIndexException(){
        super("Invalid index provided");
    }

    public InvalidIndexException(Object index){
        super("Invalid index provided as : " + index);
    }

    public InvalidIndexException(Object index, String because){
        super("Invalid index provided as : " + index + " | because it must be : " + because);
    }
}
