package android.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.BitUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ScanFilter implements Parcelable {
    public static final Creator<ScanFilter> CREATOR = new Creator<ScanFilter>() {
        public ScanFilter[] newArray(int size) {
            return new ScanFilter[size];
        }

        public ScanFilter createFromParcel(Parcel in) {
            ParcelUuid uuid;
            Builder builder = new Builder();
            if (in.readInt() == 1) {
                builder.setDeviceName(in.readString());
            }
            if (in.readInt() == 1) {
                builder.setDeviceAddress(in.readString());
            }
            if (in.readInt() == 1) {
                uuid = (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader());
                builder.setServiceUuid(uuid);
                if (in.readInt() == 1) {
                    builder.setServiceUuid(uuid, (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader()));
                }
            }
            if (in.readInt() == 1) {
                uuid = (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader());
                if (in.readInt() == 1) {
                    byte[] serviceData = new byte[in.readInt()];
                    in.readByteArray(serviceData);
                    if (in.readInt() == 0) {
                        builder.setServiceData(uuid, serviceData);
                    } else {
                        byte[] serviceDataMask = new byte[in.readInt()];
                        in.readByteArray(serviceDataMask);
                        builder.setServiceData(uuid, serviceData, serviceDataMask);
                    }
                }
            }
            int manufacturerId = in.readInt();
            if (in.readInt() == 1) {
                byte[] manufacturerData = new byte[in.readInt()];
                in.readByteArray(manufacturerData);
                if (in.readInt() == 0) {
                    builder.setManufacturerData(manufacturerId, manufacturerData);
                } else {
                    byte[] manufacturerDataMask = new byte[in.readInt()];
                    in.readByteArray(manufacturerDataMask);
                    builder.setManufacturerData(manufacturerId, manufacturerData, manufacturerDataMask);
                }
            }
            return builder.build();
        }
    };
    public static final ScanFilter EMPTY = new Builder().build();
    private final String mDeviceAddress;
    private final String mDeviceName;
    private final byte[] mManufacturerData;
    private final byte[] mManufacturerDataMask;
    private final int mManufacturerId;
    private final byte[] mServiceData;
    private final byte[] mServiceDataMask;
    private final ParcelUuid mServiceDataUuid;
    private final ParcelUuid mServiceUuid;
    private final ParcelUuid mServiceUuidMask;

    public static final class Builder {
        private String mDeviceAddress;
        private String mDeviceName;
        private byte[] mManufacturerData;
        private byte[] mManufacturerDataMask;
        private int mManufacturerId = -1;
        private byte[] mServiceData;
        private byte[] mServiceDataMask;
        private ParcelUuid mServiceDataUuid;
        private ParcelUuid mServiceUuid;
        private ParcelUuid mUuidMask;

        public Builder setDeviceName(String deviceName) {
            this.mDeviceName = deviceName;
            return this;
        }

        public Builder setDeviceAddress(String deviceAddress) {
            if (deviceAddress == null || BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
                this.mDeviceAddress = deviceAddress;
                return this;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("invalid device address ");
            stringBuilder.append(deviceAddress);
            throw new IllegalArgumentException(stringBuilder.toString());
        }

        public Builder setServiceUuid(ParcelUuid serviceUuid) {
            this.mServiceUuid = serviceUuid;
            this.mUuidMask = null;
            return this;
        }

        public Builder setServiceUuid(ParcelUuid serviceUuid, ParcelUuid uuidMask) {
            if (this.mUuidMask == null || this.mServiceUuid != null) {
                this.mServiceUuid = serviceUuid;
                this.mUuidMask = uuidMask;
                return this;
            }
            throw new IllegalArgumentException("uuid is null while uuidMask is not null!");
        }

        public Builder setServiceData(ParcelUuid serviceDataUuid, byte[] serviceData) {
            if (serviceDataUuid != null) {
                this.mServiceDataUuid = serviceDataUuid;
                this.mServiceData = serviceData;
                this.mServiceDataMask = null;
                return this;
            }
            throw new IllegalArgumentException("serviceDataUuid is null");
        }

        public Builder setServiceData(ParcelUuid serviceDataUuid, byte[] serviceData, byte[] serviceDataMask) {
            if (serviceDataUuid != null) {
                if (this.mServiceDataMask != null) {
                    if (this.mServiceData == null) {
                        throw new IllegalArgumentException("serviceData is null while serviceDataMask is not null");
                    } else if (this.mServiceData.length != this.mServiceDataMask.length) {
                        throw new IllegalArgumentException("size mismatch for service data and service data mask");
                    }
                }
                this.mServiceDataUuid = serviceDataUuid;
                this.mServiceData = serviceData;
                this.mServiceDataMask = serviceDataMask;
                return this;
            }
            throw new IllegalArgumentException("serviceDataUuid is null");
        }

        public Builder setManufacturerData(int manufacturerId, byte[] manufacturerData) {
            if (manufacturerData == null || manufacturerId >= 0) {
                this.mManufacturerId = manufacturerId;
                this.mManufacturerData = manufacturerData;
                this.mManufacturerDataMask = null;
                return this;
            }
            throw new IllegalArgumentException("invalid manufacture id");
        }

        public Builder setManufacturerData(int manufacturerId, byte[] manufacturerData, byte[] manufacturerDataMask) {
            if (manufacturerData == null || manufacturerId >= 0) {
                if (this.mManufacturerDataMask != null) {
                    if (this.mManufacturerData == null) {
                        throw new IllegalArgumentException("manufacturerData is null while manufacturerDataMask is not null");
                    } else if (this.mManufacturerData.length != this.mManufacturerDataMask.length) {
                        throw new IllegalArgumentException("size mismatch for manufacturerData and manufacturerDataMask");
                    }
                }
                this.mManufacturerId = manufacturerId;
                this.mManufacturerData = manufacturerData;
                this.mManufacturerDataMask = manufacturerDataMask;
                return this;
            }
            throw new IllegalArgumentException("invalid manufacture id");
        }

        public ScanFilter build() {
            return new ScanFilter(this.mDeviceName, this.mDeviceAddress, this.mServiceUuid, this.mUuidMask, this.mServiceDataUuid, this.mServiceData, this.mServiceDataMask, this.mManufacturerId, this.mManufacturerData, this.mManufacturerDataMask, null);
        }
    }

    /* synthetic */ ScanFilter(String x0, String x1, ParcelUuid x2, ParcelUuid x3, ParcelUuid x4, byte[] x5, byte[] x6, int x7, byte[] x8, byte[] x9, AnonymousClass1 x10) {
        this(x0, x1, x2, x3, x4, x5, x6, x7, x8, x9);
    }

    private ScanFilter(String name, String deviceAddress, ParcelUuid uuid, ParcelUuid uuidMask, ParcelUuid serviceDataUuid, byte[] serviceData, byte[] serviceDataMask, int manufacturerId, byte[] manufacturerData, byte[] manufacturerDataMask) {
        this.mDeviceName = name;
        this.mServiceUuid = uuid;
        this.mServiceUuidMask = uuidMask;
        this.mDeviceAddress = deviceAddress;
        this.mServiceDataUuid = serviceDataUuid;
        this.mServiceData = serviceData;
        this.mServiceDataMask = serviceDataMask;
        this.mManufacturerId = manufacturerId;
        this.mManufacturerData = manufacturerData;
        this.mManufacturerDataMask = manufacturerDataMask;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        dest.writeInt(this.mDeviceName == null ? 0 : 1);
        if (this.mDeviceName != null) {
            dest.writeString(this.mDeviceName);
        }
        dest.writeInt(this.mDeviceAddress == null ? 0 : 1);
        if (this.mDeviceAddress != null) {
            dest.writeString(this.mDeviceAddress);
        }
        dest.writeInt(this.mServiceUuid == null ? 0 : 1);
        if (this.mServiceUuid != null) {
            dest.writeParcelable(this.mServiceUuid, flags);
            dest.writeInt(this.mServiceUuidMask == null ? 0 : 1);
            if (this.mServiceUuidMask != null) {
                dest.writeParcelable(this.mServiceUuidMask, flags);
            }
        }
        dest.writeInt(this.mServiceDataUuid == null ? 0 : 1);
        if (this.mServiceDataUuid != null) {
            dest.writeParcelable(this.mServiceDataUuid, flags);
            dest.writeInt(this.mServiceData == null ? 0 : 1);
            if (this.mServiceData != null) {
                dest.writeInt(this.mServiceData.length);
                dest.writeByteArray(this.mServiceData);
                dest.writeInt(this.mServiceDataMask == null ? 0 : 1);
                if (this.mServiceDataMask != null) {
                    dest.writeInt(this.mServiceDataMask.length);
                    dest.writeByteArray(this.mServiceDataMask);
                }
            }
        }
        dest.writeInt(this.mManufacturerId);
        dest.writeInt(this.mManufacturerData == null ? 0 : 1);
        if (this.mManufacturerData != null) {
            dest.writeInt(this.mManufacturerData.length);
            dest.writeByteArray(this.mManufacturerData);
            if (this.mManufacturerDataMask == null) {
                i = 0;
            }
            dest.writeInt(i);
            if (this.mManufacturerDataMask != null) {
                dest.writeInt(this.mManufacturerDataMask.length);
                dest.writeByteArray(this.mManufacturerDataMask);
            }
        }
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public ParcelUuid getServiceUuid() {
        return this.mServiceUuid;
    }

    public ParcelUuid getServiceUuidMask() {
        return this.mServiceUuidMask;
    }

    public String getDeviceAddress() {
        return this.mDeviceAddress;
    }

    public byte[] getServiceData() {
        return this.mServiceData;
    }

    public byte[] getServiceDataMask() {
        return this.mServiceDataMask;
    }

    public ParcelUuid getServiceDataUuid() {
        return this.mServiceDataUuid;
    }

    public int getManufacturerId() {
        return this.mManufacturerId;
    }

    public byte[] getManufacturerData() {
        return this.mManufacturerData;
    }

    public byte[] getManufacturerDataMask() {
        return this.mManufacturerDataMask;
    }

    public boolean matches(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        BluetoothDevice device = scanResult.getDevice();
        if (this.mDeviceAddress != null && (device == null || !this.mDeviceAddress.equals(device.getAddress()))) {
            return false;
        }
        ScanRecord scanRecord = scanResult.getScanRecord();
        if (scanRecord == null && (this.mDeviceName != null || this.mServiceUuid != null || this.mManufacturerData != null || this.mServiceData != null)) {
            return false;
        }
        if (this.mDeviceName != null && !this.mDeviceName.equals(scanRecord.getDeviceName())) {
            return false;
        }
        if (this.mServiceUuid != null && !matchesServiceUuids(this.mServiceUuid, this.mServiceUuidMask, scanRecord.getServiceUuids())) {
            return false;
        }
        if (this.mServiceDataUuid != null && !matchesPartialData(this.mServiceData, this.mServiceDataMask, scanRecord.getServiceData(this.mServiceDataUuid))) {
            return false;
        }
        if (this.mManufacturerId < 0 || matchesPartialData(this.mManufacturerData, this.mManufacturerDataMask, scanRecord.getManufacturerSpecificData(this.mManufacturerId))) {
            return true;
        }
        return false;
    }

    public static boolean matchesServiceUuids(ParcelUuid uuid, ParcelUuid parcelUuidMask, List<ParcelUuid> uuids) {
        if (uuid == null) {
            return true;
        }
        if (uuids == null) {
            return false;
        }
        for (ParcelUuid parcelUuid : uuids) {
            if (matchesServiceUuid(uuid.getUuid(), parcelUuidMask == null ? null : parcelUuidMask.getUuid(), parcelUuid.getUuid())) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesServiceUuid(UUID uuid, UUID mask, UUID data) {
        return BitUtils.maskedEquals(data, uuid, mask);
    }

    private boolean matchesPartialData(byte[] data, byte[] dataMask, byte[] parsedData) {
        if (parsedData == null || parsedData.length < data.length) {
            return false;
        }
        int i;
        if (dataMask == null) {
            for (i = 0; i < data.length; i++) {
                if (parsedData[i] != data[i]) {
                    return false;
                }
            }
            return true;
        }
        for (i = 0; i < data.length; i++) {
            if ((dataMask[i] & parsedData[i]) != (dataMask[i] & data[i])) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("BluetoothLeScanFilter [mDeviceName=");
        stringBuilder.append(this.mDeviceName);
        stringBuilder.append(", mDeviceAddress=");
        stringBuilder.append(this.mDeviceAddress);
        stringBuilder.append(", mUuid=");
        stringBuilder.append(this.mServiceUuid);
        stringBuilder.append(", mUuidMask=");
        stringBuilder.append(this.mServiceUuidMask);
        stringBuilder.append(", mServiceDataUuid=");
        stringBuilder.append(Objects.toString(this.mServiceDataUuid));
        stringBuilder.append(", mServiceData=");
        stringBuilder.append(Arrays.toString(this.mServiceData));
        stringBuilder.append(", mServiceDataMask=");
        stringBuilder.append(Arrays.toString(this.mServiceDataMask));
        stringBuilder.append(", mManufacturerId=");
        stringBuilder.append(this.mManufacturerId);
        stringBuilder.append(", mManufacturerData=");
        stringBuilder.append(Arrays.toString(this.mManufacturerData));
        stringBuilder.append(", mManufacturerDataMask=");
        stringBuilder.append(Arrays.toString(this.mManufacturerDataMask));
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mDeviceName, this.mDeviceAddress, Integer.valueOf(this.mManufacturerId), Integer.valueOf(Arrays.hashCode(this.mManufacturerData)), Integer.valueOf(Arrays.hashCode(this.mManufacturerDataMask)), this.mServiceDataUuid, Integer.valueOf(Arrays.hashCode(this.mServiceData)), Integer.valueOf(Arrays.hashCode(this.mServiceDataMask)), this.mServiceUuid, this.mServiceUuidMask});
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ScanFilter other = (ScanFilter) obj;
        if (!(Objects.equals(this.mDeviceName, other.mDeviceName) && Objects.equals(this.mDeviceAddress, other.mDeviceAddress) && this.mManufacturerId == other.mManufacturerId && Objects.deepEquals(this.mManufacturerData, other.mManufacturerData) && Objects.deepEquals(this.mManufacturerDataMask, other.mManufacturerDataMask) && Objects.equals(this.mServiceDataUuid, other.mServiceDataUuid) && Objects.deepEquals(this.mServiceData, other.mServiceData) && Objects.deepEquals(this.mServiceDataMask, other.mServiceDataMask) && Objects.equals(this.mServiceUuid, other.mServiceUuid) && Objects.equals(this.mServiceUuidMask, other.mServiceUuidMask))) {
            z = false;
        }
        return z;
    }

    public boolean isAllFieldsEmpty() {
        return EMPTY.equals(this);
    }
}
