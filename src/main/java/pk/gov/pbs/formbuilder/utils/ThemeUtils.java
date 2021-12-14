package pk.gov.pbs.formbuilder.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;

import pk.gov.pbs.formbuilder.R;

public class ThemeUtils {
    public static void applyThemedDrawableToView(View view, int resId){
        Context context = view.getContext();
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[] {resId});
        int attributeResourceId = a.getResourceId(0, 0);
        Drawable drawable = context.getResources().getDrawable(attributeResourceId);
        view.setBackground(drawable);
        a.recycle();
    }

    public static int getColorByTheme(Context context, int resID){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(resID, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }
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
