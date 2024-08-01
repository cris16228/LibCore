package com.github.cris16228.libcore;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

enum Theme {
    DARK,
    LIGHT,
    AUTO
}

public class ThemeUtils {

    //Theme
    public static final String THEME = "theme";
    public static final String LIGHT = "light";
    public static final String DARK = "dark";
    public static final String AUTO = "follow_system";
    public static String[] themes = new String[]{
            StringUtils.convertToFirstUpper(ThemeUtils.LIGHT), StringUtils.convertToFirstUpper(ThemeUtils.DARK),
            StringUtils.convertToFirstUpper(ThemeUtils.AUTO).replace("_", " ")};

    public Theme theme;
    Context context;
    private PrefUtils prefUtils;

    public static ThemeUtils with(PrefUtils prefUtils) {
        ThemeUtils themeUtils = new ThemeUtils();
        themeUtils.prefUtils = prefUtils;
        return themeUtils;
    }

    public static ThemeUtils with(Context _context) {
        ThemeUtils themeUtils = new ThemeUtils();
        themeUtils.context = _context;
        if (themeUtils.prefUtils == null)
            themeUtils.prefUtils = PrefUtils.with(_context);
        return themeUtils;
    }

    public static ThemeUtils with(Context _context, String prefName) {
        ThemeUtils themeUtils = new ThemeUtils();
        themeUtils.context = _context;
        if (themeUtils.prefUtils == null)
            themeUtils.prefUtils = PrefUtils.with(_context);
        if (StringUtils.isEmpty(PrefUtils.PREF))
            themeUtils.prefUtils.setSharedPref(prefName);
        return themeUtils;
    }

    public static ThemeUtils with(PrefUtils _prefUtils, Context _context) {
        ThemeUtils themeUtils = new ThemeUtils();
        themeUtils.prefUtils = _prefUtils;
        themeUtils.context = _context;
        return themeUtils;
    }

    public Theme getTheme() {
        String theme = prefUtils.getString(THEME, AUTO);
        switch (theme) {
            case DARK:
                return Theme.DARK;
            case LIGHT:
                return Theme.LIGHT;
            case AUTO:
                return Theme.AUTO;
        }
        return Theme.LIGHT;
    }

    public void applyTheme() {
        if (getTheme() == Theme.LIGHT && AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if (getTheme() == Theme.DARK && AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else if (getTheme() == Theme.AUTO)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
