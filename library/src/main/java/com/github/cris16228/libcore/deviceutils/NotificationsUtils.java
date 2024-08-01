package com.github.cris16228.libcore.deviceutils;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.text.TextUtils;

import com.github.cris16228.libcore.ActivityUtils;
import com.github.cris16228.libcore.FileUtils;
import com.github.cris16228.libcore.StringUtils;

public class NotificationsUtils {
    Context context;
    Notification notification;

    public static NotificationsUtils with(Context _context) {
        NotificationsUtils notificationsUtils = new NotificationsUtils();
        notificationsUtils.context = _context;
        return notificationsUtils;
    }

    public static NotificationsUtils with(Context _context, Notification notification) {
        NotificationsUtils notificationsUtils = new NotificationsUtils();
        notificationsUtils.context = _context;
        notificationsUtils.notification = notification;
        return notificationsUtils;
    }

    public boolean isNotificationServiceActive(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (!StringUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getTitle() {
        return notification.extras.getString(Notification.EXTRA_TITLE);
    }

    public String getBigTitle() {
        return notification.extras.getString(Notification.EXTRA_TITLE_BIG);
    }

    public String getContent() {
        return notification.extras.getString(Notification.EXTRA_TEXT);
    }

    public String getBigContent() {
        return notification.extras.getString(Notification.EXTRA_BIG_TEXT);
    }

    public Bitmap getIcon() {
        Icon largeIcon = notification.getLargeIcon();
        if (largeIcon != null) {
            largeIcon.loadDrawable(context);
            return BitmapFactory.decodeResource(context.getResources(), largeIcon.getResId());
        }
        return null;
    }

    public String getImageConverted(Bitmap bitmap) {
        return FileUtils.with(context).saveBitmap(bitmap);
    }

    public void openNotificationServices() {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        ActivityUtils.with(context).startIntent(context, intent);
    }
}
