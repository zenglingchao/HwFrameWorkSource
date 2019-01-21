package android.hardware.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

@SystemApi
public final class NanoAppState implements Parcelable {
    public static final Creator<NanoAppState> CREATOR = new Creator<NanoAppState>() {
        public NanoAppState createFromParcel(Parcel in) {
            return new NanoAppState(in, null);
        }

        public NanoAppState[] newArray(int size) {
            return new NanoAppState[size];
        }
    };
    private boolean mIsEnabled;
    private long mNanoAppId;
    private int mNanoAppVersion;

    /* synthetic */ NanoAppState(Parcel x0, AnonymousClass1 x1) {
        this(x0);
    }

    public NanoAppState(long nanoAppId, int appVersion, boolean enabled) {
        this.mNanoAppId = nanoAppId;
        this.mNanoAppVersion = appVersion;
        this.mIsEnabled = enabled;
    }

    public long getNanoAppId() {
        return this.mNanoAppId;
    }

    public long getNanoAppVersion() {
        return (long) this.mNanoAppVersion;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    private NanoAppState(Parcel in) {
        this.mNanoAppId = in.readLong();
        this.mNanoAppVersion = in.readInt();
        boolean z = true;
        if (in.readInt() != 1) {
            z = false;
        }
        this.mIsEnabled = z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mNanoAppId);
        out.writeInt(this.mNanoAppVersion);
        out.writeInt(this.mIsEnabled);
    }
}
