package com.android.server.wm;

import android.content.res.Configuration;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import android.view.SurfaceControl.Builder;
import android.view.SurfaceControl.Transaction;

public abstract class AbsDisplayContent extends WindowContainer<DisplayChildWindowContainer> {
    public /* bridge */ /* synthetic */ void commitPendingTransaction() {
        super.commitPendingTransaction();
    }

    public /* bridge */ /* synthetic */ int compareTo(WindowContainer windowContainer) {
        return super.compareTo(windowContainer);
    }

    public /* bridge */ /* synthetic */ SurfaceControl getAnimationLeashParent() {
        return super.getAnimationLeashParent();
    }

    public /* bridge */ /* synthetic */ SurfaceControl getParentSurfaceControl() {
        return super.getParentSurfaceControl();
    }

    public /* bridge */ /* synthetic */ Transaction getPendingTransaction() {
        return super.getPendingTransaction();
    }

    public /* bridge */ /* synthetic */ SurfaceControl getSurfaceControl() {
        return super.getSurfaceControl();
    }

    public /* bridge */ /* synthetic */ int getSurfaceHeight() {
        return super.getSurfaceHeight();
    }

    public /* bridge */ /* synthetic */ int getSurfaceWidth() {
        return super.getSurfaceWidth();
    }

    public /* bridge */ /* synthetic */ Builder makeAnimationLeash() {
        return super.makeAnimationLeash();
    }

    public /* bridge */ /* synthetic */ void onAnimationLeashCreated(Transaction transaction, SurfaceControl surfaceControl) {
        super.onAnimationLeashCreated(transaction, surfaceControl);
    }

    public /* bridge */ /* synthetic */ void onAnimationLeashDestroyed(Transaction transaction) {
        super.onAnimationLeashDestroyed(transaction);
    }

    public /* bridge */ /* synthetic */ void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public /* bridge */ /* synthetic */ void onOverrideConfigurationChanged(Configuration configuration) {
        super.onOverrideConfigurationChanged(configuration);
    }

    public /* bridge */ /* synthetic */ void writeToProto(ProtoOutputStream protoOutputStream, long j, boolean z) {
        super.writeToProto(protoOutputStream, j, z);
    }

    AbsDisplayContent(WindowManagerService service) {
        super(service);
    }

    public void setDisplayRotationFR(int rotation) {
    }

    protected void uploadOrientation(int rotation) {
    }
}
