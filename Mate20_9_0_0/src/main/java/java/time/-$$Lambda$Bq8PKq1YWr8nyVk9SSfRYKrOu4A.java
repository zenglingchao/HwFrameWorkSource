package java.time;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$Bq8PKq1YWr8nyVk9SSfRYKrOu4A implements TemporalQuery {
    public static final /* synthetic */ -$$Lambda$Bq8PKq1YWr8nyVk9SSfRYKrOu4A INSTANCE = new -$$Lambda$Bq8PKq1YWr8nyVk9SSfRYKrOu4A();

    private /* synthetic */ -$$Lambda$Bq8PKq1YWr8nyVk9SSfRYKrOu4A() {
    }

    public final Object queryFrom(TemporalAccessor temporalAccessor) {
        return LocalDate.from(temporalAccessor);
    }
}
