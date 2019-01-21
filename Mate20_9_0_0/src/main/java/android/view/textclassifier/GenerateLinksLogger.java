package android.view.textclassifier;

import android.metrics.LogMaker;
import android.telephony.PreciseDisconnectCause;
import android.util.ArrayMap;
import android.view.textclassifier.TextLinks.TextLink;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.annotations.VisibleForTesting.Visibility;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.Preconditions;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

@VisibleForTesting(visibility = Visibility.PACKAGE)
public final class GenerateLinksLogger {
    private static final boolean DEBUG_LOG_ENABLED = false;
    private static final String LOG_TAG = "GenerateLinksLogger";
    private static final String ZERO = "0";
    private final MetricsLogger mMetricsLogger;
    private final Random mRng;
    private final int mSampleRate;

    private static final class LinkifyStats {
        int mNumLinks;
        int mNumLinksTextLength;

        private LinkifyStats() {
        }

        void countLink(TextLink link) {
            this.mNumLinks++;
            this.mNumLinksTextLength += link.getEnd() - link.getStart();
        }
    }

    public GenerateLinksLogger(int sampleRate) {
        this.mSampleRate = sampleRate;
        this.mRng = new Random(System.nanoTime());
        this.mMetricsLogger = new MetricsLogger();
    }

    @VisibleForTesting
    public GenerateLinksLogger(int sampleRate, MetricsLogger metricsLogger) {
        this.mSampleRate = sampleRate;
        this.mRng = new Random(System.nanoTime());
        this.mMetricsLogger = metricsLogger;
    }

    public void logGenerateLinks(CharSequence text, TextLinks links, String callingPackageName, long latencyMs) {
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(links);
        Preconditions.checkNotNull(callingPackageName);
        if (shouldLog()) {
            LinkifyStats totalStats = new LinkifyStats();
            Map<String, LinkifyStats> perEntityTypeStats = new ArrayMap();
            for (TextLink link : links.getLinks()) {
                if (link.getEntityCount() != 0) {
                    String entityType = link.getEntity(null);
                    if (!(entityType == null || "other".equals(entityType))) {
                        if (!"".equals(entityType)) {
                            totalStats.countLink(link);
                            ((LinkifyStats) perEntityTypeStats.computeIfAbsent(entityType, -$$Lambda$GenerateLinksLogger$vmbT_h7MLlbrIm0lJJwA-eHQhXk.INSTANCE)).countLink(link);
                        }
                    }
                }
            }
            String callId = UUID.randomUUID().toString();
            writeStats(callId, callingPackageName, null, totalStats, text, latencyMs);
            for (Entry<String, LinkifyStats> entry : perEntityTypeStats.entrySet()) {
                writeStats(callId, callingPackageName, (String) entry.getKey(), (LinkifyStats) entry.getValue(), text, latencyMs);
            }
        }
    }

    private boolean shouldLog() {
        boolean z = true;
        if (this.mSampleRate <= 1) {
            return true;
        }
        if (this.mRng.nextInt(this.mSampleRate) != 0) {
            z = false;
        }
        return z;
    }

    private void writeStats(String callId, String callingPackageName, String entityType, LinkifyStats stats, CharSequence text, long latencyMs) {
        LogMaker log = new LogMaker(PreciseDisconnectCause.SIP_NOT_SUPPORTED).setPackageName(callingPackageName).addTaggedData(PreciseDisconnectCause.SIP_NOT_ACCEPTABLE, callId).addTaggedData(PreciseDisconnectCause.SIP_BAD_ADDRESS, Integer.valueOf(stats.mNumLinks)).addTaggedData(PreciseDisconnectCause.SIP_BUSY, Integer.valueOf(stats.mNumLinksTextLength)).addTaggedData(PreciseDisconnectCause.SIP_TEMPRARILY_UNAVAILABLE, Integer.valueOf(text.length())).addTaggedData(PreciseDisconnectCause.SIP_REQUEST_TIMEOUT, Long.valueOf(latencyMs));
        if (entityType != null) {
            log.addTaggedData(PreciseDisconnectCause.SIP_REQUEST_CANCELLED, entityType);
        }
        this.mMetricsLogger.write(log);
        debugLog(log);
    }

    private static void debugLog(LogMaker log) {
    }
}
