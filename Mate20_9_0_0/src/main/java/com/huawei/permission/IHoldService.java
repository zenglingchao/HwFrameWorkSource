package com.huawei.permission;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHoldService extends IInterface {

    public static abstract class Stub extends Binder implements IHoldService {
        private static final String DESCRIPTOR = "com.huawei.permission.IHoldService";
        static final int TRANSACTION_addRecord = 4;
        static final int TRANSACTION_authenticateSmsSend = 11;
        static final int TRANSACTION_callHsmService = 9;
        static final int TRANSACTION_checkBeforeShowDialog = 6;
        static final int TRANSACTION_checkBeforeShowDialogWithPid = 7;
        static final int TRANSACTION_checkPreBlock = 10;
        static final int TRANSACTION_checkSystemAppInternal = 2;
        static final int TRANSACTION_getAlwaysForbiddenPerms = 20;
        static final int TRANSACTION_getPendingIntent = 3;
        static final int TRANSACTION_holdServiceByRequestPermission = 1;
        static final int TRANSACTION_notifyBackgroundMgr = 12;
        static final int TRANSACTION_queryAllMaliPkgs = 13;
        static final int TRANSACTION_queryMaliAppInfoByPkg = 14;
        static final int TRANSACTION_queryMaliAppInfoShort = 15;
        static final int TRANSACTION_registMaliAppInfoListener = 16;
        static final int TRANSACTION_releaseHoldService = 5;
        static final int TRANSACTION_removeRuntimePermissions = 8;
        static final int TRANSACTION_reportMaliInfoBean = 19;
        static final int TRANSACTION_setRestrictStatus = 18;
        static final int TRANSACTION_unregistMaliAppInfoListener = 17;

        private static class Proxy implements IHoldService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public int holdServiceByRequestPermission(int uid, int pid, int permissionType, String desAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(permissionType);
                    _data.writeString(desAddr);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean checkSystemAppInternal(int callUid, boolean excludeCust) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callUid);
                    _data.writeInt(excludeCust);
                    boolean z = false;
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PendingIntent getPendingIntent(int requestCode, Intent intent, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PendingIntent _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestCode);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (PendingIntent) PendingIntent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addRecord(int uid, int permissionType, int state, boolean click) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(permissionType);
                    _data.writeInt(state);
                    _data.writeInt(click);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int releaseHoldService(int uid, int userSelection, int permissionType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(userSelection);
                    _data.writeInt(permissionType);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkBeforeShowDialog(int uid, int permissionType, String desAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(permissionType);
                    _data.writeString(desAddr);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkBeforeShowDialogWithPid(int uid, int pid, int permissionType, String desAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(permissionType);
                    _data.writeString(desAddr);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeRuntimePermissions(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle callHsmService(String method, Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(method);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean checkPreBlock(int callUid, int permissionType, boolean showToast) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callUid);
                    _data.writeInt(permissionType);
                    _data.writeInt(showToast);
                    boolean z = false;
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void authenticateSmsSend(IBinder notifyResult, int uidOf3RdApk, int smsId, String smsBody, String smsAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(notifyResult);
                    _data.writeInt(uidOf3RdApk);
                    _data.writeInt(smsId);
                    _data.writeString(smsBody);
                    _data.writeString(smsAddress);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyBackgroundMgr(String pkgName, int uidOf3RdApk, int permType, int permCfg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(uidOf3RdApk);
                    _data.writeInt(permType);
                    _data.writeInt(permCfg);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> queryAllMaliPkgs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<MaliInfoBean> queryMaliAppInfoByPkg(String packageName, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    List<MaliInfoBean> _result = _reply.createTypedArrayList(MaliInfoBean.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<MaliInfoBean> queryMaliAppInfoShort(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    List<MaliInfoBean> _result = _reply.createTypedArrayList(MaliInfoBean.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registMaliAppInfoListener(IHsmMaliAppInfoListener listener, int hashCode, int flags, int priority) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(hashCode);
                    _data.writeInt(flags);
                    _data.writeInt(priority);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregistMaliAppInfoListener(int hashCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hashCode);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRestrictStatus(String maliciousApp, boolean isRestricted) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(maliciousApp);
                    _data.writeInt(isRestricted);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportMaliInfoBean(List<MaliInfoBean> beans, boolean clearBefore) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(beans);
                    _data.writeInt(clearBefore);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getAlwaysForbiddenPerms() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHoldService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHoldService)) {
                return new Proxy(obj);
            }
            return (IHoldService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            String descriptor = DESCRIPTOR;
            if (i != 1598968902) {
                Intent _arg1 = null;
                boolean _arg3 = false;
                int _result;
                int _arg0;
                int _arg02;
                int _arg2;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(descriptor);
                        _result = holdServiceByRequestPermission(data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(descriptor);
                        _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        boolean _result2 = checkSystemAppInternal(_arg0, _arg3);
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(descriptor);
                        _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        PendingIntent _result3 = getPendingIntent(_arg02, _arg1, data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            parcel2.writeInt(1);
                            _result3.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 4:
                        parcel.enforceInterface(descriptor);
                        _arg0 = data.readInt();
                        _arg02 = data.readInt();
                        _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        addRecord(_arg0, _arg02, _arg2, _arg3);
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(descriptor);
                        _arg2 = releaseHoldService(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_arg2);
                        return true;
                    case 6:
                        parcel.enforceInterface(descriptor);
                        _arg2 = checkBeforeShowDialog(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_arg2);
                        return true;
                    case 7:
                        parcel.enforceInterface(descriptor);
                        _result = checkBeforeShowDialogWithPid(data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 8:
                        parcel.enforceInterface(descriptor);
                        removeRuntimePermissions(data.readString());
                        reply.writeNoException();
                        return true;
                    case 9:
                        Bundle _arg12;
                        parcel.enforceInterface(descriptor);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        Bundle _result4 = callHsmService(_arg03, _arg12);
                        reply.writeNoException();
                        if (_result4 != null) {
                            parcel2.writeInt(1);
                            _result4.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 10:
                        parcel.enforceInterface(descriptor);
                        _arg0 = data.readInt();
                        _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        boolean _result5 = checkPreBlock(_arg0, _arg02, _arg3);
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 11:
                        parcel.enforceInterface(descriptor);
                        authenticateSmsSend(data.readStrongBinder(), data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 12:
                        parcel.enforceInterface(descriptor);
                        notifyBackgroundMgr(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        parcel.enforceInterface(descriptor);
                        List<String> _result6 = queryAllMaliPkgs();
                        reply.writeNoException();
                        parcel2.writeStringList(_result6);
                        return true;
                    case 14:
                        parcel.enforceInterface(descriptor);
                        List<MaliInfoBean> _result7 = queryMaliAppInfoByPkg(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result7);
                        return true;
                    case 15:
                        parcel.enforceInterface(descriptor);
                        List<MaliInfoBean> _result8 = queryMaliAppInfoShort(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result8);
                        return true;
                    case 16:
                        parcel.enforceInterface(descriptor);
                        registMaliAppInfoListener(com.huawei.permission.IHsmMaliAppInfoListener.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 17:
                        parcel.enforceInterface(descriptor);
                        unregistMaliAppInfoListener(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 18:
                        parcel.enforceInterface(descriptor);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        setRestrictStatus(_arg04, _arg3);
                        reply.writeNoException();
                        return true;
                    case 19:
                        parcel.enforceInterface(descriptor);
                        List<MaliInfoBean> _arg05 = parcel.createTypedArrayList(MaliInfoBean.CREATOR);
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        reportMaliInfoBean(_arg05, _arg3);
                        reply.writeNoException();
                        return true;
                    case 20:
                        parcel.enforceInterface(descriptor);
                        String[] _result9 = getAlwaysForbiddenPerms();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result9);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            }
            parcel2.writeString(descriptor);
            return true;
        }
    }

    void addRecord(int i, int i2, int i3, boolean z) throws RemoteException;

    void authenticateSmsSend(IBinder iBinder, int i, int i2, String str, String str2) throws RemoteException;

    Bundle callHsmService(String str, Bundle bundle) throws RemoteException;

    int checkBeforeShowDialog(int i, int i2, String str) throws RemoteException;

    int checkBeforeShowDialogWithPid(int i, int i2, int i3, String str) throws RemoteException;

    boolean checkPreBlock(int i, int i2, boolean z) throws RemoteException;

    boolean checkSystemAppInternal(int i, boolean z) throws RemoteException;

    String[] getAlwaysForbiddenPerms() throws RemoteException;

    PendingIntent getPendingIntent(int i, Intent intent, int i2) throws RemoteException;

    int holdServiceByRequestPermission(int i, int i2, int i3, String str) throws RemoteException;

    void notifyBackgroundMgr(String str, int i, int i2, int i3) throws RemoteException;

    List<String> queryAllMaliPkgs() throws RemoteException;

    List<MaliInfoBean> queryMaliAppInfoByPkg(String str, int i) throws RemoteException;

    List<MaliInfoBean> queryMaliAppInfoShort(int i) throws RemoteException;

    void registMaliAppInfoListener(IHsmMaliAppInfoListener iHsmMaliAppInfoListener, int i, int i2, int i3) throws RemoteException;

    int releaseHoldService(int i, int i2, int i3) throws RemoteException;

    void removeRuntimePermissions(String str) throws RemoteException;

    void reportMaliInfoBean(List<MaliInfoBean> list, boolean z) throws RemoteException;

    void setRestrictStatus(String str, boolean z) throws RemoteException;

    void unregistMaliAppInfoListener(int i) throws RemoteException;
}
