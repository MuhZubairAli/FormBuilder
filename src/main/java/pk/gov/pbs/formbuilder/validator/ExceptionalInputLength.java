package pk.gov.pbs.formbuilder.validator;

/**
 * For those validators who allow input limit beyond Global Defaults
 * as defined in below constants
 *      Constants.INPUT_MAX_NUMBERS_LIMIT
 *      Constants.INPUT_MAX_CHARACTERS_LIMIT
 *      Constants.INPUT_PHONE_CHARACTERS_LIMIT
 * This must be used care fully or else Keyboard input might be abused
 */
public interface ExceptionalInputLength {
    int getMaxLength();
}
