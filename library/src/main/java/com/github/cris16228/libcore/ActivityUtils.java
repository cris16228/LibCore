package com.github.cris16228.libcore;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.view.ContentInfoCompat;
import androidx.fragment.app.FragmentActivity;

import com.github.cris16228.libcore.deviceutils.DeviceUtils;

public class ActivityUtils {

    Context context;

    public static ActivityUtils with(Context _context) {
        ActivityUtils activityUtils = new ActivityUtils();
        activityUtils.context = _context;
        return activityUtils;
    }

    public static boolean isAppBackground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

    public void restartApp(Context currentActivity, Class<?> destinationActivity) {
        if (currentActivity instanceof FragmentActivity)
            ((FragmentActivity) currentActivity).finish();
        else
            ((Activity) currentActivity).finish();
        if (DeviceUtils.isEmulator())
            System.out.println("Finishing " + currentActivity.getClass().getSimpleName());
        restartActivity(currentActivity, destinationActivity);
    }

    public void delayActivity(Class<?> destinationActivity, long delay, boolean finish) {
        if (delay <= 0) {
            startActivity(context, destinationActivity);
            if (finish)
                ((Activity) context).finish();
        } else {
            new Handler().postDelayed(() -> {
                startActivity(context, destinationActivity);
                if (finish)
                    ((Activity) context).finish();
            }, delay);
        }
    }

    public long delayedCloseApp(long mBackPressed) {
        return delayedCloseApp(mBackPressed, 2000);
    }

    public long delayedCloseApp(long mBackPressed, int timeInterval) {
        if (mBackPressed + timeInterval > System.currentTimeMillis()) {
            ((Activity) context).finish();
            return mBackPressed;
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.press_back_twice), Toast.LENGTH_SHORT).show();
        }
        return mBackPressed = System.currentTimeMillis();
    }

    public void delayActivity(Class<?> destinationActivity, long delay, boolean finish, Bundle bundles) {
        if (delay <= 0) {
            startActivity(context, destinationActivity, bundles);
            if (finish)
                ((Activity) context).finish();
        } else {
            new Handler().postDelayed(() -> {
                startActivity(context, destinationActivity, bundles);
                if (finish)
                    ((Activity) context).finish();
            }, delay);
        }
    }

    public void enableFullscreen(Window window) {
        if (window == null) {
            return;
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final View decorView = window.getDecorView();
        if (decorView != null) {
            int uiOption = decorView.getSystemUiVisibility();
            uiOption |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
            uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOption);
        }
    }

    public void disableFullscreen(Window window) {
        if (window == null) {
            return;
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        final View decorView = window.getDecorView();
        int uiOption = decorView.getSystemUiVisibility();
        uiOption &= ~View.SYSTEM_UI_FLAG_LOW_PROFILE;
        uiOption &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOption &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOption);
    }

    public void startActivity(Context currentActivity, Class<?> destinationActivity) {
        Intent activity = new Intent(currentActivity, destinationActivity);
        activity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        currentActivity.startActivity(activity);
    }

    public void startIntent(Context currentActivity, Intent intent) {
        currentActivity.startActivity(intent);
    }

    public void restartActivity(Context currentActivity, Class<?> destinationActivity) {
        Intent activity = new Intent(currentActivity, destinationActivity);
        activity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        currentActivity.startActivity(activity);
    }

    public void startActivity(Context currentActivity, Class<?> destinationActivity, Bundle bundles) {
        Intent activity = new Intent(currentActivity, destinationActivity);
        activity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.putExtras(bundles);
        currentActivity.startActivity(activity);
    }

    public void startActivity(Context currentActivity, Class<?> destinationActivity, @ContentInfoCompat.Flags int[] flags) {
        Intent activity = new Intent(currentActivity, destinationActivity);
        for (int flag : flags) {
            activity.addFlags(flag);
        }
        currentActivity.startActivity(activity);
    }

    public void startActivity(Context currentActivity, Class<?> destinationActivity, @ContentInfoCompat.Flags int[] flags, Bundle bundle) {
        Intent activity = new Intent(currentActivity, destinationActivity);
        for (int flag : flags) {
            activity.addFlags(flag);
        }
        activity.putExtras(bundle);
        currentActivity.startActivity(activity);
    }

    public Intent activityIntent(Context currentActivity, Class<?> destinationActivity) {
        Intent activity = new Intent(currentActivity, destinationActivity);
        activity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return activity;
    }

    public void startActivity(Intent activity, boolean finish, String[] names, Bundle[] values) {

        if (values.length == names.length) {
            for (int i = 0; i < names.length; i++) {
                activity.putExtra(names[i], values[i]);
            }
        }
        context.startActivity(activity);
        if (finish)
            try {
                ((Activity) context).finish();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.toString());
            }
    }

    public void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null && view != null)
            manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void startActivity(Context currentActivity, Class<?> destinationActivity, boolean finish) {
        Intent activity = new Intent(currentActivity, destinationActivity);
        activity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        currentActivity.startActivity(activity);
        if (finish)
            try {
                ((Activity) currentActivity).finish();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.toString());
            }
    }
}
