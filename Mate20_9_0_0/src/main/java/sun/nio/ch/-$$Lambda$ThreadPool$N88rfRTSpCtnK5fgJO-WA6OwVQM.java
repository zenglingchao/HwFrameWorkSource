package sun.nio.ch;

import java.util.concurrent.ThreadFactory;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ThreadPool$N88rfRTSpCtnK5fgJO-WA6OwVQM implements ThreadFactory {
    public static final /* synthetic */ -$$Lambda$ThreadPool$N88rfRTSpCtnK5fgJO-WA6OwVQM INSTANCE = new -$$Lambda$ThreadPool$N88rfRTSpCtnK5fgJO-WA6OwVQM();

    private /* synthetic */ -$$Lambda$ThreadPool$N88rfRTSpCtnK5fgJO-WA6OwVQM() {
    }

    public final Thread newThread(Runnable runnable) {
        return ThreadPool.lambda$defaultThreadFactory$0(runnable);
    }
}
