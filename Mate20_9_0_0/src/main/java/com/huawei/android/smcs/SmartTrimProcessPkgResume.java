package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.StringTokenizer;

public final class SmartTrimProcessPkgResume extends SmartTrimProcessEvent {
    public static final Creator<SmartTrimProcessPkgResume> CREATOR = new Creator<SmartTrimProcessPkgResume>() {
        public SmartTrimProcessPkgResume createFromParcel(Parcel source) {
            return new SmartTrimProcessPkgResume(source);
        }

        public SmartTrimProcessPkgResume[] newArray(int size) {
            return new SmartTrimProcessPkgResume[size];
        }
    };
    private static final String TAG = "SmartTrimProcessPkgResume";
    private static final boolean mDebugLocalClass = false;
    public String mPkgName = null;
    public String mProcessName = null;

    SmartTrimProcessPkgResume(Parcel source) {
        super(source);
        readFromParcel(source);
    }

    SmartTrimProcessPkgResume(Parcel source, int event) {
        super(event);
        readFromParcel(source);
    }

    public SmartTrimProcessPkgResume(String sPkg, String processName) {
        super(1);
        this.mPkgName = sPkg;
        this.mProcessName = processName;
    }

    SmartTrimProcessPkgResume(StringTokenizer stzer) {
        super(1);
    }

    public int hashCode() {
        try {
            String sHashCode = new StringBuilder();
            sHashCode.append(this.mProcessName);
            sHashCode.append("_");
            sHashCode.append(this.mPkgName);
            sHashCode = sHashCode.toString();
            if (sHashCode == null || sHashCode.length() <= 0) {
                return -1;
            }
            return sHashCode.hashCode();
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SmartTrimProcessPkgResume.hashCode: catch exception ");
            stringBuilder.append(e.toString());
            Log.e(str, stringBuilder.toString());
            return -1;
        }
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        try {
            if (!(o instanceof SmartTrimProcessPkgResume)) {
                return false;
            }
            SmartTrimProcessPkgResume input = (SmartTrimProcessPkgResume) o;
            if (input.mProcessName.equals(this.mProcessName) && input.mPkgName.equals(this.mPkgName)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SmartTrimProcessPkgResume.equals: catch exception ");
            stringBuilder.append(e.toString());
            Log.e(str, stringBuilder.toString());
            return false;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SmartTrimProcessPkgResume:\n");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("process: ");
        stringBuilder.append(this.mProcessName);
        stringBuilder.append("\n");
        sb.append(stringBuilder.toString());
        stringBuilder = new StringBuilder();
        stringBuilder.append("pkg: ");
        stringBuilder.append(this.mPkgName);
        stringBuilder.append("\n");
        sb.append(stringBuilder.toString());
        return sb.toString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mProcessName);
        dest.writeString(this.mPkgName);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.mProcessName = source.readString();
        this.mPkgName = source.readString();
    }
}
