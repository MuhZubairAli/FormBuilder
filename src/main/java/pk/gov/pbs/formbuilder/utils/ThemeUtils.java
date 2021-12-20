package pk.gov.pbs.formbuilder.utils;

import pk.gov.pbs.formbuilder.R;

public class ThemeUtils extends pk.gov.pbs.utils.ThemeUtils {
    public static String[] themeList = new String[]{
            "Light Theme"
            , "Dark Theme"
            //, "Colorful Theme"
    };

    public static int getThemeResId(int themeIndex){
        int[] themeIndexToResId = new int[]{
                R.style.DayLight
                , R.style.DawnDark
        };

        return themeIndexToResId[themeIndex];
    }
}
