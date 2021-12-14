package pk.gov.pbs.formbuilder.meta;

public enum QuestionStates {
    PENDING,
    // no connection with lock/unlock only tells if question object has answer,
    // and it is for question which is not yet inflated
    ANSWERED,
    UNLOCKED,
    LOCKED,
    READ_ONLY
}
