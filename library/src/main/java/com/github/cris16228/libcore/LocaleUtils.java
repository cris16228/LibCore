package com.github.cris16228.libcore;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleUtils {

    //Locale
    public static final String LOCALE = "locale";
    public static final String EN = "en";
    public static final String IT = "it";
    PrefUtils prefUtils;
    Context context;

    public static LocaleUtils with(Context _context) {
        LocaleUtils localeUtils = new LocaleUtils();
        localeUtils.context = _context;
        if (localeUtils.prefUtils == null)
            localeUtils.prefUtils = new PrefUtils();
        return localeUtils;
    }

    public static LocaleUtils with(PrefUtils _prefUtils, Context _context) {
        LocaleUtils localeUtils = new LocaleUtils();
        localeUtils.prefUtils = _prefUtils;
        localeUtils.context = _context;
        return localeUtils;
    }

    public void onStartSetLocale() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PrefUtils.PREF, MODE_PRIVATE);
        String language = sharedPreferences.getString(LOCALE, EN);
        setLocale(language);
    }

    public void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
