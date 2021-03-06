package android.os;

import android.util.Log;

public class HwExHandler extends Handler {
    private static final String TAG = "HwExHandler";
    long mLastStartTime = 0;
    private long runningTimeout = 3000;

    public HwExHandler(Looper looper) {
        super(looper);
    }

    public HwExHandler(Looper looper, long timeout) {
        super(looper);
        this.runningTimeout = timeout;
    }

    public void dispatchMessage(Message msg) {
        this.mLastStartTime = SystemClock.uptimeMillis();
        super.dispatchMessage(msg);
        this.mLastStartTime = 0;
    }

    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        long threadRunningTime = SystemClock.uptimeMillis() - this.mLastStartTime;
        if (this.mLastStartTime != 0 && threadRunningTime > this.runningTimeout && getLooper().getThread().isAlive()) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Thread:");
            stringBuilder.append(getLooper().getThread().getName());
            stringBuilder.append(",threadRunningTime:");
            stringBuilder.append(threadRunningTime);
            Log.e(str, stringBuilder.toString());
            StackTraceElement[] stackTrace = getLooper().getThread().getStackTrace();
            if (stackTrace != null) {
                for (StackTraceElement stack : stackTrace) {
                    Log.e(TAG, stack.toString());
                }
            }
        }
        return super.sendMessageAtTime(msg, uptimeMillis);
    }
}
