package com.android.internal.telephony;

import java.util.BitSet;

public class HardwareConfig {
    public static final int DEV_HARDWARE_STATE_DISABLED = 2;
    public static final int DEV_HARDWARE_STATE_ENABLED = 0;
    public static final int DEV_HARDWARE_STATE_STANDBY = 1;
    public static final int DEV_HARDWARE_TYPE_MODEM = 0;
    public static final int DEV_HARDWARE_TYPE_SIM = 1;
    public static final int DEV_MODEM_RIL_MODEL_MULTIPLE = 1;
    public static final int DEV_MODEM_RIL_MODEL_SINGLE = 0;
    static final String LOG_TAG = "HardwareConfig";
    public int maxActiveDataCall;
    public int maxActiveVoiceCall;
    public int maxStandby;
    public String modemUuid;
    public BitSet rat;
    public int rilModel;
    public int state;
    public int type;
    public String uuid;

    public HardwareConfig(int type) {
        this.type = type;
    }

    public HardwareConfig(String res) {
        String[] split = res.split(",");
        this.type = Integer.parseInt(split[0]);
        switch (this.type) {
            case 0:
                assignModem(split[1].trim(), Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]), Integer.parseInt(split[5]), Integer.parseInt(split[6]), Integer.parseInt(split[7]));
                return;
            case 1:
                assignSim(split[1].trim(), Integer.parseInt(split[2]), split[3].trim());
                return;
            default:
                return;
        }
    }

    public void assignModem(String id, int state, int model, int ratBits, int maxV, int maxD, int maxS) {
        if (this.type == 0) {
            char[] bits = Integer.toBinaryString(ratBits).toCharArray();
            this.uuid = id;
            this.state = state;
            this.rilModel = model;
            this.rat = new BitSet(bits.length);
            for (int i = 0; i < bits.length; i++) {
                this.rat.set(i, bits[i] == '1');
            }
            this.maxActiveVoiceCall = maxV;
            this.maxActiveDataCall = maxD;
            this.maxStandby = maxS;
        }
    }

    public void assignSim(String id, int state, String link) {
        if (this.type == 1) {
            this.uuid = id;
            this.modemUuid = link;
            this.state = state;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        StringBuilder stringBuilder;
        if (this.type == 0) {
            builder.append("Modem ");
            stringBuilder = new StringBuilder();
            stringBuilder.append("{ uuid=");
            stringBuilder.append(this.uuid);
            builder.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(", state=");
            stringBuilder.append(this.state);
            builder.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(", rilModel=");
            stringBuilder.append(this.rilModel);
            builder.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(", rat=");
            stringBuilder.append(this.rat.toString());
            builder.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(", maxActiveVoiceCall=");
            stringBuilder.append(this.maxActiveVoiceCall);
            builder.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(", maxActiveDataCall=");
            stringBuilder.append(this.maxActiveDataCall);
            builder.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(", maxStandby=");
            stringBuilder.append(this.maxStandby);
            builder.append(stringBuilder.toString());
            builder.append(" }");
        } else if (this.type == 1) {
            builder.append("Sim ");
            stringBuilder = new StringBuilder();
            stringBuilder.append("{ uuid=");
            stringBuilder.append(this.uuid);
            builder.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(", modemUuid=");
            stringBuilder.append(this.modemUuid);
            builder.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(", state=");
            stringBuilder.append(this.state);
            builder.append(stringBuilder.toString());
            builder.append(" }");
        } else {
            builder.append("Invalid Configration");
        }
        return builder.toString();
    }

    public int compareTo(HardwareConfig hw) {
        return toString().compareTo(hw.toString());
    }
}
