package com.github.cris16228.libcore;

import android.content.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class FloatUtils {

    Context context;

    public static FloatUtils with(Context _context) {
        FloatUtils floatUtils = new FloatUtils();
        floatUtils.context = _context;
        return floatUtils;
    }

    public static String getNumberFormat(Object value, int limit) {
        if (limit < 0) {
            return "NaN";
        }
        float tempFloat;
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(limit);
        format.setMaximumFractionDigits(limit);
        if (value instanceof Float || value instanceof Double)
            return format.format(value);
        if (value instanceof String)
            try {
                tempFloat = Float.parseFloat((String) value);
                return format.format(tempFloat);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        else {
            return "NaN";
        }
        return (String) value;
    }

    public static String getNumberFormatSmart(Object value, int limit) {
        if (limit < 0) {
            return "NaN";
        }
        float tempFloat;
        DecimalFormat format = new DecimalFormat("#.##");
        format.setMinimumFractionDigits(limit);
        format.setMaximumFractionDigits(limit);
        if (value instanceof Float || value instanceof Double) {
            if ((double) value == Math.round((double) value)) {
                format.setMaximumFractionDigits(0);
                format.setMinimumFractionDigits(0);
            }
            return format.format(value);
        }
        if (value instanceof String)
            try {
                tempFloat = Float.parseFloat((String) value);
                return format.format(tempFloat);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        else {
            return "NaN";
        }
        return (String) value;
    }

    public static String getNumberFormat(Object value, int limit, Context context) {
        FileUtils fileUtils = FileUtils.with(context);
        if (limit < 0) {
            fileUtils.debugLog(limit + " is not valid! It must be higher than 0");
            return "NaN";
        }
        float tempFloat;
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(limit);
        format.setMaximumFractionDigits(limit);
        if (value instanceof Float || value instanceof Double)
            return format.format(value);
        if (value instanceof String)
            try {
                tempFloat = Float.parseFloat((String) value);
                return format.format(tempFloat);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        else {
            fileUtils.debugLog(value + "is not valid! It must be a \"float\" or a \"double\"");
            return "NaN";
        }
        return (String) value;
    }

    public static String getNumberFormat(Object value, int limit, boolean round, RoundingMode roundingMode, Context context) {
        FileUtils fileUtils = FileUtils.with(context);
        if (limit <= 0) {
            fileUtils.debugLog(limit + "is not valid! It must be higher than 0");
            return "NaN";
        }
        double tempDouble;
        NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
        format.setMinimumFractionDigits(limit);
        format.setMaximumFractionDigits(limit);
        if (round)
            format.setRoundingMode(roundingMode);
        if (value instanceof Float || value instanceof Double) {
            BigDecimal bd = BigDecimal.valueOf((double) value);
            return format.format(bd);
        }
        if (value instanceof String)
            try {
                tempDouble = Double.parseDouble((String) value);
                BigDecimal bd = BigDecimal.valueOf((double) value);
                return format.format(bd);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        else {
            fileUtils.debugLog(value + "is not valid! It must be a \"float\" or a \"double\"");
            return "NaN";
        }
        return (String) value;
    }
}
