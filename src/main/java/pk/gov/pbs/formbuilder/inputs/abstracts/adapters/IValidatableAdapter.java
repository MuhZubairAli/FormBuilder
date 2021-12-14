package pk.gov.pbs.formbuilder.inputs.abstracts.adapters;

public interface IValidatableAdapter {

    /**
     * Validation method to access relevant function
     * GroupedInput shall compile message from all askables having validator
     * and perform validation check on said GroupInput
     */
    boolean performValidationCheck();
    String getValidationErrorStatement();
    String getValidationRulesStatement();
}
