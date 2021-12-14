package pk.gov.pbs.formbuilder.validator;

import pk.gov.pbs.formbuilder.utils.ValueStore;
import pk.gov.pbs.formbuilder.pojos.Jump;

public class PositiveNumberValidator extends GreaterThanValidator {
    public PositiveNumberValidator() {
        super(new ValueStore(-1));
    }

    public PositiveNumberValidator(Jump[] jumps) {
        super(new ValueStore(-1), jumps);
    }
}
