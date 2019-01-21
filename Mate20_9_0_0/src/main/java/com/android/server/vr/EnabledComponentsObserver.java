package com.android.server.vr;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.content.PackageMonitor;
import com.android.server.slice.SliceClientPermissions.SliceAuthority;
import com.android.server.vr.SettingsObserver.SettingChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EnabledComponentsObserver implements SettingChangeListener {
    public static final int DISABLED = -1;
    private static final String ENABLED_SERVICES_SEPARATOR = ":";
    public static final int NOT_INSTALLED = -2;
    public static final int NO_ERROR = 0;
    private static final String TAG = EnabledComponentsObserver.class.getSimpleName();
    private final Context mContext;
    private final Set<EnabledComponentChangeListener> mEnabledComponentListeners = new ArraySet();
    private final SparseArray<ArraySet<ComponentName>> mEnabledSet = new SparseArray();
    private final SparseArray<ArraySet<ComponentName>> mInstalledSet = new SparseArray();
    private final Object mLock;
    private final String mServiceName;
    private final String mServicePermission;
    private final String mSettingName;

    public interface EnabledComponentChangeListener {
        void onEnabledComponentChanged();
    }

    private EnabledComponentsObserver(Context context, String settingName, String servicePermission, String serviceName, Object lock, Collection<EnabledComponentChangeListener> listeners) {
        this.mLock = lock;
        this.mContext = context;
        this.mSettingName = settingName;
        this.mServiceName = serviceName;
        this.mServicePermission = servicePermission;
        this.mEnabledComponentListeners.addAll(listeners);
    }

    public static EnabledComponentsObserver build(Context context, Handler handler, String settingName, Looper looper, String servicePermission, String serviceName, Object lock, Collection<EnabledComponentChangeListener> listeners) {
        SettingsObserver s = SettingsObserver.build(context, handler, settingName);
        EnabledComponentsObserver o = new EnabledComponentsObserver(context, settingName, servicePermission, serviceName, lock, listeners);
        new PackageMonitor() {
            public void onSomePackagesChanged() {
                EnabledComponentsObserver.this.onPackagesChanged();
            }

            public void onPackageDisappeared(String packageName, int reason) {
                EnabledComponentsObserver.this.onPackagesChanged();
            }

            public void onPackageModified(String packageName) {
                EnabledComponentsObserver.this.onPackagesChanged();
            }

            public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
                EnabledComponentsObserver.this.onPackagesChanged();
                return super.onHandleForceStop(intent, packages, uid, doit);
            }
        }.register(context, looper, UserHandle.ALL, true);
        s.addListener(o);
        return o;
    }

    public void onPackagesChanged() {
        rebuildAll();
    }

    public void onSettingChanged() {
        rebuildAll();
    }

    public void onSettingRestored(String prevValue, String newValue, int userId) {
        rebuildAll();
    }

    public void onUsersChanged() {
        rebuildAll();
    }

    public void rebuildAll() {
        synchronized (this.mLock) {
            this.mInstalledSet.clear();
            this.mEnabledSet.clear();
            for (int i : getCurrentProfileIds()) {
                ArraySet<ComponentName> implementingPackages = loadComponentNamesForUser(i);
                ArraySet<ComponentName> packagesFromSettings = loadComponentNamesFromSetting(this.mSettingName, i);
                packagesFromSettings.retainAll(implementingPackages);
                this.mInstalledSet.put(i, implementingPackages);
                this.mEnabledSet.put(i, packagesFromSettings);
            }
        }
        sendSettingChanged();
    }

    public int isValid(ComponentName component, int userId) {
        synchronized (this.mLock) {
            ArraySet<ComponentName> installedComponents = (ArraySet) this.mInstalledSet.get(userId);
            if (installedComponents != null) {
                if (installedComponents.contains(component)) {
                    ArraySet<ComponentName> validComponents = (ArraySet) this.mEnabledSet.get(userId);
                    if (validComponents != null) {
                        if (validComponents.contains(component)) {
                            return 0;
                        }
                    }
                    return -1;
                }
            }
            return -2;
        }
    }

    public ArraySet<ComponentName> getInstalled(int userId) {
        synchronized (this.mLock) {
            ArraySet<ComponentName> ret = (ArraySet) this.mInstalledSet.get(userId);
            if (ret == null) {
                ArraySet arraySet = new ArraySet();
                return arraySet;
            }
            return ret;
        }
    }

    public ArraySet<ComponentName> getEnabled(int userId) {
        synchronized (this.mLock) {
            ArraySet<ComponentName> ret = (ArraySet) this.mEnabledSet.get(userId);
            if (ret == null) {
                ArraySet arraySet = new ArraySet();
                return arraySet;
            }
            return ret;
        }
    }

    private int[] getCurrentProfileIds() {
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (userManager == null) {
            return null;
        }
        return userManager.getEnabledProfileIds(ActivityManager.getCurrentUser());
    }

    public static ArraySet<ComponentName> loadComponentNames(PackageManager pm, int userId, String serviceName, String permissionName) {
        ArraySet<ComponentName> installed = new ArraySet();
        List<ResolveInfo> installedServices = pm.queryIntentServicesAsUser(new Intent(serviceName), 786564, userId);
        if (installedServices != null) {
            int count = installedServices.size();
            for (int i = 0; i < count; i++) {
                ServiceInfo info = ((ResolveInfo) installedServices.get(i)).serviceInfo;
                ComponentName component = new ComponentName(info.packageName, info.name);
                if (permissionName.equals(info.permission)) {
                    installed.add(component);
                } else {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Skipping service ");
                    stringBuilder.append(info.packageName);
                    stringBuilder.append(SliceAuthority.DELIMITER);
                    stringBuilder.append(info.name);
                    stringBuilder.append(": it does not require the permission ");
                    stringBuilder.append(permissionName);
                    Slog.w(str, stringBuilder.toString());
                }
            }
        }
        return installed;
    }

    private ArraySet<ComponentName> loadComponentNamesForUser(int userId) {
        return loadComponentNames(this.mContext.getPackageManager(), userId, this.mServiceName, this.mServicePermission);
    }

    private ArraySet<ComponentName> loadComponentNamesFromSetting(String settingName, int userId) {
        String settingValue = Secure.getStringForUser(this.mContext.getContentResolver(), settingName, userId);
        if (TextUtils.isEmpty(settingValue)) {
            return new ArraySet();
        }
        String[] restored = settingValue.split(ENABLED_SERVICES_SEPARATOR);
        ArraySet<ComponentName> result = new ArraySet(restored.length);
        for (ComponentName value : restored) {
            ComponentName value2 = ComponentName.unflattenFromString(value2);
            if (value2 != null) {
                result.add(value2);
            }
        }
        return result;
    }

    private void sendSettingChanged() {
        for (EnabledComponentChangeListener l : this.mEnabledComponentListeners) {
            l.onEnabledComponentChanged();
        }
    }
}
