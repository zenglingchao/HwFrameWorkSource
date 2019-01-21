package android.media;

import android.app.ActivityThread;
import android.content.Context;
import android.media.IPlayer.Stub;
import android.media.VolumeShaper.Configuration;
import android.media.VolumeShaper.Operation;
import android.media.VolumeShaper.State;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsService;
import java.lang.ref.WeakReference;
import java.util.Objects;

public abstract class PlayerBase {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_APP_OPS = false;
    private static final String TAG = "PlayerBase";
    private static IAudioService sService;
    boolean isReleased;
    private IAppOpsService mAppOps;
    private IAppOpsCallback mAppOpsCallback;
    protected AudioAttributes mAttributes;
    protected float mAuxEffectSendLevel = 0.0f;
    @GuardedBy("mLock")
    private boolean mHasAppOpsPlayAudio = true;
    private final int mImplType;
    protected float mLeftVolume = 1.0f;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private float mPanMultiplierL = 1.0f;
    @GuardedBy("mLock")
    private float mPanMultiplierR = 1.0f;
    private int mPlayerIId = 0;
    protected float mRightVolume = 1.0f;
    @GuardedBy("mLock")
    private int mStartDelayMs = 0;
    @GuardedBy("mLock")
    private int mState;

    public static class PlayerIdCard implements Parcelable {
        public static final int AUDIO_ATTRIBUTES_DEFINED = 1;
        public static final int AUDIO_ATTRIBUTES_NONE = 0;
        public static final Creator<PlayerIdCard> CREATOR = new Creator<PlayerIdCard>() {
            public PlayerIdCard createFromParcel(Parcel p) {
                return new PlayerIdCard(p);
            }

            public PlayerIdCard[] newArray(int size) {
                return new PlayerIdCard[size];
            }
        };
        public final AudioAttributes mAttributes;
        public final IPlayer mIPlayer;
        private String mPkgName;
        public final int mPlayerType;

        public String getPkgName() {
            return this.mPkgName;
        }

        public void setPkgName(String pkgName) {
            this.mPkgName = pkgName;
        }

