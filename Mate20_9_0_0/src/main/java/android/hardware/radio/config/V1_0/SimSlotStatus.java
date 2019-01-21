package android.hardware.radio.config.V1_0;

import android.hardware.radio.V1_0.CardState;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class SimSlotStatus {
    public String atr = new String();
    public int cardState;
    public String iccid = new String();
    public int logicalSlotId;
    public int slotState;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != SimSlotStatus.class) {
            return false;
        }
        SimSlotStatus other = (SimSlotStatus) otherObject;
        if (this.cardState == other.cardState && this.slotState == other.slotState && HidlSupport.deepEquals(this.atr, other.atr) && this.logicalSlotId == other.logicalSlotId && HidlSupport.deepEquals(this.iccid, other.iccid)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cardState))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.slotState))), Integer.valueOf(HidlSupport.deepHashCode(this.atr)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.logicalSlotId))), Integer.valueOf(HidlSupport.deepHashCode(this.iccid))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".cardState = ");
        builder.append(CardState.toString(this.cardState));
        builder.append(", .slotState = ");
        builder.append(SlotState.toString(this.slotState));
        builder.append(", .atr = ");
        builder.append(this.atr);
        builder.append(", .logicalSlotId = ");
        builder.append(this.logicalSlotId);
        builder.append(", .iccid = ");
        builder.append(this.iccid);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<SimSlotStatus> readVectorFromParcel(HwParcel parcel) {
        ArrayList<SimSlotStatus> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            SimSlotStatus _hidl_vec_element = new SimSlotStatus();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.cardState = hwBlob.getInt32(_hidl_offset + 0);
        this.slotState = hwBlob.getInt32(_hidl_offset + 4);
        this.atr = hwBlob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.atr.getBytes().length + 1), _hidl_blob.handle(), (_hidl_offset + 8) + 0, false);
        this.logicalSlotId = hwBlob.getInt32(_hidl_offset + 24);
        this.iccid = hwBlob.getString(_hidl_offset + 32);
        parcel.readEmbeddedBuffer((long) (this.iccid.getBytes().length + 1), _hidl_blob.handle(), (_hidl_offset + 32) + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<SimSlotStatus> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        int _hidl_index_0 = 0;
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        while (_hidl_index_0 < _hidl_vec_size) {
            ((SimSlotStatus) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
            _hidl_index_0++;
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.cardState);
        _hidl_blob.putInt32(4 + _hidl_offset, this.slotState);
        _hidl_blob.putString(8 + _hidl_offset, this.atr);
        _hidl_blob.putInt32(24 + _hidl_offset, this.logicalSlotId);
        _hidl_blob.putString(32 + _hidl_offset, this.iccid);
    }
}
