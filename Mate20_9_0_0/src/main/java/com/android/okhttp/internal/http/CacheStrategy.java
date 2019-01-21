package com.android.okhttp.internal.http;

import com.android.okhttp.CacheControl;
import com.android.okhttp.Headers;
import com.android.okhttp.Request;
import com.android.okhttp.Response;
import com.android.okhttp.Response.Builder;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class CacheStrategy {
    public final Response cacheResponse;
    public final Request networkRequest;

    public static class Factory {
        private int ageSeconds = -1;
        final Response cacheResponse;
        private String etag;
        private Date expires;
        private Date lastModified;
        private String lastModifiedString;
        final long nowMillis;
        private long receivedResponseMillis;
        final Request request;
        private long sentRequestMillis;
        private Date servedDate;
        private String servedDateString;

        public Factory(long nowMillis, Request request, Response cacheResponse) {
            this.nowMillis = nowMillis;
            this.request = request;
            this.cacheResponse = cacheResponse;
            if (cacheResponse != null) {
                Headers headers = cacheResponse.headers();
                int size = headers.size();
                for (int i = 0; i < size; i++) {
                    String fieldName = headers.name(i);
                    String value = headers.value(i);
                    if ("Date".equalsIgnoreCase(fieldName)) {
                        this.servedDate = HttpDate.parse(value);
                        this.servedDateString = value;
                    } else if ("Expires".equalsIgnoreCase(fieldName)) {
                        this.expires = HttpDate.parse(value);
                    } else if ("Last-Modified".equalsIgnoreCase(fieldName)) {
                        this.lastModified = HttpDate.parse(value);
                        this.lastModifiedString = value;
                    } else if ("ETag".equalsIgnoreCase(fieldName)) {
                        this.etag = value;
                    } else if ("Age".equalsIgnoreCase(fieldName)) {
                        this.ageSeconds = HeaderParser.parseSeconds(value, -1);
                    } else if (OkHeaders.SENT_MILLIS.equalsIgnoreCase(fieldName)) {
                        this.sentRequestMillis = Long.parseLong(value);
                    } else if (OkHeaders.RECEIVED_MILLIS.equalsIgnoreCase(fieldName)) {
                        this.receivedResponseMillis = Long.parseLong(value);
                    }
                }
            }
        }

        public CacheStrategy get() {
            CacheStrategy candidate = getCandidate();
            if (candidate.networkRequest == null || !this.request.cacheControl().onlyIfCached()) {
                return candidate;
            }
            return new CacheStrategy(null, null);
        }

        private CacheStrategy getCandidate() {
            if (this.cacheResponse == null) {
                return new CacheStrategy(this.request, null);
            }
            if (this.request.isHttps() && this.cacheResponse.handshake() == null) {
                return new CacheStrategy(this.request, null);
            }
            if (!CacheStrategy.isCacheable(this.cacheResponse, this.request)) {
                return new CacheStrategy(this.request, null);
            }
            Response response;
            CacheControl requestCaching = this.request.cacheControl();
            if (requestCaching.noCache()) {
                response = null;
            } else if (hasConditions(this.request)) {
                CacheControl cacheControl = requestCaching;
                response = null;
            } else {
                long ageMillis = cacheResponseAge();
                long freshMillis = computeFreshnessLifetime();
                if (requestCaching.maxAgeSeconds() != -1) {
                    freshMillis = Math.min(freshMillis, TimeUnit.SECONDS.toMillis((long) requestCaching.maxAgeSeconds()));
                }
                long minFreshMillis = 0;
                if (requestCaching.minFreshSeconds() != -1) {
                    minFreshMillis = TimeUnit.SECONDS.toMillis((long) requestCaching.minFreshSeconds());
                }
                long maxStaleMillis = 0;
                CacheControl responseCaching = this.cacheResponse.cacheControl();
                if (!(responseCaching.mustRevalidate() || requestCaching.maxStaleSeconds() == -1)) {
                    maxStaleMillis = TimeUnit.SECONDS.toMillis((long) requestCaching.maxStaleSeconds());
                }
                if (responseCaching.noCache() || ageMillis + minFreshMillis >= freshMillis + maxStaleMillis) {
                    CacheStrategy cacheStrategy;
                    requestCaching = this.request.newBuilder();
                    if (this.etag != null) {
                        requestCaching.header("If-None-Match", this.etag);
                    } else if (this.lastModified != null) {
                        requestCaching.header("If-Modified-Since", this.lastModifiedString);
                    } else if (this.servedDate != null) {
                        requestCaching.header("If-Modified-Since", this.servedDateString);
                    }
                    Request conditionalRequest = requestCaching.build();
                    if (hasConditions(conditionalRequest)) {
                        cacheStrategy = new CacheStrategy(conditionalRequest, this.cacheResponse);
                    } else {
                        cacheStrategy = new CacheStrategy(conditionalRequest, null);
                    }
                    return cacheStrategy;
                }
                Builder builder = this.cacheResponse.newBuilder();
                if (ageMillis + minFreshMillis >= freshMillis) {
                    builder.addHeader("Warning", "110 HttpURLConnection \"Response is stale\"");
                }
                if (ageMillis > 86400000 && isFreshnessLifetimeHeuristic()) {
                    builder.addHeader("Warning", "113 HttpURLConnection \"Heuristic expiration\"");
                }
                return new CacheStrategy(null, builder.build());
            }
            return new CacheStrategy(this.request, response);
        }

        private long computeFreshnessLifetime() {
            CacheControl responseCaching = this.cacheResponse.cacheControl();
            if (responseCaching.maxAgeSeconds() != -1) {
                return TimeUnit.SECONDS.toMillis((long) responseCaching.maxAgeSeconds());
            }
            long j = 0;
            long servedMillis;
            long delta;
            if (this.expires != null) {
                if (this.servedDate != null) {
                    servedMillis = this.servedDate.getTime();
                } else {
                    servedMillis = this.receivedResponseMillis;
                }
                delta = this.expires.getTime() - servedMillis;
                if (delta > 0) {
                    j = delta;
                }
                return j;
            } else if (this.lastModified == null || this.cacheResponse.request().httpUrl().query() != null) {
                return 0;
            } else {
                if (this.servedDate != null) {
                    servedMillis = this.servedDate.getTime();
                } else {
                    servedMillis = this.sentRequestMillis;
                }
                delta = servedMillis - this.lastModified.getTime();
                if (delta > 0) {
                    j = delta / 10;
                }
                return j;
            }
        }

        private long cacheResponseAge() {
            long receivedAge;
            long j = 0;
            if (this.servedDate != null) {
                j = Math.max(0, this.receivedResponseMillis - this.servedDate.getTime());
            }
            long apparentReceivedAge = j;
            if (this.ageSeconds != -1) {
                receivedAge = Math.max(apparentReceivedAge, TimeUnit.SECONDS.toMillis((long) this.ageSeconds));
            } else {
                receivedAge = apparentReceivedAge;
            }
            return (receivedAge + (this.receivedResponseMillis - this.sentRequestMillis)) + (this.nowMillis - this.receivedResponseMillis);
        }

        private boolean isFreshnessLifetimeHeuristic() {
            return this.cacheResponse.cacheControl().maxAgeSeconds() == -1 && this.expires == null;
        }

        private static boolean hasConditions(Request request) {
            return (request.header("If-Modified-Since") == null && request.header("If-None-Match") == null) ? false : true;
        }
    }

    private CacheStrategy(Request networkRequest, Response cacheResponse) {
        this.networkRequest = networkRequest;
        this.cacheResponse = cacheResponse;
    }

    /* JADX WARNING: Missing block: B:9:0x002e, code skipped:
            if (r3.cacheControl().isPrivate() == false) goto L_0x0049;
     */
    /* JADX WARNING: Missing block: B:11:0x003a, code skipped:
            if (r3.cacheControl().noStore() != false) goto L_0x0048;
     */
    /* JADX WARNING: Missing block: B:13:0x0044, code skipped:
            if (r4.cacheControl().noStore() != false) goto L_0x0048;
     */
    /* JADX WARNING: Missing block: B:14:0x0046, code skipped:
            r1 = true;
     */
    /* JADX WARNING: Missing block: B:15:0x0048, code skipped:
            return r1;
     */
    /* JADX WARNING: Missing block: B:16:0x0049, code skipped:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isCacheable(Response response, Request request) {
        boolean z = false;
        switch (response.code()) {
            case 200:
            case 203:
            case 204:
            case 300:
            case 301:
            case StatusLine.HTTP_PERM_REDIRECT /*308*/:
            case 404:
            case 405:
            case 410:
            case 414:
            case 501:
                break;
            case 302:
            case StatusLine.HTTP_TEMP_REDIRECT /*307*/:
                if (response.header("Expires") == null) {
                    if (response.cacheControl().maxAgeSeconds() == -1) {
                        if (!response.cacheControl().isPublic()) {
                            break;
                        }
                    }
                }
                break;
        }
    }
}
