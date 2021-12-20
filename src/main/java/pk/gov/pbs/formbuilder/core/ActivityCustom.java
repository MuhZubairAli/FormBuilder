package pk.gov.pbs.formbuilder.core;

import android.os.Bundle;

import androidx.annotation.Nullable;

import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.utils.ThemeUtils;
import pk.gov.pbs.utils.Application;
import pk.gov.pbs.utils.CustomActivity;
public abstract class ActivityCustom extends CustomActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(
                ThemeUtils.getThemeResId(
                        Application.getSharedPreferencesManager()
                                .getInt(Constants.Index.SHARED_PREFERENCE_THEME, 0)
                )
        );
        super.onCreate(savedInstanceState);
    }
}
