package android.hardware.usb;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;

public class UsbDevice implements Parcelable {
    public static final Creator<UsbDevice> CREATOR = new Creator<UsbDevice>() {
        public UsbDevice createFromParcel(Parcel in) {
            String name = in.readString();
            int vendorId = in.readInt();
            int productId = in.readInt();
            int clasz = in.readInt();
            int subClass = in.readInt();
            int protocol = in.readInt();
            String manufacturerName = in.readString();
            String productName = in.readString();
            String version = in.readString();
            String serialNumber = in.readString();
            Parcelable[] configurations = in.readParcelableArray(UsbInterface.class.getClassLoader());
            UsbDevice device = new UsbDevice(name, vendorId, productId, clasz, subClass, protocol, manufacturerName, productName, version, serialNumber);
            device.setConfigurations(configurations);
            device.setBackupSubProductId(in.readInt());
            return device;
        }

        public UsbDevice[] newArray(int size) {
            return new UsbDevice[size];
        }
    };
    private static final boolean DEBUG = false;
    private static final String TAG = "UsbDevice";
    private final int mClass;
    private Parcelable[] mConfigurations;
    private UsbInterface[] mInterfaces;
    private final String mManufacturerName;
    private final String mName;
    private final int mProductId;
    private final String mProductName;
    private final int mProtocol;
    private final String mSerialNumber;
    private int mSubProductId = -1;
    private final int mSubclass;
    private final int mVendorId;
    private final String mVersion;

    private static native int native_get_device_id(String str);

    private static native String native_get_device_name(int i);

    public UsbDevice(String name, int vendorId, int productId, int Class, int subClass, int protocol, String manufacturerName, String productName, String version, String serialNumber) {
        this.mName = (String) Preconditions.checkNotNull(name);
        this.mVendorId = vendorId;
        this.mProductId = productId;
        this.mClass = Class;
        this.mSubclass = subClass;
        this.mProtocol = protocol;
        this.mManufacturerName = manufacturerName;
        this.mProductName = productName;
        this.mVersion = (String) Preconditions.checkStringNotEmpty(version);
        this.mSerialNumber = serialNumber;
    }

    public String getDeviceName() {
        return this.mName;
    }

    public String getManufacturerName() {
        return this.mManufacturerName;
    }

    public String getProductName() {
        return this.mProductName;
    }

    public String getVersion() {
        return this.mVersion;
    }

    public String getSerialNumber() {
        return this.mSerialNumber;
    }

    public int getDeviceId() {
        return getDeviceId(this.mName);
    }

    public int getVendorId() {
        return this.mVendorId;
    }

    public int getProductId() {
        return this.mProductId;
    }

    public int getDeviceClass() {
        return this.mClass;
    }

    public int getDeviceSubclass() {
        return this.mSubclass;
    }

    public int getDeviceProtocol() {
        return this.mProtocol;
    }

    public int getConfigurationCount() {
        return this.mConfigurations.length;
    }

    public void setBackupSubProductId(int info) {
        this.mSubProductId = info;
    }

    public int getBackupSubProductId() {
        return this.mSubProductId;
    }

    public String getBackupSubDeviceName() {
        if (this.mSubProductId == 0) {
            return "HUAWEI Back-up";
        }
        if (1 == this.mSubProductId) {
            return "HONOR Storage";
        }
        return "";
    }

    public UsbConfiguration getConfiguration(int index) {
        return (UsbConfiguration) this.mConfigurations[index];
    }

    private UsbInterface[] getInterfaceList() {
        if (this.mInterfaces == null) {
            int interfaceCount = 0;
            for (UsbConfiguration configuration : this.mConfigurations) {
                interfaceCount += configuration.getInterfaceCount();
            }
            this.mInterfaces = new UsbInterface[interfaceCount];
            int offset = 0;
            int i = 0;
            while (i < configurationCount) {
                UsbConfiguration configuration2 = this.mConfigurations[i];
                interfaceCount = configuration2.getInterfaceCount();
                int offset2 = offset;
                offset = 0;
                while (offset < interfaceCount) {
                    int offset3 = offset2 + 1;
                    this.mInterfaces[offset2] = configuration2.getInterface(offset);
                    offset++;
                    offset2 = offset3;
                }
                i++;
                offset = offset2;
            }
        }
        return this.mInterfaces;
    }

    public int getInterfaceCount() {
        return getInterfaceList().length;
    }

    public UsbInterface getInterface(int index) {
        return getInterfaceList()[index];
    }

    public void setConfigurations(Parcelable[] configuration) {
        this.mConfigurations = (Parcelable[]) Preconditions.checkArrayElementsNotNull(configuration, "configuration");
    }

    public boolean equals(Object o) {
        if (o instanceof UsbDevice) {
            return ((UsbDevice) o).mName.equals(this.mName);
        }
        if (o instanceof String) {
            return ((String) o).equals(this.mName);
        }
        return false;
    }

    public int hashCode() {
        return this.mName.hashCode();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UsbDevice[mName=");
        stringBuilder.append(this.mName);
        stringBuilder.append(",mVendorId=");
        stringBuilder.append(this.mVendorId);
        stringBuilder.append(",mProductId=");
        stringBuilder.append(this.mProductId);
        stringBuilder.append(",mClass=");
        stringBuilder.append(this.mClass);
        stringBuilder.append(",mSubclass=");
        stringBuilder.append(this.mSubclass);
        stringBuilder.append(",mProtocol=");
        stringBuilder.append(this.mProtocol);
        stringBuilder.append(",mManufacturerName=");
        stringBuilder.append(this.mManufacturerName);
        stringBuilder.append(",mProductName=");
        stringBuilder.append(this.mProductName);
        stringBuilder.append(",mVersion=");
        stringBuilder.append(this.mVersion);
        stringBuilder.append(",mSerialNumber=");
        stringBuilder.append(this.mSerialNumber);
        stringBuilder.append(",mSubProductId =");
        stringBuilder.append(this.mSubProductId);
        stringBuilder.append(",mConfigurations=[");
        StringBuilder builder = new StringBuilder(stringBuilder.toString());
        for (Object obj : this.mConfigurations) {
            builder.append("\n");
            builder.append(obj.toString());
        }
        builder.append("]");
        return builder.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mName);
        parcel.writeInt(this.mVendorId);
        parcel.writeInt(this.mProductId);
        parcel.writeInt(this.mClass);
        parcel.writeInt(this.mSubclass);
        parcel.writeInt(this.mProtocol);
        parcel.writeString(this.mManufacturerName);
        parcel.writeString(this.mProductName);
        parcel.writeString(this.mVersion);
        parcel.writeString(this.mSerialNumber);
        parcel.writeParcelableArray(this.mConfigurations, 0);
        parcel.writeInt(this.mSubProductId);
    }

    public static int getDeviceId(String name) {
        return native_get_device_id(name);
    }

    public static String getDeviceName(int id) {
        return native_get_device_name(id);
    }
}
