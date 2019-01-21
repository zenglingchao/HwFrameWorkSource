package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class UusInfo {
    public String uusData = new String();
    public int uusDcs;
    public int uusType;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != UusInfo.class) {
            return false;
        }
        UusInfo other = (UusInfo) otherObject;
        if (this.uusType == other.uusType && this.uusDcs == other.uusDcs && HidlSupport.deepEquals(this.uusData, other.uusData)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.uusType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.uusDcs))), Integer.valueOf(HidlSupport.deepHashCode(this.uusData))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".uusType = ");
        builder.append(UusType.toString(this.uusType));
        builder.append(", .uusDcs = ");
        builder.append(UusDcs.toString(this.uusDcs));
        builder.append(", .uusData = ");
        builder.append(this.uusData);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<UusInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<UusInfo> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            UusInfo _hidl_vec_element = new UusInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.uusType = hwBlob.getInt32(_hidl_offset + 0);
        this.uusDcs = hwBlob.getInt32(_hidl_offset + 4);
        this.uusData = hwBlob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.uusData.getBytes().length + 1), _hidl_blob.handle(), (_hidl_offset + 8) + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<UusInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        int _hidl_index_0 = 0;
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        while (_hidl_index_0 < _hidl_vec_size) {
            ((UusInfo) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
            _hidl_index_0++;
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.uusType);
        _hidl_blob.putInt32(4 + _hidl_offset, this.uusDcs);
        _hidl_blob.putString(8 + _hidl_offset, this.uusData);
    }
}
