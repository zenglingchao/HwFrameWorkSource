package com.huawei.attestation;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.attestation.IHwAttestationService.Stub;
import java.nio.charset.StandardCharsets;

public class HwAttestationManager {
    public static final int AUTH_OK = 0;
    public static final int DEVICE_ID_TYPE_EMMC = 1;
    public static final int KEY_INDEX_GENERAL = 2;
    public static final int KEY_INDEX_HWCLOUD = 1;
    public static final int STATE_ERR_DEVICE_KEY = -2;
    public static final int STATE_ERR_GET_CERT = -8;
    public static final int STATE_ERR_GET_CERT_TYPE = -7;
    public static final int STATE_ERR_GET_PUBKEY = -6;
    public static final int STATE_ERR_INPUT_PARAMETER = -4;
    public static final int STATE_ERR_NO_ATTESTATION_SERVICE = -1;
    public static final int STATE_ERR_PERMISSION_DENIED = -5;
    public static final int STATE_ERR_READ_EMMCID = -3;
    public static final int STATE_ERR_UNKNOW = -10;
    public static final int STATE_OK = 0;
    private static final String TAG = "HwAttestationManager";
    private IHwAttestationService mService;

    public int getLastError() {
        this.mService = getHwAttestationService();
        if (this.mService == null) {
            Log.e(TAG, "getState DeviceAttestation service is null");
            return -1;
        }
        try {
            int ret = this.mService.getLastError();
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mService.getState() ret = ");
            stringBuilder.append(ret);
            Log.d(str, stringBuilder.toString());
            return ret;
        } catch (RemoteException e) {
            e.printStackTrace();
            return -10;
        }
    }

    public byte[] getAttestationSignatureWithPkgName(int keyIndex, int deviceIdType, String signatureType, byte[] challenge, String packageName) {
        this.mService = getHwAttestationService();
        if (this.mService == null) {
            Log.e(TAG, "getSignature DeviceAttestation service is null");
            return new byte[0];
        } else if (signatureType.length() > AppOpsManagerEx.TYPE_MICROPHONE || challenge.length > 64) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("oversize : signatureType length");
            stringBuilder.append(signatureType.length());
            stringBuilder.append("challenge length ");
            stringBuilder.append(challenge.length);
            Log.e(str, stringBuilder.toString());
            return new byte[0];
        } else {
            try {
                return this.mService.getAttestationSignatureWithPkgName(keyIndex, deviceIdType, signatureType, challenge, packageName);
            } catch (RemoteException e) {
                Log.e(TAG, "getAttestationSignatureWithPkgName failed", e);
                return new byte[0];
            }
        }
    }

    public byte[] getAttestationSignature(int keyIndex, int deviceIdType, String signatureType, byte[] challenge) {
        this.mService = getHwAttestationService();
        if (this.mService == null) {
            Log.e(TAG, "getSignature DeviceAttestation service is null");
            return new byte[0];
        } else if (signatureType.length() > AppOpsManagerEx.TYPE_MICROPHONE || challenge.length > 64) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("oversize : signatureType length");
            stringBuilder.append(signatureType.length());
            stringBuilder.append("challenge length ");
            stringBuilder.append(challenge.length);
            Log.e(str, stringBuilder.toString());
            return new byte[0];
        } else {
            try {
                return this.mService.getAttestationSignature(keyIndex, deviceIdType, signatureType, challenge);
            } catch (RemoteException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }
    }

    public byte[] getDeviceID(int deviceIdType) {
        this.mService = getHwAttestationService();
        if (this.mService == null) {
            Log.e(TAG, "getDeviceID DeviceAttestation service is null");
            return null;
        }
        try {
            return this.mService.getDeviceID(deviceIdType);
        } catch (RemoteException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static String getPublickKey(int keyIndex) {
        IHwAttestationService attService = getHwAttestationService();
        if (attService == null) {
            Log.e(TAG, "getDeviceID DeviceAttestation service is null");
            return null;
        }
        byte[] pubKey = new byte[1024];
        try {
            int actLen = attService.getPublickKey(keyIndex, pubKey);
            if (actLen > 0) {
                return new String(pubKey, 0, actLen, StandardCharsets.UTF_8);
            }
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("attService.getPublickKey error: ");
            stringBuilder.append(actLen);
            Log.e(str, stringBuilder.toString());
            return null;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static IHwAttestationService getHwAttestationService() {
        return Stub.asInterface(ServiceManager.getService("attestation_service"));
    }
}
