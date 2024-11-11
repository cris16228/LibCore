package com.github.cris16228.libcore.deviceutils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.github.cris16228.libcore.FloatUtils;

import java.math.RoundingMode;

public class DeviceUtils {

    Context context;
    DisplayMetrics dm;

    public static DeviceUtils with(Context _context) {
        DeviceUtils deviceUtils = new DeviceUtils();
        deviceUtils.context = _context;
        deviceUtils.dm = new DisplayMetrics();
        return deviceUtils;
    }

    public static boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("sdk_gphone64_arm64")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }

    public void clearFocus(Activity activity) {
        View view = activity.getCurrentFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    public void getFocus(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public void clearFocus(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    public String getDisplaySize(int limit) {
        double x = 0, y = 0;
        int mWidthPixels, mHeightPixels;
        try {
            WindowManager windowManager = ((Activity) context).getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
            Point realSize = new Point();
            Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
            mWidthPixels = realSize.x;
            mHeightPixels = realSize.y;
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            x = Math.pow(mWidthPixels / dm.xdpi, 2);
            y = Math.pow(mHeightPixels / dm.ydpi, 2);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return FloatUtils.getNumberFormat(Math.sqrt(x + y), limit, true, RoundingMode.HALF_UP, context) + "\"";
    }

    public int getWidth() {
        dm = context.getResources().getDisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public int getHeight() {
        dm = context.getResources().getDisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public boolean isLeftSideTouchInRange(Activity activity, int percent, float xValue) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int calculatedPercentage = (width) * percent / 100;
        return (xValue <= calculatedPercentage);
    }

    public boolean isRightSideTouchInRange(Activity activity, int percent, float xValue, float minXRange) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int calculatedPercentage = (width) * percent / 100;
        float calculatedPercentageRange = (width) * minXRange / 100;
        return (xValue >= calculatedPercentageRange && xValue <= calculatedPercentage);
    }

    public boolean isMiddleSideTouchInRange(Activity activity, int percent, float xValue, float minXRange, float maxXRange) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        float minCalculatedPercentage = (width) * minXRange / 100;
        float maxCalculatedPercentage = (width) * maxXRange / 100;
        return (xValue >= minCalculatedPercentage && xValue <= maxCalculatedPercentage);
    }

    public String getResolution() {
        return getWidth() + "x" + getHeight();
    }

    public String getOrientation(int orientation) {
        switch (orientation) {
            case 0:
                return "Undefined";
            case 1:
                return "Portrait";
            case 2:
                return "Landscape";
        }
        return "Undefined";
    }
}
