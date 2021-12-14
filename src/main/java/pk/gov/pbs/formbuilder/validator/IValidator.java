package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.pojos.Jump;

public interface IValidator {
    Jump validate(ValueStore data);
    boolean isValid(ValueStore data);
    String getErrorStatement();
    String getRuleStatement();
}
