package android.hardware.fingerprint;

import android.hardware.fingerprint.FingerprintManager.AnonymousClass2;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$FingerprintManager$2$iiSGvjInjtzVqJ-wXw-4RQIjKDs implements Runnable {
    private final /* synthetic */ AnonymousClass2 f$0;
    private final /* synthetic */ long f$1;
    private final /* synthetic */ int f$2;
    private final /* synthetic */ int f$3;

    public /* synthetic */ -$$Lambda$FingerprintManager$2$iiSGvjInjtzVqJ-wXw-4RQIjKDs(AnonymousClass2 anonymousClass2, long j, int i, int i2) {
        this.f$0 = anonymousClass2;
        this.f$1 = j;
        this.f$2 = i;
        this.f$3 = i2;
    }

    public final void run() {
        FingerprintManager.this.sendErrorResult(this.f$1, this.f$2, this.f$3);
    }
}
