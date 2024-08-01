package com.github.cris16228.libcore.deviceutils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;

public class PackageUtils {
    Context context;

    public static PackageUtils with(Context _context) {
        PackageUtils packageUtils = new PackageUtils();
        packageUtils.context = _context;
        return packageUtils;
    }

    public PackageInfo appFromPackage(String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PackageInfo getPermissions(PackageInfo app) {
        try {
            return context.getPackageManager().getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PackageInfo getActivities(PackageInfo app) {
        try {
            return context.getPackageManager().getPackageInfo(app.packageName, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PackageInfo getReceivers(PackageInfo app) {
        try {
            return context.getPackageManager().getPackageInfo(app.packageName, PackageManager.GET_RECEIVERS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PackageInfo getProviders(PackageInfo app) {
        try {
            return context.getPackageManager().getPackageInfo(app.packageName, PackageManager.GET_PROVIDERS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PackageInfo getServices(PackageInfo app) {
        try {
            return context.getPackageManager().getPackageInfo(app.packageName, PackageManager.GET_SERVICES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isSystemApp(PackageInfo p) {
        return ((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM);
    }

    public boolean isAppOld(int versionCode, int onlineVersionCode) {
        int value = Integer.compare(versionCode, onlineVersionCode);
        /*Returns: the value 0 if x == y; a value less than 0 if x < y; and a value greater than 0 if x > y*/
        if (value == 0)
            return false;
        return value < 0;
    }

    public void openApp(PackageInfo app) {
        Intent open_app_intent = context.getPackageManager().getLaunchIntentForPackage(app.packageName);
        if (open_app_intent != null)
            context.startActivity(open_app_intent);
    }

    public void uninstallApp(PackageInfo app) {
        Intent delete_intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        Uri uri = Uri.fromParts("package", app.packageName, null);
        delete_intent.setData(uri);
        context.startActivity(delete_intent);
    }

    public void infoApp(PackageInfo app) {
        Intent open_info_intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", app.packageName, null);
        open_info_intent.setData(uri);
        context.startActivity(open_info_intent);
    }

    public Bitmap getAppIcon(String pkg) {
        try {
            Drawable icon = context.getPackageManager().getApplicationIcon(pkg);
            return ((BitmapDrawable) icon).getBitmap();
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAppName(String pkg) {
        return appFromPackage(pkg).applicationInfo.loadLabel(context.getPackageManager()).toString();
    }
}
