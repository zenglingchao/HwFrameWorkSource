package com.huawei.hsm.permission;

import android.os.Binder;
import android.util.Slog;

public class BackgroundPermManager {
    private static final String TAG = "BackgroundPermManager";
    private static BackgroundPermManager mInstance;
    private static final Object mLock = new Object();

    public static BackgroundPermManager getInstance() {
        BackgroundPermManager backgroundPermManager;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new BackgroundPermManager();
            }
            backgroundPermManager = mInstance;
        }
        return backgroundPermManager;
    }

    public void notifyBackgroundMgr(String pkgName, int pid, int uidOf3RdApk, int permType, int permCfg) {
        long identify = Binder.clearCallingIdentity();
        try {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("pkgName: ");
            stringBuilder.append(pkgName);
            stringBuilder.append(",pid: ");
            stringBuilder.append(pid);
            stringBuilder.append(" ,uidOf3RdApk: ");
            stringBuilder.append(uidOf3RdApk);
            stringBuilder.append(" ,permType: ");
            stringBuilder.append(permType);
            stringBuilder.append(" ,permCfg: ");
            stringBuilder.append(permCfg);
            Slog.i(str, stringBuilder.toString());
            StubController.notifyBackgroundMgr(pkgName, pid, uidOf3RdApk, permType, permCfg);
        } finally {
            Binder.restoreCallingIdentity(identify);
        }
    }

    private BackgroundPermManager() {
    }
}
