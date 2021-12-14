package pk.gov.pbs.formbuilder.validator;

import java.util.ArrayList;
import java.util.List;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.pojos.Jump;

public abstract class Validator implements IValidator{
    public enum Operator { AND, OR }
    protected Jump[] jumps;
    protected Boolean result;
    protected List<Validator> andConjuncts;
    protected List<Validator> orConjuncts;

    public Validator(){
        jumps = null;
        result = null;
    }

    public Validator(Jump[] jumps){
        this.jumps = jumps;
        result = null;
    }

    public Jump validate(ValueStore data){
        result = isValid(data); // && conjunction;
        if(result){
            if(jumps != null && jumps.length > 0){
                for (Jump jump : jumps){
                    if (jump.isApplicable(data))
                        return jump;
                }
            }
            return new Jump(data);
        }
        return null;
    }

    public boolean isValid(ValueStore answer){
        result = predicate(answer);
        return result;
    }

    public Validator addConjunct(Operator by, Validator validator){
        if (by == Operator.AND){
            if (andConjuncts == null)
                andConjuncts = new ArrayList<>();
            andConjuncts.add(validator);
        } else if (by == Operator.OR){
            if (orConjuncts == null)
                orConjuncts = new ArrayList<>();
            orConjuncts.add(validator);
        }
        return this;
    }

    public boolean hasJumps(){
        return jumps != null && jumps.length > 0;
    }

    public void reset(){
        this.result = null;
    }

    protected abstract boolean predicate(ValueStore valueStore);
    //Todo: Create mechanism for obtaining statements according to locale
    public abstract String getErrorStatement();
    public abstract String getRuleStatement();
}
