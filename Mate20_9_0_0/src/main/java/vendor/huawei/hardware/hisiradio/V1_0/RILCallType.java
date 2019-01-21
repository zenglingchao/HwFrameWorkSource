package vendor.huawei.hardware.hisiradio.V1_0;

import java.util.ArrayList;

public final class RILCallType {
    public static final int RIL_CALL_TYPE_VOICE = 0;
    public static final int RIL_CALL_TYPE_VS_RX = 2;
    public static final int RIL_CALL_TYPE_VS_TX = 1;
    public static final int RIL_CALL_TYPE_VT = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "RIL_CALL_TYPE_VOICE";
        }
        if (o == 1) {
            return "RIL_CALL_TYPE_VS_TX";
        }
        if (o == 2) {
            return "RIL_CALL_TYPE_VS_RX";
        }
        if (o == 3) {
            return "RIL_CALL_TYPE_VT";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("0x");
        stringBuilder.append(Integer.toHexString(o));
        return stringBuilder.toString();
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("RIL_CALL_TYPE_VOICE");
        if ((o & 1) == 1) {
            list.add("RIL_CALL_TYPE_VS_TX");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("RIL_CALL_TYPE_VS_RX");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("RIL_CALL_TYPE_VT");
            flipped |= 3;
        }
        if (o != flipped) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("0x");
            stringBuilder.append(Integer.toHexString((~flipped) & o));
            list.add(stringBuilder.toString());
        }
        return String.join(" | ", list);
    }
}
