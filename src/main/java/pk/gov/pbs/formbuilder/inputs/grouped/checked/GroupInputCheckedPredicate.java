package pk.gov.pbs.formbuilder.inputs.grouped.checked;

import pk.gov.pbs.formbuilder.utils.ValueStore;

public interface GroupInputCheckedPredicate {
    boolean predicate(ValueStore[] answers);
}
