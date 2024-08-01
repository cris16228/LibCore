package com.github.cris16228.libcore.utils;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.cris16228.libcore.AsyncUtils;
import com.github.cris16228.libcore.FileUtils;
import com.github.cris16228.libcore.deviceutils.PackageUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler defaultUEH;
    private final Activity app;
    private final String bearer;
    private final boolean wifiAccess;

    public UncaughtExceptionHandler(Activity app, boolean wifiAccess) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.app = app;
        this.bearer = "";
        this.wifiAccess = wifiAccess;
    }

    public UncaughtExceptionHandler(Activity app, String bearer, boolean wifiAccess) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.app = app;
        this.bearer = bearer;
        this.wifiAccess = wifiAccess;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        CountDownLatch latch = new CountDownLatch(1);
        AsyncUtils uploadCrash = AsyncUtils.get();
        uploadCrash.onExecuteListener(new AsyncUtils.onExecuteListener() {
            @Override
            public void preExecute() {
                StackTraceElement[] arr = e.getStackTrace();
                StringBuilder report = new StringBuilder();
                report.append("\n");
                report.append("App: ").append(PackageUtils.with(app).getAppName(app.getPackageName())).append("\n");
                report.append("Version: ").append(PackageUtils.with(app).appFromPackage(app.getPackageName()).getLongVersionCode()).append("\n");
                report.append("Package: ").append(app.getPackageName()).append("\n");
                report.append("VersionCode: ").append(PackageUtils.with(app).appFromPackage(app.getPackageName()).versionName).append("\n");
                report.append("Error: ").append(e).append("\n").append("\n");
                report.append("-------------------------------- Stack trace --------------------------------").append("\n");
                for (StackTraceElement stackTraceElement : arr) {
                    report.append(stackTraceElement.toString()).append("\n");
                }
                report.append("-----------------------------------------------------------------------------").append("\n").append("\n");

                Throwable cause = e.getCause();
                if (cause != null) {
                    report.append("----------------------------------- Cause -----------------------------------").append("\n");
                    report.append(cause).append("\n");
                    arr = cause.getStackTrace();
                    for (StackTraceElement stackTraceElement : arr) {
                        report.append(stackTraceElement.toString()).append("\n");
                    }
                    report.append("-----------------------------------------------------------------------------").append("\n").append("\n");
                }
                String dateTime = new SimpleDateFormat("dd-MM-yyyy_hh.mm.ss", Locale.getDefault()).format(new Date());
                String fileName = "/crash-reports/crash_" + dateTime + ".log";
                FileUtils.with(app).debugLog(report.toString(), fileName);
            }

            @Override
            public void doInBackground() {
            }

            @Override
            public void postDelayed() {
            }
        });
        uploadCrash.execute();
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                Log.e("UncaughtException", "Timeout waiting for AsyncTask to complete");
            }
        } catch (InterruptedException ex) {
            Log.e("UncaughtException", "InterruptedException while waiting for AsyncTask", ex);
        }
        defaultUEH.uncaughtException(t, e);
    }
}
