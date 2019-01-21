package com.huawei.displayengine;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.displayengine.IDisplayEngineServiceEx.Stub;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DisplayEngineManager {
    public static final String SERVICE_NAME = "DisplayEngineExService";
    private static final String TAG = "DE J DisplayEngineManager";
    private static volatile HBMSceneFilter mHBMFilter;
    private final Context mContext;
    private volatile DisplayEngineLibraries mLibraries;
    private Object mLockLibraries;
    private Object mLockService;
    private Object mLockUptoXnit;
    private volatile IDisplayEngineServiceEx mService;
    private volatile HwMinLuminanceUptoXnit mUptoXnit;

    public Context getContext() {
        return this.mContext;
    }

    public IDisplayEngineServiceEx getService() {
        if (this.mService == null) {
            synchronized (this.mLockService) {
                if (this.mService == null) {
                    IBinder binder = ServiceManager.getService(SERVICE_NAME);
                    if (binder != null) {
                        this.mService = Stub.asInterface(binder);
                        if (this.mService == null) {
                            DElog.w(TAG, "service is null!");
                        }
                    } else {
                        this.mService = null;
                        DElog.w(TAG, "binder is null!");
                    }
                }
            }
        }
        return this.mService;
    }

    public HwMinLuminanceUptoXnit getUptoXnit() {
        if (this.mUptoXnit == null) {
            synchronized (this.mLockUptoXnit) {
                if (this.mUptoXnit == null) {
                    this.mUptoXnit = new HwMinLuminanceUptoXnit(this);
                }
            }
        }
        return this.mUptoXnit;
    }

    public DisplayEngineLibraries getLibraries() {
        if (this.mLibraries == null) {
            synchronized (this.mLockLibraries) {
                if (this.mLibraries == null) {
                    this.mLibraries = new DisplayEngineLibraries(this);
                }
            }
        }
        return this.mLibraries;
    }

    public static HBMSceneFilter getHBMFilter() {
        if (mHBMFilter == null) {
            synchronized (HBMSceneFilter.class) {
                if (mHBMFilter == null) {
                    mHBMFilter = new HBMSceneFilter();
                }
            }
        }
        return mHBMFilter;
    }

    public DisplayEngineManager() {
        this.mService = null;
        this.mLibraries = null;
        this.mContext = null;
        this.mLockService = new Object();
        this.mLockLibraries = new Object();
        this.mLockUptoXnit = new Object();
    }

    public DisplayEngineManager(Context context) {
        this.mService = null;
        this.mLibraries = null;
        this.mContext = context;
        this.mLockService = new Object();
        this.mLockLibraries = new Object();
        this.mLockUptoXnit = new Object();
    }

    public int getSupported(int feature) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.getSupported(feature);
            }
            return 0;
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getSupported(");
            stringBuilder.append(feature);
            stringBuilder.append(") has remote exception:");
            stringBuilder.append(e.getMessage());
            DElog.e(str, stringBuilder.toString());
            return 0;
        }
    }

    public int setScene(int scene, int action) {
        int ret = 0;
        if (getHBMFilter().check(scene, action)) {
            return 0;
        }
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                ret = service.setScene(scene, action);
            }
            getLibraries().setScene(scene, action);
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("setScene(");
            stringBuilder.append(scene);
            stringBuilder.append(", ");
            stringBuilder.append(action);
            stringBuilder.append(") has remote exception:");
            stringBuilder.append(e.getMessage());
            DElog.e(str, stringBuilder.toString());
        }
        return ret;
    }

    public void updateLightSensorState(boolean sensorEnable) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                service.updateLightSensorState(sensorEnable);
            }
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("updateLightSensorState has remote exception:");
            stringBuilder.append(e.getMessage());
            DElog.e(str, stringBuilder.toString());
        }
    }

    public int setData(int type, PersistableBundle data) {
        if (type == 6) {
            return getUptoXnit().setXnit(data.getInt("MinBrightness"), data.getInt("MaxBrightness"), data.getInt("brightnesslevel"));
        }
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.setData(type, data);
            }
            return 0;
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("setData(");
            stringBuilder.append(type);
            stringBuilder.append(", ");
            stringBuilder.append(data);
            stringBuilder.append(") has remote exception:");
            stringBuilder.append(e.getMessage());
            DElog.e(str, stringBuilder.toString());
            return 0;
        }
    }

    public int sendMessage(int messageID, Bundle data) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.sendMessage(messageID, data);
            }
            return 0;
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sendMessage(");
            stringBuilder.append(messageID);
            stringBuilder.append(", ");
            stringBuilder.append(data);
            stringBuilder.append(") has remote exception:");
            stringBuilder.append(e.getMessage());
            DElog.e(str, stringBuilder.toString());
            return 0;
        }
    }

    public int getEffect(int feature, int type, byte[] status, int length) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.getEffect(feature, type, status, length);
            }
            return 0;
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getEffect(");
            stringBuilder.append(feature);
            stringBuilder.append(", ");
            stringBuilder.append(type);
            stringBuilder.append(", ");
            stringBuilder.append(Arrays.toString(status));
            stringBuilder.append(", ");
            stringBuilder.append(length);
            stringBuilder.append(") has remote exception:");
            stringBuilder.append(e.getMessage());
            DElog.e(str, stringBuilder.toString());
            return 0;
        }
    }

    public int setEffect(int feature, int mode, PersistableBundle data) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.setEffect(feature, mode, data);
            }
            return 0;
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("setEffect(");
            stringBuilder.append(feature);
            stringBuilder.append(", ");
            stringBuilder.append(mode);
            stringBuilder.append(", ");
            stringBuilder.append(data);
            stringBuilder.append(") has remote exception:");
            stringBuilder.append(e.getMessage());
            DElog.e(str, stringBuilder.toString());
            return 0;
        }
    }

    public List<Bundle> getAllRecords(String name) {
        return getAllRecords(name, null);
    }

    public List<Bundle> getAllRecords(String name, Bundle info) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.getAllRecords(name, info);
            }
            return null;
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getAllRecords(");
            stringBuilder.append(name);
            stringBuilder.append(", ");
            stringBuilder.append(info);
            stringBuilder.append(") has remote exception:");
            stringBuilder.append(e.getMessage());
            DElog.e(str, stringBuilder.toString());
            return null;
        }
    }

    public Object imageProcess(String command, Map<String, Object> param) {
        return getLibraries().imageProcess(command, param);
    }

    public int brightnessTrainingProcess() {
        DElog.i(TAG, "brightnessTrainingProcess ");
        return getLibraries().brightnessTrainingProcess(null, null);
    }

    public int brightnessTrainingAbort() {
        DElog.i(TAG, "brightnessTrainingAbort ");
        return getLibraries().brightnessTrainingAbort();
    }

    public int setDataToFilter(String filterName, Bundle data) {
        if (filterName.equals("HBM")) {
            return getHBMFilter().setData(data);
        }
        return 0;
    }
}
