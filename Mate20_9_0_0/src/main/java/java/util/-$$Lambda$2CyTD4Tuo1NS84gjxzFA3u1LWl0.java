package java.util;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$2CyTD4Tuo1NS84gjxzFA3u1LWl0 implements DoubleConsumer {
    private final /* synthetic */ Consumer f$0;

    public /* synthetic */ -$$Lambda$2CyTD4Tuo1NS84gjxzFA3u1LWl0(Consumer consumer) {
        this.f$0 = consumer;
    }

    public final void accept(double d) {
        this.f$0.accept(Double.valueOf(d));
    }
}
