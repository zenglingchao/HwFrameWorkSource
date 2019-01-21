package android.accessibilityservice;

import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.util.Preconditions;

public final class AccessibilityButtonController {
    private static final String LOG_TAG = "A11yButtonController";
    private ArrayMap<AccessibilityButtonCallback, Handler> mCallbacks;
    private final Object mLock = new Object();
    private final IAccessibilityServiceConnection mServiceConnection;

    public static abstract class AccessibilityButtonCallback {
        public void onClicked(AccessibilityButtonController controller) {
        }

        public void onAvailabilityChanged(AccessibilityButtonController controller, boolean available) {
        }
    }

    AccessibilityButtonController(IAccessibilityServiceConnection serviceConnection) {
        this.mServiceConnection = serviceConnection;
    }

    public boolean isAccessibilityButtonAvailable() {
        try {
            return this.mServiceConnection.isAccessibilityButtonAvailable();
        } catch (RemoteException re) {
            Slog.w(LOG_TAG, "Failed to get accessibility button availability.", re);
            re.rethrowFromSystemServer();
            return false;
        }
    }

    public void registerAccessibilityButtonCallback(AccessibilityButtonCallback callback) {
        registerAccessibilityButtonCallback(callback, new Handler(Looper.getMainLooper()));
    }

    public void registerAccessibilityButtonCallback(AccessibilityButtonCallback callback, Handler handler) {
        Preconditions.checkNotNull(callback);
        Preconditions.checkNotNull(handler);
        synchronized (this.mLock) {
            if (this.mCallbacks == null) {
                this.mCallbacks = new ArrayMap();
            }
            this.mCallbacks.put(callback, handler);
        }
    }

    /* JADX WARNING: Missing block: B:14:0x001f, code skipped:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void unregisterAccessibilityButtonCallback(AccessibilityButtonCallback callback) {
        Preconditions.checkNotNull(callback);
        synchronized (this.mLock) {
            if (this.mCallbacks == null) {
                return;
            }
            int keyIndex = this.mCallbacks.indexOfKey(callback);
            if (keyIndex >= 0) {
                this.mCallbacks.removeAt(keyIndex);
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0018, code skipped:
            r0 = 0;
            r2 = r1.size();
     */
    /* JADX WARNING: Missing block: B:11:0x001d, code skipped:
            if (r0 >= r2) goto L_0x0036;
     */
    /* JADX WARNING: Missing block: B:12:0x001f, code skipped:
            ((android.os.Handler) r1.valueAt(r0)).post(new android.accessibilityservice.-$$Lambda$AccessibilityButtonController$b_UAM9QJWcH4KQOC_odiN0t_boU(r6, (android.accessibilityservice.AccessibilityButtonController.AccessibilityButtonCallback) r1.keyAt(r0)));
            r0 = r0 + 1;
     */
    /* JADX WARNING: Missing block: B:13:0x0036, code skipped:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void dispatchAccessibilityButtonClicked() {
        synchronized (this.mLock) {
            if (this.mCallbacks != null) {
                if (!this.mCallbacks.isEmpty()) {
                    ArrayMap<AccessibilityButtonCallback, Handler> entries = new ArrayMap(this.mCallbacks);
                }
            }
            Slog.w(LOG_TAG, "Received accessibility button click with no callbacks!");
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0018, code skipped:
            r0 = 0;
            r2 = r1.size();
     */
    /* JADX WARNING: Missing block: B:11:0x001d, code skipped:
            if (r0 >= r2) goto L_0x0036;
     */
    /* JADX WARNING: Missing block: B:12:0x001f, code skipped:
            ((android.os.Handler) r1.valueAt(r0)).post(new android.accessibilityservice.-$$Lambda$AccessibilityButtonController$RskKrfcSyUz7I9Sqaziy1P990ZM(r6, (android.accessibilityservice.AccessibilityButtonController.AccessibilityButtonCallback) r1.keyAt(r0), r7));
            r0 = r0 + 1;
     */
    /* JADX WARNING: Missing block: B:13:0x0036, code skipped:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void dispatchAccessibilityButtonAvailabilityChanged(boolean available) {
        synchronized (this.mLock) {
            if (this.mCallbacks != null) {
                if (!this.mCallbacks.isEmpty()) {
                    ArrayMap<AccessibilityButtonCallback, Handler> entries = new ArrayMap(this.mCallbacks);
                }
            }
            Slog.w(LOG_TAG, "Received accessibility button availability change with no callbacks!");
        }
    }
}
