package com.android.internal.telephony.uicc;

import android.app.AlarmManager;
import android.app.Notification.Action;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.util.NotificationChannelController;
import java.util.Arrays;
import java.util.List;

@VisibleForTesting
public class InstallCarrierAppUtils {
    private static final int ACTIVATE_CELL_SERVICE_NOTIFICATION_ID = 12;
    private static CarrierAppInstallReceiver sCarrierAppInstallReceiver = null;

    static void showNotification(Context context, String pkgName) {
        String message;
        Resources res = Resources.getSystem();
        String title = res.getString(17040226);
        boolean persistent = true;
        if (TextUtils.isEmpty(getAppNameFromPackageName(context, pkgName))) {
            message = res.getString(17040224);
        } else {
            message = res.getString(17040225, new Object[]{getAppNameFromPackageName(context, pkgName)});
        }
        String downloadButtonText = res.getString(17040223);
        if (Global.getInt(context.getContentResolver(), "install_carrier_app_notification_persistent", 1) != 1) {
            persistent = false;
        }
        getNotificationManager(context).notify(pkgName, 12, new Builder(context, NotificationChannelController.CHANNEL_ID_SIM).setContentTitle(title).setContentText(message).setSmallIcon(17302752).addAction(new Action.Builder(null, downloadButtonText, PendingIntent.getActivity(context, 0, getPlayStoreIntent(pkgName), 134217728)).build()).setOngoing(persistent).setVisibility(-1).build());
    }

    static void hideAllNotifications(Context context) {
        NotificationManager notificationManager = getNotificationManager(context);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        if (activeNotifications != null) {
            for (StatusBarNotification notification : activeNotifications) {
                if (notification.getId() == 12) {
                    notificationManager.cancel(notification.getTag(), notification.getId());
                }
            }
        }
    }

    static void hideNotification(Context context, String pkgName) {
        getNotificationManager(context).cancel(pkgName, 12);
    }

    static Intent getPlayStoreIntent(String pkgName) {
        Intent storeIntent = new Intent("android.intent.action.VIEW");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("market://details?id=");
        stringBuilder.append(pkgName);
        storeIntent.setData(Uri.parse(stringBuilder.toString()));
        storeIntent.addFlags(268435456);
        return storeIntent;
    }

    static void showNotificationIfNotInstalledDelayed(Context context, String pkgName, long delayMillis) {
        ((AlarmManager) context.getSystemService("alarm")).set(3, SystemClock.elapsedRealtime() + delayMillis, PendingIntent.getBroadcast(context, 0, ShowInstallAppNotificationReceiver.get(context, pkgName), 0));
    }

    static void registerPackageInstallReceiver(Context context) {
        if (sCarrierAppInstallReceiver == null) {
            sCarrierAppInstallReceiver = new CarrierAppInstallReceiver();
            context = context.getApplicationContext();
            IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            intentFilter.addDataScheme("package");
            context.registerReceiver(sCarrierAppInstallReceiver, intentFilter);
        }
    }

    static void unregisterPackageInstallReceiver(Context context) {
        if (sCarrierAppInstallReceiver != null) {
            context.getApplicationContext().unregisterReceiver(sCarrierAppInstallReceiver);
            sCarrierAppInstallReceiver = null;
        }
    }

    static boolean isPackageInstallNotificationActive(Context context) {
        for (StatusBarNotification notification : getNotificationManager(context).getActiveNotifications()) {
            if (notification.getId() == 12) {
                return true;
            }
        }
        return false;
    }

    static String getAppNameFromPackageName(Context context, String packageName) {
        return getAppNameFromPackageName(packageName, Global.getString(context.getContentResolver(), "carrier_app_names"));
    }

    @VisibleForTesting
    public static String getAppNameFromPackageName(String packageName, String mapString) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        packageName = packageName.toLowerCase();
        String pairDelim = "\\s*;\\s*";
        String keyValueDelim = "\\s*:\\s*";
        if (TextUtils.isEmpty(mapString)) {
            return null;
        }
        List<String> keyValuePairList = Arrays.asList(mapString.split("\\s*;\\s*"));
        if (keyValuePairList.isEmpty()) {
            return null;
        }
        for (String keyValueString : keyValuePairList) {
            String[] keyValue = keyValueString.split("\\s*:\\s*");
            if (keyValue.length == 2 && keyValue[0].equals(packageName)) {
                return keyValue[1];
            }
        }
        return null;
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService("notification");
    }
}
