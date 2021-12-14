package pk.gov.pbs.formbuilder.exceptions;

public class IllegalMethodCallException extends Exception {
    public Object context;

    public IllegalMethodCallException(Object context){
        super("This method call is not allowed, Please verify if you need to cast the operand object before calling this method Or verify parameter");
        this.context = context;
    }

    public IllegalMethodCallException(String msg, Object context){
        super(msg);
        this.context = context;
    }
}
