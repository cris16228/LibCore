package com.github.cris16228.libcore;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionsUtils {

    private final Context context;
    private Activity activity;

    public PermissionsUtils(Context context) {
        this.context = context;
    }

    public static PermissionsUtils get(Context context, Activity activity) {
        PermissionsUtils permissionsUtils = new PermissionsUtils(context);

        permissionsUtils.activity = activity;
        return permissionsUtils;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public boolean checkNotificationsPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public boolean checkSetExactAlarmPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED;
    }

    public void checkPermission(String permission, int requestCode, permissionResult permissionResult) {
        if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission) || Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
            List<String> permissions = new ArrayList<>();
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            checkPermissions(permissions, new ArrayList<>(), permissionResult);
        }
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        } else {
            permissionResult.OnSuccess();
        }
    }

    public void checkPermission(List<String> permissions, int requestCode) {
        if (!permissions.isEmpty()) {
            String permission = permissions.get(0);
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                checkPermission(permissions, requestCode);
            } else {
                permissions.remove(0);
                checkPermission(permissions, requestCode);
            }
        }
    }

    public void checkPermissions(List<String> permissions, List<Integer> requestCodes, permissionResult permissionResult) {
        if (permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                if (requestCodes.size() < permissions.size() || requestCodes.size() > permissions.size())
                    return;
                for (int i = 0; i < permissions.size(); i++) {
                    if (!Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions.get(i)) && ContextCompat.checkSelfPermission(context,
                            permissions.get(i)) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, new String[]{permissions.get(i)}, requestCodes.get(i));
                    } else {
                        permissionResult.OnSuccess();
                    }
                }
            } else {
                permissionResult.OnSuccess();
            }
        }
    }

    public interface permissionResult {
        void OnSuccess();
    }

    private static class Permission {

        public String ID;
        public String name;
        public String description;

        public Permission(String ID, String name, String description) {
            this.ID = ID;
            this.name = name;
            this.description = description;
        }

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