        PlayerIdCard(int type, AudioAttributes attr, IPlayer iplayer) {
            this.mPlayerType = type;
            this.mAttributes = attr;
            this.mIPlayer = iplayer;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(this.mPlayerType)});
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mPlayerType);
            this.mAttributes.writeToParcel(dest, 0);
            dest.writeStrongBinder(this.mIPlayer == null ? null : this.mIPlayer.asBinder());
        }

        private PlayerIdCard(Parcel in) {
            this.mPlayerType = in.readInt();
            this.mAttributes = (AudioAttributes) AudioAttributes.CREATOR.createFromParcel(in);
            IBinder b = in.readStrongBinder();
            this.mIPlayer = b == null ? null : Stub.asInterface(b);
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof PlayerIdCard)) {
                return false;
            }
            PlayerIdCard that = (PlayerIdCard) o;
            if (!(this.mPlayerType == that.mPlayerType && this.mAttributes.equals(that.mAttributes))) {
                z = false;
            }
            return z;
        }
    }

    private static class IAppOpsCallbackWrapper extends IAppOpsCallback.Stub {
        private final WeakReference<PlayerBase> mWeakPB;

        public IAppOpsCallbackWrapper(PlayerBase pb) {
            this.mWeakPB = new WeakReference(pb);
        }

        public void opChanged(int op, int uid, String packageName) {
            if (op == 28) {
                PlayerBase pb = (PlayerBase) this.mWeakPB.get();
                if (pb != null) {
                    pb.updateAppOpsPlayAudio();
                }
            }
        }
    }

    private static class IPlayerWrapper extends Stub {
        private final WeakReference<PlayerBase> mWeakPB;

        public IPlayerWrapper(PlayerBase pb) {
            this.mWeakPB = new WeakReference(pb);
        }

        public void start() {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.playerStart();
            }
        }

        public void pause() {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.playerPause();
            }
        }

        public void stop() {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.playerStop();
            }
        }

        public void setVolume(float vol) {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.baseSetVolume(vol, vol);
            }
        }

        public void setPan(float pan) {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.baseSetPan(pan);
            }
        }

        public void setStartDelayMs(int delayMs) {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.baseSetStartDelayMs(delayMs);
            }
        }

        public void applyVolumeShaper(Configuration configuration, Operation operation) {
            PlayerBase pb = (PlayerBase) this.mWeakPB.get();
            if (pb != null) {
                pb.playerApplyVolumeShaper(configuration, operation);
            }
        }
    }

    abstract int playerApplyVolumeShaper(Configuration configuration, Operation operation);

    abstract State playerGetVolumeShaperState(int i);

    abstract void playerPause();

    abstract int playerSetAuxEffectSendLevel(boolean z, float f);

    abstract void playerSetVolume(boolean z, float f, float f2);

    abstract void playerStart();

    abstract void playerStop();

    PlayerBase(AudioAttributes attr, int implType) {
        if (attr != null) {
            this.mAttributes = attr;
            this.mImplType = implType;
            this.mState = 1;
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioAttributes");
    }

    protected void baseRegisterPlayer() {
        int newPiid = -1;
        this.mAppOps = IAppOpsService.Stub.asInterface(ServiceManager.getService(Context.APP_OPS_SERVICE));
        updateAppOpsPlayAudio();
        this.mAppOpsCallback = new IAppOpsCallbackWrapper(this);
        try {
            this.mAppOps.startWatchingMode(28, ActivityThread.currentPackageName(), this.mAppOpsCallback);
        } catch (RemoteException e) {
            Log.e(TAG, "Error registering appOps callback", e);
            this.mHasAppOpsPlayAudio = false;
        }
        try {
            newPiid = getService().trackPlayer(new PlayerIdCard(this.mImplType, this.mAttributes, new IPlayerWrapper(this)));
        } catch (RemoteException e2) {
            Log.e(TAG, "Error talking to audio service, player will not be tracked", e2);
        }
        this.mPlayerIId = newPiid;
    }

    void baseUpdateAudioAttributes(AudioAttributes attr) {
        if (attr != null) {
            try {
                getService().playerAttributes(this.mPlayerIId, attr);
            } catch (RemoteException e) {
                Log.e(TAG, "Error talking to audio service, STARTED state will not be tracked", e);
            }
            synchronized (this.mLock) {
                boolean attributesChanged = this.mAttributes != attr;
                this.mAttributes = attr;
                updateAppOpsPlayAudio_sync(attributesChanged);
            }
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioAttributes");
    }

    private void updateState(int state) {
        int piid;
        synchronized (this.mLock) {
            this.mState = state;
            piid = this.mPlayerIId;
        }
        try {
            getService().playerEvent(piid, state);
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error talking to audio service, ");
            stringBuilder.append(AudioPlaybackConfiguration.toLogFriendlyPlayerState(state));
            stringBuilder.append(" state will not be tracked for piid=");
            stringBuilder.append(piid);
            Log.e(str, stringBuilder.toString(), e);
        }
    }

    void baseStart() {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("baseStart() piid=");
        stringBuilder.append(this.mPlayerIId);
        Log.v(str, stringBuilder.toString());
        updateState(2);
        synchronized (this.mLock) {
            if (isRestricted_sync()) {
                playerSetVolume(true, 0.0f, 0.0f);
            }
        }
    }

    void baseSetStartDelayMs(int delayMs) {
        synchronized (this.mLock) {
            this.mStartDelayMs = Math.max(delayMs, 0);
        }
    }

    protected int getStartDelayMs() {
        int i;
        synchronized (this.mLock) {
            i = this.mStartDelayMs;
        }
        return i;
    }

    void basePause() {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("basePause() piid=");
        stringBuilder.append(this.mPlayerIId);
        Log.v(str, stringBuilder.toString());
        updateState(3);
    }

    void baseStop() {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("baseStop() piid=");
        stringBuilder.append(this.mPlayerIId);
        Log.v(str, stringBuilder.toString());
        updateState(4);
    }

    void baseSetPan(float pan) {
        float p = Math.min(Math.max(-1.0f, pan), 1.0f);
        synchronized (this.mLock) {
            if (p >= 0.0f) {
                try {
                    this.mPanMultiplierL = 1.0f - p;
                    this.mPanMultiplierR = 1.0f;
                } catch (Throwable th) {
                    while (true) {
                    }
                }
            } else {
                this.mPanMultiplierL = 1.0f;
                this.mPanMultiplierR = 1.0f + p;
            }
        }
        baseSetVolume(this.mLeftVolume, this.mRightVolume);
    }

    void baseSetVolume(float leftVolume, float rightVolume) {
        boolean isRestricted;
        synchronized (this.mLock) {
            this.mLeftVolume = leftVolume;
            this.mRightVolume = rightVolume;
            isRestricted = isRestricted_sync();
        }
        playerSetVolume(isRestricted, this.mPanMultiplierL * leftVolume, this.mPanMultiplierR * rightVolume);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("isRestricted: ");
        stringBuilder.append(isRestricted);
        stringBuilder.append("Leftvolume: ");
        stringBuilder.append(leftVolume);
        stringBuilder.append(" Rightvolume: ");
        stringBuilder.append(rightVolume);
        stringBuilder.append("mPanMultiplierL: ");
        stringBuilder.append(this.mPanMultiplierL);
        stringBuilder.append("mPanMultiplierR: ");
        stringBuilder.append(this.mPanMultiplierR);
        Log.i(str, stringBuilder.toString());
    }

    int baseSetAuxEffectSendLevel(float level) {
        synchronized (this.mLock) {
            this.mAuxEffectSendLevel = level;
            if (isRestricted_sync()) {
                return 0;
            }
            return playerSetAuxEffectSendLevel(false, level);
        }
    }

    void baseRelease() {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("baseRelease() piid=");
        stringBuilder.append(this.mPlayerIId);
        stringBuilder.append(" state=");
        stringBuilder.append(this.mState);
        Log.v(str, stringBuilder.toString());
        boolean releasePlayer = false;
        synchronized (this.mLock) {
            if (this.mState != 0) {
                releasePlayer = true;
                this.mState = 0;
            }
        }
        if (releasePlayer) {
            try {
                getService().releasePlayer(this.mPlayerIId);
            } catch (RemoteException e) {
                Log.e(TAG, "Error talking to audio service, the player will still be tracked", e);
            }
        }
        try {
            if (this.mAppOps != null) {
                this.mAppOps.stopWatchingMode(this.mAppOpsCallback);
                this.isReleased = true;
            }
        } catch (Exception e2) {
        }
    }

    private void updateAppOpsPlayAudio() {
        synchronized (this.mLock) {
            updateAppOpsPlayAudio_sync(false);
        }
    }

    void updateAppOpsPlayAudio_sync(boolean attributesChanged) {
        boolean oldHasAppOpsPlayAudio = this.mHasAppOpsPlayAudio;
        int mode = 1;
        try {
            if (this.mAppOps != null) {
                mode = this.mAppOps.checkAudioOperation(28, this.mAttributes.getUsage(), Process.myUid(), ActivityThread.currentPackageName());
            }
            this.mHasAppOpsPlayAudio = mode == 0;
        } catch (RemoteException e) {
            this.mHasAppOpsPlayAudio = false;
        }
        try {
            if (oldHasAppOpsPlayAudio != this.mHasAppOpsPlayAudio || attributesChanged) {
                getService().playerHasOpPlayAudio(this.mPlayerIId, this.mHasAppOpsPlayAudio);
                if (isRestricted_sync()) {
                    playerSetVolume(true, 0.0f, 0.0f);
                    playerSetAuxEffectSendLevel(true, 0.0f);
                    return;
                }
                playerSetVolume(false, this.mLeftVolume * this.mPanMultiplierL, this.mRightVolume * this.mPanMultiplierR);
                playerSetAuxEffectSendLevel(false, this.mAuxEffectSendLevel);
            }
        } catch (Exception e2) {
        }
    }

    boolean isRestricted_sync() {
        IAudioService service = getService();
        if (service != null) {
            try {
                if (this.mAttributes != null && service.getRingerModeExternal() == 0) {
                    int stream = AudioAttributes.toLegacyStreamType(this.mAttributes);
                    if (stream == 2 || stream == 5) {
                        Log.i(TAG, "mute stream under RINGER_MODE_SILENT");
                        return true;
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot access AudioService getRingerModeExternal in isRestricted_sync()");
            }
        }
        if (this.mHasAppOpsPlayAudio || (this.mAttributes.getAllFlags() & 64) != 0) {
            return false;
        }
        if ((this.mAttributes.getAllFlags() & 1) != 0 && this.mAttributes.getUsage() == 13) {
            boolean cameraSoundForced = false;
            try {
                cameraSoundForced = getService().isCameraSoundForced();
            } catch (RemoteException e2) {
                Log.e(TAG, "Cannot access AudioService in isRestricted_sync()");
            } catch (NullPointerException e3) {
                Log.e(TAG, "Null AudioService in isRestricted_sync()");
            }
            if (cameraSoundForced) {
                return false;
            }
        }
        return true;
    }

    private static IAudioService getService() {
        if (sService != null) {
            return sService;
        }
        sService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        return sService;
    }

    public void setStartDelayMs(int delayMs) {
        baseSetStartDelayMs(delayMs);
    }

    public static void deprecateStreamTypeForPlayback(int streamType, String className, String opName) throws IllegalArgumentException {
        if (streamType == 10) {
            throw new IllegalArgumentException("Use of STREAM_ACCESSIBILITY is reserved for volume control");
        }
    }
}
