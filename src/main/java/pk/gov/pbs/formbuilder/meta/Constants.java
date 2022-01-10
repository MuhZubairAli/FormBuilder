package pk.gov.pbs.formbuilder.meta;

public class Constants {
    public static final boolean DEBUG_MODE = pk.gov.pbs.utils.Constants.DEBUG_MODE;
    public static final boolean CAN_LOCATION_BE_OUTSIDE_ACCURACY_CIRCLE = false;
    public static final boolean CAN_LOCATION_BE_OUTSIDE_BLOCK = false;

    public static final int DATABASE_VERSION = 51;

    public static final int INVALID_NUMBER = -1;
    public static final char INVALID_CHAR = '\0';

    public static final int INPUT_MAX_NUMBERS_LIMIT = 9;
    public static final int INPUT_PHONE_CHARACTERS_LIMIT = 14;
    public static final int INPUT_MAX_CHARACTERS_LIMIT = 255;

    public static final float ANIM_SHOW_TRANSLATE_Y =-5f;
    public static final float ANIM_HIDE_TRANSLATE_Y = 5f;
    public static final long ANIM_DURATION = 500;

    public static class Index {
        public static final String QUESTION_SECTION_END = "section_ended";
        public static final String LABEL_BTN_NEXT_SECTION = "btn_go_next";
        public static final String LABEL_BTN_PARTIAL_REFUSE = "btn_partial_refuse";
        public static final String LABEL_BTN_REFUSED = "btn_refuse";
        public static final String LABEL_BTN_REPEAT = "btn_repeat_for_next_member";
        public static final String LABEL_BTN_NEXT_HH = "btn_go_next_hh";
        public static final String LABEL_BTN_UPDATE = "btn_update_section";
        public static final String LABEL_BTN_NEXT_ITERATION = "btn_repeat_for_next_iteration";

        public static final String SHARED_PREFERENCES_CONTAINER = "app_preferences_theme";
        public static final String SHARED_PREFERENCE_THEME = "themeIndex";

        // Intents indexes
        public static final String INTENT_EXTRA_FORM_CONTEXT = "form_section_context";
        public static final String INTENT_EXTRA_ASSIGNMENT = "block_household_assignments";
        public static final String INTENT_EXTRA_FORM_STATUS = "action_close_form_status";
        public static final String INTENT_EXTRA_FORM_MODEL = "action_form_model";
    }

    public static class Status {
        public static final int SECTION_OPENED = 1; //section have been opened/paused and incomplete (editable) yet
        public static final int SECTION_CLOSED = 2; //section is closed for single member household and is complete

        public static final int FORM_PROCEED = 0;
        public static final int FORM_COMPLETED = 1;
        public static final int FORM_PARTIALLY_REFUSED = 2;
        public static final int FORM_REFUSED = 3;
        public static final int FORM_NON_CONTACTED = 4;
        public static final int FORM_INCOMPLETE_POSTPONED = 5;

    }
}
