package pk.gov.pbs.formbuilder.utils;

import java.util.ArrayList;
import java.util.List;

import pk.gov.pbs.formbuilder.R;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.utils.Application;
import pk.gov.pbs.utils.ThemeUtils;

public class FormBuilderThemeHelper extends ThemeUtils {

    public static final List<String> THEMES_LABELS_LIST = new ArrayList<>();
    public static final List<Integer> THEMES_RESOURCES_LIST = new ArrayList<>();

    static {
        FormBuilderThemeHelper.THEMES_LABELS_LIST.add("Light Theme");
        FormBuilderThemeHelper.THEMES_LABELS_LIST.add("Dark Theme");

        FormBuilderThemeHelper.THEMES_RESOURCES_LIST.add(R.style.DayLight);
        FormBuilderThemeHelper.THEMES_RESOURCES_LIST.add(R.style.DawnDark);
    }

    public static int getThemeResId(int themeIndex){
        return THEMES_RESOURCES_LIST.get(themeIndex);
    }

    public static int getCurrentThemeResId(){
        return FormBuilderThemeHelper.getThemeResId(
                Application.getSharedPreferencesManager()
                        .getInt(Constants.Index.SHARED_PREFERENCE_THEME, 0)
        );
    }
}
