package com.android.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IImsServiceFeatureCallback extends IInterface {

    public static abstract class Stub extends Binder implements IImsServiceFeatureCallback {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsServiceFeatureCallback";
        static final int TRANSACTION_imsFeatureCreated = 1;
        static final int TRANSACTION_imsFeatureRemoved = 2;
        static final int TRANSACTION_imsStatusChanged = 3;

        private static class Proxy implements IImsServiceFeatureCallback {
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

            public void imsFeatureCreated(int slotId, int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(feature);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void imsFeatureRemoved(int slotId, int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(feature);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void imsStatusChanged(int slotId, int feature, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(feature);
                    _data.writeInt(status);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsServiceFeatureCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsServiceFeatureCallback)) {
                return new Proxy(obj);
            }
            return (IImsServiceFeatureCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = DESCRIPTOR;
            if (code != IBinder.INTERFACE_TRANSACTION) {
                switch (code) {
                    case 1:
                        data.enforceInterface(descriptor);
                        imsFeatureCreated(data.readInt(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(descriptor);
                        imsFeatureRemoved(data.readInt(), data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(descriptor);
                        imsStatusChanged(data.readInt(), data.readInt(), data.readInt());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            }
            reply.writeString(descriptor);
            return true;
        }
    }

    void imsFeatureCreated(int i, int i2) throws RemoteException;

    void imsFeatureRemoved(int i, int i2) throws RemoteException;

    void imsStatusChanged(int i, int i2, int i3) throws RemoteException;
}
