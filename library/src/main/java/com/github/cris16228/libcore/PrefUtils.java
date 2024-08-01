package com.github.cris16228.libcore;

import android.content.Context;

public class PrefUtils {

    public static String PREF = "";
    Context context;

    public static PrefUtils with(Context _context) {
        PrefUtils prefUtils = new PrefUtils();
        prefUtils.context = _context;
        return prefUtils;
    }

    public static PrefUtils with(Context _context, String pref) {
        PrefUtils prefUtils = new PrefUtils();
        prefUtils.context = _context;
        PREF = pref;
        return prefUtils;
    }

    public String getSharedPref() {
        return PREF;
    }

    public void setSharedPref(String pref) {
        PREF = pref;
    }

    public boolean isKeyPresent(String key) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).contains(key);
    }

    public String getString(String key, String value) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(key, value);
    }

    public Integer getInt(String key, Integer value) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(key, value);
    }

    public Boolean getBoolean(String key, boolean value) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean(key, value);
    }

    public Float getFloat(String key, float value) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getFloat(key, value);
    }

    public Long getLong(String key, long value) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getLong(key, value);
    }

    public PrefUtils setString(String key, String value) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(key, value).apply();
        return this;
    }

    public PrefUtils setInt(String key, int value) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putInt(key, value).apply();
        return this;
    }

    public PrefUtils setBoolean(String key, boolean value) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply();
        return this;
    }

    public PrefUtils setFloat(String key, float value) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putFloat(key, value).apply();
        return this;
    }

    public PrefUtils setLong(String key, long value) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putLong(key, value).apply();
        return this;
    }

    public void deleteKey(String key) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().remove(key).apply();
    }
}
