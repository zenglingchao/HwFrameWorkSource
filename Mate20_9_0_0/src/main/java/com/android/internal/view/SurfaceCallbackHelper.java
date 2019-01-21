package com.android.internal.view;

import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceHolder.Callback2;

public class SurfaceCallbackHelper {
    int mFinishDrawingCollected = 0;
    int mFinishDrawingExpected = 0;
    private Runnable mFinishDrawingRunnable = new Runnable() {
        public void run() {
            synchronized (SurfaceCallbackHelper.this) {
                SurfaceCallbackHelper surfaceCallbackHelper = SurfaceCallbackHelper.this;
                surfaceCallbackHelper.mFinishDrawingCollected++;
                if (SurfaceCallbackHelper.this.mFinishDrawingCollected < SurfaceCallbackHelper.this.mFinishDrawingExpected) {
                    return;
                }
                SurfaceCallbackHelper.this.mRunnable.run();
            }
        }
    };
    Runnable mRunnable;

    public SurfaceCallbackHelper(Runnable callbacksCollected) {
        this.mRunnable = callbacksCollected;
    }

    public void dispatchSurfaceRedrawNeededAsync(SurfaceHolder holder, Callback[] callbacks) {
        if (callbacks == null || callbacks.length == 0) {
            this.mRunnable.run();
            return;
        }
        int i;
        synchronized (this) {
            this.mFinishDrawingExpected = callbacks.length;
            i = 0;
            this.mFinishDrawingCollected = 0;
        }
        int length = callbacks.length;
        while (i < length) {
            Callback c = callbacks[i];
            if (c instanceof Callback2) {
                ((Callback2) c).surfaceRedrawNeededAsync(holder, this.mFinishDrawingRunnable);
            } else {
                this.mFinishDrawingRunnable.run();
            }
            i++;
        }
    }
}
