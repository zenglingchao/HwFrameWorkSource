package com.huawei.fileprotect;

import android.content.Context;
import android.os.SystemProperties;
import com.huawei.android.os.UserHandleEx;
import huawei.android.fileprotect.HwSfpIudfXattr;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class HwSfpPolicyManager {
    private static final String DB_PATH = "/database/";
    private static final boolean DEFAULT_VALUE = false;
    private static final String DOT = ".";
    private static final int ERROR_TYPE = -1;
    private static final String FILES_PATH = "/files/";
    private static final String LOG_TAG = "HwSfpPolicyManager";
    private static final int NEITHER_ECE_NOR_SECE = 1;
    private static final int RESULT_SUCESS = 0;
    private static final String SEPARATOR_DOT_AND_SLASH = "../";
    private static final String SHARED_PREFS_PATH = "/shared_prefs/";
    private static final String SHARED_PREFS_SUFFIX = ".xml";
    private static final String SLASH = "/";
    private static final int STORAGE_TYPE_ECE = 2;
    private static final int STORAGE_TYPE_SECE = 3;
    private static final String TYPE_DB_TAG = ".db";
    private static final int TYPE_FILE = 1;
    private static HwSfpPolicyManager sInstance = null;
    private static final boolean sIsSupportIudf = SystemProperties.getBoolean("ro.config.support_iudf", false);
    private String eceKeyDesc;
    private HashMap<Integer, String> mEceKeyDescMap = new HashMap();
    HwSfpManager mHwSfpManager = HwSfpManager.getDefault();
    private HashMap<Integer, String> mSeceKeyDescMap = new HashMap();
    private String seceKeyDesc;

    private HwSfpPolicyManager() {
    }

    public static HwSfpPolicyManager getDefault() {
        HwSfpPolicyManager hwSfpPolicyManager;
        synchronized (HwSfpPolicyManager.class) {
            if (sInstance == null) {
                sInstance = new HwSfpPolicyManager();
            }
            hwSfpPolicyManager = sInstance;
        }
        return hwSfpPolicyManager;
    }

    public void setEcePolicy(Context context, String name) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException, IOException {
        preCheck(context, name);
        int userId = UserHandleEx.myUserId();
        if (this.mEceKeyDescMap.containsKey(Integer.valueOf(userId))) {
            this.eceKeyDesc = (String) this.mEceKeyDescMap.get(Integer.valueOf(userId));
        } else {
            this.eceKeyDesc = this.mHwSfpManager.getKeyDesc(userId, 2);
            if (this.eceKeyDesc != null) {
                this.mEceKeyDescMap.put(Integer.valueOf(userId), this.eceKeyDesc);
            } else {
                throw new IllegalStateException("Get ece key desc failed! Something wrong with the interface.");
            }
        }
        if (HwSfpIudfXattr.setFileXattrEx(getExactPath(context, name), this.eceKeyDesc, 2, 1) != 0) {
            throw new IOException("Set ECE policy failed!");
        }
    }

    public void setSecePolicy(Context context, String name) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException, IOException {
        preCheck(context, name);
        int userId = UserHandleEx.myUserId();
        if (this.mSeceKeyDescMap.containsKey(Integer.valueOf(userId))) {
            this.seceKeyDesc = (String) this.mSeceKeyDescMap.get(Integer.valueOf(userId));
        } else {
            this.seceKeyDesc = this.mHwSfpManager.getKeyDesc(userId, 3);
            if (this.seceKeyDesc != null) {
                this.mSeceKeyDescMap.put(Integer.valueOf(userId), this.seceKeyDesc);
            } else {
                throw new IllegalStateException("Get sece key desc failed! Something wrong with the interface.");
            }
        }
        if (HwSfpIudfXattr.setFileXattrEx(getExactPath(context, name), this.seceKeyDesc, 3, 1) != 0) {
            throw new IOException("Set SECE policy failed!");
        }
    }

    public static final int getPolicyProtectType(Context context, String name) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException {
        preCheck(context, name);
        int type = HwSfpIudfXattr.getFileXattrEx(getExactPath(context, name), 1);
        if (type < 0) {
            return -1;
        }
        return type;
    }

    private static void preCheck(Context context, String name) throws IllegalArgumentException, IllegalStateException, IllegalAccessException {
        if (context == null || name == null) {
            throw new IllegalArgumentException("Input value is null!");
        } else if (!isSupportIudf()) {
            throw new IllegalStateException("Action is not permitted, the system does not support it!");
        } else if (name.contains(SEPARATOR_DOT_AND_SLASH)) {
            throw new IllegalArgumentException("Inputed file path contains illegal components!");
        } else if (context.isDeviceProtectedStorage()) {
            throw new IllegalAccessException("Could not set policy for DE context!");
        }
    }

    private static String getExactPath(Context context, String name) throws IllegalArgumentException, FileNotFoundException {
        String path;
        String fileName = name;
        String[] names;
        StringBuilder stringBuilder;
        if (new File(name).isAbsolute()) {
            String pkgName = context.getPackageName();
            String internDataDir = context.getDataDir().getPath();
            String internSdDataDir = new StringBuilder();
            internSdDataDir.append(context.getExternalCacheDirs()[0].getPath().split(pkgName)[0]);
            internSdDataDir.append(pkgName);
            internSdDataDir = internSdDataDir.toString();
            String internSdMediaDir = context.getExternalMediaDirs()[0].getPath();
            if (name.startsWith(internDataDir) || name.startsWith(internSdDataDir) || name.startsWith(internSdMediaDir)) {
                path = name;
            } else {
                throw new IllegalArgumentException("Inputed file path does not meet the requirement of absolute path!");
            }
        } else if (!name.contains(DOT)) {
            if (name.contains(SLASH)) {
                names = name.split(SLASH);
                fileName = names[names.length - 1];
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append(context.getDataDir());
            stringBuilder.append(SHARED_PREFS_PATH);
            stringBuilder.append(fileName);
            stringBuilder.append(SHARED_PREFS_SUFFIX);
            path = stringBuilder.toString();
        } else if (name.endsWith(TYPE_DB_TAG)) {
            if (name.contains(SLASH)) {
                names = name.split(SLASH);
                fileName = names[names.length - 1];
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append(context.getDataDir());
            stringBuilder.append(DB_PATH);
            stringBuilder.append(fileName);
            path = stringBuilder.toString();
        } else {
            if (name.contains(SLASH)) {
                names = name.split(SLASH);
                fileName = names[names.length - 1];
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append(context.getDataDir());
            stringBuilder.append(FILES_PATH);
            stringBuilder.append(fileName);
            path = stringBuilder.toString();
        }
        if (new File(path).exists()) {
            return path;
        }
        throw new FileNotFoundException("Target file does not exist!");
    }

    public static final boolean isSupportIudf() {
        return sIsSupportIudf;
    }
}
