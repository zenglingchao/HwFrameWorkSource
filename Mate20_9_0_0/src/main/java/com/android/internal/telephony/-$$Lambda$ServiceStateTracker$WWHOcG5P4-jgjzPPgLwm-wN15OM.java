package com.android.internal.telephony;

import android.telephony.PhysicalChannelConfig;
import java.util.function.Function;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ServiceStateTracker$WWHOcG5P4-jgjzPPgLwm-wN15OM implements Function {
    public static final /* synthetic */ -$$Lambda$ServiceStateTracker$WWHOcG5P4-jgjzPPgLwm-wN15OM INSTANCE = new -$$Lambda$ServiceStateTracker$WWHOcG5P4-jgjzPPgLwm-wN15OM();

    private /* synthetic */ -$$Lambda$ServiceStateTracker$WWHOcG5P4-jgjzPPgLwm-wN15OM() {
    }

    public final Object apply(Object obj) {
        return Integer.valueOf(((PhysicalChannelConfig) obj).getCellBandwidthDownlink());
    }
}
