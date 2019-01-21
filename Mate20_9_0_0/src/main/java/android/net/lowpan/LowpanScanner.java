package android.net.lowpan;

import android.net.lowpan.ILowpanNetScanCallback.Stub;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LowpanScanner {
    private static final String TAG = LowpanScanner.class.getSimpleName();
    private ILowpanInterface mBinder;
    private Callback mCallback = null;
    private ArrayList<Integer> mChannelMask = null;
    private Handler mHandler = null;
    private int mTxPower = Integer.MAX_VALUE;

    public static abstract class Callback {
        public void onNetScanBeacon(LowpanBeaconInfo beacon) {
        }

        public void onEnergyScanResult(LowpanEnergyScanResult result) {
        }

        public void onScanFinished() {
        }
    }

    LowpanScanner(ILowpanInterface binder) {
        this.mBinder = binder;
    }

    public synchronized void setCallback(Callback cb, Handler handler) {
        this.mCallback = cb;
        this.mHandler = handler;
    }

    public void setCallback(Callback cb) {
        setCallback(cb, null);
    }

    public void setChannelMask(Collection<Integer> mask) {
        if (mask == null) {
            this.mChannelMask = null;
            return;
        }
        if (this.mChannelMask == null) {
            this.mChannelMask = new ArrayList();
        } else {
            this.mChannelMask.clear();
        }
        this.mChannelMask.addAll(mask);
    }

    public Collection<Integer> getChannelMask() {
        return (Collection) this.mChannelMask.clone();
    }

    public void addChannel(int channel) {
        if (this.mChannelMask == null) {
            this.mChannelMask = new ArrayList();
        }
        this.mChannelMask.add(Integer.valueOf(channel));
    }

    public void setTxPower(int txPower) {
        this.mTxPower = txPower;
    }

    public int getTxPower() {
        return this.mTxPower;
    }

    private Map<String, Object> createScanOptionMap() {
        Map<String, Object> map = new HashMap();
        if (this.mChannelMask != null) {
            LowpanProperties.KEY_CHANNEL_MASK.putInMap(map, this.mChannelMask.stream().mapToInt(-$$Lambda$LowpanScanner$b0nnjTe02JXonssLsm5Kp4EaFqs.INSTANCE).toArray());
        }
        if (this.mTxPower != Integer.MAX_VALUE) {
            LowpanProperties.KEY_MAX_TX_POWER.putInMap(map, Integer.valueOf(this.mTxPower));
        }
        return map;
    }

    public void startNetScan() throws LowpanException {
        try {
            this.mBinder.startNetScan(createScanOptionMap(), new Stub() {
                public void onNetScanBeacon(LowpanBeaconInfo beaconInfo) {
                    Callback callback;
                    Handler handler;
                    synchronized (LowpanScanner.this) {
                        callback = LowpanScanner.this.mCallback;
                        handler = LowpanScanner.this.mHandler;
                    }
                    if (callback != null) {
                        Runnable runnable = new -$$Lambda$LowpanScanner$1$47buDsybUOrvvSl0JOZR_FC9ISg(callback, beaconInfo);
                        if (handler != null) {
                            handler.post(runnable);
                        } else {
                            runnable.run();
                        }
                    }
                }

                public void onNetScanFinished() {
                    Callback callback;
                    Handler handler;
                    synchronized (LowpanScanner.this) {
                        callback = LowpanScanner.this.mCallback;
                        handler = LowpanScanner.this.mHandler;
                    }
                    if (callback != null) {
                        Runnable runnable = new -$$Lambda$LowpanScanner$1$lUw1npYnRpaO9LS5odGyASQYaic(callback);
                        if (handler != null) {
                            handler.post(runnable);
                        } else {
                            runnable.run();
                        }
                    }
                }
            });
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public void stopNetScan() {
        try {
            this.mBinder.stopNetScan();
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public void startEnergyScan() throws LowpanException {
        try {
            this.mBinder.startEnergyScan(createScanOptionMap(), new ILowpanEnergyScanCallback.Stub() {
                public void onEnergyScanResult(int channel, int rssi) {
                    Callback callback = LowpanScanner.this.mCallback;
                    Handler handler = LowpanScanner.this.mHandler;
                    if (callback != null) {
                        Runnable runnable = new -$$Lambda$LowpanScanner$2$GBDCgjndr24KQueHMX2qGNtfLPg(callback, channel, rssi);
                        if (handler != null) {
                            handler.post(runnable);
                        } else {
                            runnable.run();
                        }
                    }
                }

                static /* synthetic */ void lambda$onEnergyScanResult$0(Callback callback, int channel, int rssi) {
                    if (callback != null) {
                        LowpanEnergyScanResult result = new LowpanEnergyScanResult();
                        result.setChannel(channel);
                        result.setMaxRssi(rssi);
                        callback.onEnergyScanResult(result);
                    }
                }

                public void onEnergyScanFinished() {
                    Callback callback = LowpanScanner.this.mCallback;
                    Handler handler = LowpanScanner.this.mHandler;
                    if (callback != null) {
                        Runnable runnable = new -$$Lambda$LowpanScanner$2$n8MSb22N9MEsazioSumvyQhW3Z4(callback);
                        if (handler != null) {
                            handler.post(runnable);
                        } else {
                            runnable.run();
                        }
                    }
                }
            });
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public void stopEnergyScan() {
        try {
            this.mBinder.stopEnergyScan();
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }
}
