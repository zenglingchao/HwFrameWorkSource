package android.hardware.location;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ContextHubManager$3$8oeFzBAC_VuH1d32Kod8BVn0Os8 implements Runnable {
    private final /* synthetic */ ContextHubClientCallback f$0;
    private final /* synthetic */ ContextHubClient f$1;
    private final /* synthetic */ long f$2;

    public /* synthetic */ -$$Lambda$ContextHubManager$3$8oeFzBAC_VuH1d32Kod8BVn0Os8(ContextHubClientCallback contextHubClientCallback, ContextHubClient contextHubClient, long j) {
        this.f$0 = contextHubClientCallback;
        this.f$1 = contextHubClient;
        this.f$2 = j;
    }

    public final void run() {
        this.f$0.onNanoAppEnabled(this.f$1, this.f$2);
    }
}
