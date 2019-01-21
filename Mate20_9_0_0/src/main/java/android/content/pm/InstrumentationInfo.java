package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseArray;

public class InstrumentationInfo extends PackageItemInfo implements Parcelable {
    public static final Creator<InstrumentationInfo> CREATOR = new Creator<InstrumentationInfo>() {
        public InstrumentationInfo createFromParcel(Parcel source) {
            return new InstrumentationInfo(source, null);
        }

        public InstrumentationInfo[] newArray(int size) {
            return new InstrumentationInfo[size];
        }
    };
    public String credentialProtectedDataDir;
    public String dataDir;
    public String deviceProtectedDataDir;
    public boolean functionalTest;
    public boolean handleProfiling;
    public String nativeLibraryDir;
    public String primaryCpuAbi;
    public String publicSourceDir;
    public String secondaryCpuAbi;
    public String secondaryNativeLibraryDir;
    public String sourceDir;
    public SparseArray<int[]> splitDependencies;
    public String[] splitNames;
    public String[] splitPublicSourceDirs;
    public String[] splitSourceDirs;
    public String targetPackage;
    public String targetProcesses;

    /* synthetic */ InstrumentationInfo(Parcel x0, AnonymousClass1 x1) {
        this(x0);
    }

    public InstrumentationInfo(InstrumentationInfo orig) {
        super((PackageItemInfo) orig);
        this.targetPackage = orig.targetPackage;
        this.targetProcesses = orig.targetProcesses;
        this.sourceDir = orig.sourceDir;
        this.publicSourceDir = orig.publicSourceDir;
        this.splitNames = orig.splitNames;
        this.splitSourceDirs = orig.splitSourceDirs;
        this.splitPublicSourceDirs = orig.splitPublicSourceDirs;
        this.splitDependencies = orig.splitDependencies;
        this.dataDir = orig.dataDir;
        this.deviceProtectedDataDir = orig.deviceProtectedDataDir;
        this.credentialProtectedDataDir = orig.credentialProtectedDataDir;
        this.primaryCpuAbi = orig.primaryCpuAbi;
        this.secondaryCpuAbi = orig.secondaryCpuAbi;
        this.nativeLibraryDir = orig.nativeLibraryDir;
        this.secondaryNativeLibraryDir = orig.secondaryNativeLibraryDir;
        this.handleProfiling = orig.handleProfiling;
        this.functionalTest = orig.functionalTest;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("InstrumentationInfo{");
        stringBuilder.append(Integer.toHexString(System.identityHashCode(this)));
        stringBuilder.append(" ");
        stringBuilder.append(this.packageName);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        super.writeToParcel(dest, parcelableFlags);
        dest.writeString(this.targetPackage);
        dest.writeString(this.targetProcesses);
        dest.writeString(this.sourceDir);
        dest.writeString(this.publicSourceDir);
        dest.writeStringArray(this.splitNames);
        dest.writeStringArray(this.splitSourceDirs);
        dest.writeStringArray(this.splitPublicSourceDirs);
        dest.writeSparseArray(this.splitDependencies);
        dest.writeString(this.dataDir);
        dest.writeString(this.deviceProtectedDataDir);
        dest.writeString(this.credentialProtectedDataDir);
        dest.writeString(this.primaryCpuAbi);
        dest.writeString(this.secondaryCpuAbi);
        dest.writeString(this.nativeLibraryDir);
        dest.writeString(this.secondaryNativeLibraryDir);
        dest.writeInt(this.handleProfiling);
        dest.writeInt(this.functionalTest);
    }

    private InstrumentationInfo(Parcel source) {
        super(source);
        this.targetPackage = source.readString();
        this.targetProcesses = source.readString();
        this.sourceDir = source.readString();
        this.publicSourceDir = source.readString();
        this.splitNames = source.readStringArray();
        this.splitSourceDirs = source.readStringArray();
        this.splitPublicSourceDirs = source.readStringArray();
        this.splitDependencies = source.readSparseArray(null);
        this.dataDir = source.readString();
        this.deviceProtectedDataDir = source.readString();
        this.credentialProtectedDataDir = source.readString();
        this.primaryCpuAbi = source.readString();
        this.secondaryCpuAbi = source.readString();
        this.nativeLibraryDir = source.readString();
        this.secondaryNativeLibraryDir = source.readString();
        boolean z = false;
        this.handleProfiling = source.readInt() != 0;
        if (source.readInt() != 0) {
            z = true;
        }
        this.functionalTest = z;
    }

    public void copyTo(ApplicationInfo ai) {
        ai.packageName = this.packageName;
        ai.sourceDir = this.sourceDir;
        ai.publicSourceDir = this.publicSourceDir;
        ai.splitNames = this.splitNames;
        ai.splitSourceDirs = this.splitSourceDirs;
        ai.splitPublicSourceDirs = this.splitPublicSourceDirs;
        ai.splitDependencies = this.splitDependencies;
        ai.dataDir = this.dataDir;
        ai.deviceProtectedDataDir = this.deviceProtectedDataDir;
        ai.credentialProtectedDataDir = this.credentialProtectedDataDir;
        ai.primaryCpuAbi = this.primaryCpuAbi;
        ai.secondaryCpuAbi = this.secondaryCpuAbi;
        ai.nativeLibraryDir = this.nativeLibraryDir;
        ai.secondaryNativeLibraryDir = this.secondaryNativeLibraryDir;
    }
}
