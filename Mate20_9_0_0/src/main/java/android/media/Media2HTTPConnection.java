package android.media;

import android.content.IntentFilter;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.captiveportal.CaptivePortalProbeSpec;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Media2HTTPConnection {
    private static final int CONNECT_TIMEOUT_MS = 30000;
    private static final int HTTP_TEMP_REDIRECT = 307;
    private static final int MAX_REDIRECTS = 20;
    private static final String TAG = "Media2HTTPConnection";
    private static final boolean VERBOSE = false;
    private boolean mAllowCrossDomainRedirect = true;
    private boolean mAllowCrossProtocolRedirect = true;
    private HttpURLConnection mConnection = null;
    private long mCurrentOffset = -1;
    private Map<String, String> mHeaders = null;
    private InputStream mInputStream = null;
    private long mTotalSize = -1;
    private URL mURL = null;

    public Media2HTTPConnection() {
        if (CookieHandler.getDefault() == null) {
            Log.w(TAG, "Media2HTTPConnection: Unexpected. No CookieHandler found.");
        }
    }

    public boolean connect(String uri, String headers) {
        try {
            disconnect();
            this.mAllowCrossDomainRedirect = true;
            this.mURL = new URL(uri);
            this.mHeaders = convertHeaderStringToMap(headers);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private boolean parseBoolean(String val) {
        boolean z = false;
        try {
            if (Long.parseLong(val) != 0) {
                z = true;
            }
            return z;
        } catch (NumberFormatException e) {
            if ("true".equalsIgnoreCase(val) || "yes".equalsIgnoreCase(val)) {
                z = true;
            }
            return z;
        }
    }

    private boolean filterOutInternalHeaders(String key, String val) {
        if (!"android-allow-cross-domain-redirect".equalsIgnoreCase(key)) {
            return false;
        }
        this.mAllowCrossDomainRedirect = parseBoolean(val);
        this.mAllowCrossProtocolRedirect = this.mAllowCrossDomainRedirect;
        return true;
    }

    private Map<String, String> convertHeaderStringToMap(String headers) {
        HashMap<String, String> map = new HashMap();
        for (String pair : headers.split("\r\n")) {
            int colonPos = pair.indexOf(":");
            if (colonPos >= 0) {
                String key = pair.substring(0, colonPos);
                String val = pair.substring(colonPos + 1);
                if (!filterOutInternalHeaders(key, val)) {
                    map.put(key, val);
                }
            }
        }
        return map;
    }

    public void disconnect() {
        teardownConnection();
        this.mHeaders = null;
        this.mURL = null;
    }

    private void teardownConnection() {
        if (this.mConnection != null) {
            if (this.mInputStream != null) {
                try {
                    this.mInputStream.close();
                } catch (IOException e) {
                }
                this.mInputStream = null;
            }
            this.mConnection.disconnect();
            this.mConnection = null;
            this.mCurrentOffset = -1;
        }
    }

    private static final boolean isLocalHost(URL url) {
        if (url == null) {
            return false;
        }
        String host = url.getHost();
        if (host == null) {
            return false;
        }
        try {
            if (host.equalsIgnoreCase(ProxyInfo.LOCAL_HOST) || NetworkUtils.numericToInetAddress(host).isLoopbackAddress()) {
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
        }
    }

    /* JADX WARNING: Missing block: B:25:0x00a1, code skipped:
            if (r1.mAllowCrossDomainRedirect == false) goto L_0x00ab;
     */
    /* JADX WARNING: Missing block: B:26:0x00a3, code skipped:
            r1.mURL = r1.mConnection.getURL();
     */
    /* JADX WARNING: Missing block: B:28:0x00ad, code skipped:
            if (r11 != 206) goto L_0x00d5;
     */
    /* JADX WARNING: Missing block: B:29:0x00af, code skipped:
            r13 = r1.mConnection.getHeaderField("Content-Range");
            r1.mTotalSize = r4;
     */
    /* JADX WARNING: Missing block: B:30:0x00ba, code skipped:
            if (r13 == null) goto L_0x00e2;
     */
    /* JADX WARNING: Missing block: B:31:0x00bc, code skipped:
            r14 = r13.lastIndexOf(47);
     */
    /* JADX WARNING: Missing block: B:32:0x00c3, code skipped:
            if (r14 < 0) goto L_0x00e2;
     */
    /* JADX WARNING: Missing block: B:36:?, code skipped:
            r1.mTotalSize = java.lang.Long.parseLong(r13.substring(r14 + 1));
     */
    /* JADX WARNING: Missing block: B:39:0x00d7, code skipped:
            if (r11 != 200) goto L_0x0100;
     */
    /* JADX WARNING: Missing block: B:41:?, code skipped:
            r1.mTotalSize = (long) r1.mConnection.getContentLength();
     */
    /* JADX WARNING: Missing block: B:51:0x0105, code skipped:
            throw new java.io.IOException();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void seekTo(long offset) throws IOException {
        long j = offset;
        teardownConnection();
        long j2 = -1;
        try {
            int response;
            URL url = this.mURL;
            boolean noProxy = isLocalHost(url);
            int redirectCount = 0;
            while (true) {
                if (noProxy) {
                    this.mConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
                } else {
                    this.mConnection = (HttpURLConnection) url.openConnection();
                }
                this.mConnection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                this.mConnection.setInstanceFollowRedirects(this.mAllowCrossDomainRedirect);
                if (this.mHeaders != null) {
                    for (Entry<String, String> entry : this.mHeaders.entrySet()) {
                        this.mConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
                    }
                }
                if (j > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("bytes=");
                    stringBuilder.append(j);
                    stringBuilder.append("-");
                    this.mConnection.setRequestProperty("Range", stringBuilder.toString());
                }
                response = this.mConnection.getResponseCode();
                if (response != 300 && response != 301 && response != 302 && response != 303 && response != 307) {
                    break;
                }
                redirectCount++;
                if (redirectCount <= 20) {
                    String method = this.mConnection.getRequestMethod();
                    if (response == 307 && !method.equals("GET")) {
                        if (!method.equals("HEAD")) {
                            throw new NoRouteToHostException("Invalid redirect");
                        }
                    }
                    String location = this.mConnection.getHeaderField(CaptivePortalProbeSpec.HTTP_LOCATION_HEADER_NAME);
                    if (location != null) {
                        url = new URL(this.mURL, location);
                        if (!url.getProtocol().equals(IntentFilter.SCHEME_HTTPS)) {
                            if (!url.getProtocol().equals(IntentFilter.SCHEME_HTTP)) {
                                throw new NoRouteToHostException("Unsupported protocol redirect");
                            }
                        }
                        boolean sameProtocol = this.mURL.getProtocol().equals(url.getProtocol());
                        if (!this.mAllowCrossProtocolRedirect) {
                            if (!sameProtocol) {
                                throw new NoRouteToHostException("Cross-protocol redirects are disallowed");
                            }
                        }
                        boolean sameHost = this.mURL.getHost().equals(url.getHost());
                        if (!this.mAllowCrossDomainRedirect) {
                            if (!sameHost) {
                                throw new NoRouteToHostException("Cross-domain redirects are disallowed");
                            }
                        }
                        if (response != 307) {
                            this.mURL = url;
                        }
                        j2 = -1;
                    } else {
                        throw new NoRouteToHostException("Invalid redirect");
                    }
                }
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Too many redirects: ");
                stringBuilder2.append(redirectCount);
                throw new NoRouteToHostException(stringBuilder2.toString());
            }
            if (j > 0) {
                if (response != 206) {
                    throw new ProtocolException();
                }
            }
            this.mInputStream = new BufferedInputStream(this.mConnection.getInputStream());
            this.mCurrentOffset = j;
        } catch (IOException e) {
            this.mTotalSize = -1;
            teardownConnection();
            this.mCurrentOffset = -1;
            throw e;
        }
    }

    public int readAt(long offset, byte[] data, int size) {
        String str;
        StringBuilder stringBuilder;
        StrictMode.setThreadPolicy(new Builder().permitAll().build());
        try {
            if (offset != this.mCurrentOffset) {
                seekTo(offset);
            }
            int n = this.mInputStream.read(data, 0, size);
            if (n == -1) {
                n = 0;
            }
            this.mCurrentOffset += (long) n;
            return n;
        } catch (ProtocolException e) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("readAt ");
            stringBuilder.append(offset);
            stringBuilder.append(" / ");
            stringBuilder.append(size);
            stringBuilder.append(" => ");
            stringBuilder.append(e);
            Log.w(str, stringBuilder.toString());
            return -1010;
        } catch (NoRouteToHostException e2) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("readAt ");
            stringBuilder.append(offset);
            stringBuilder.append(" / ");
            stringBuilder.append(size);
            stringBuilder.append(" => ");
            stringBuilder.append(e2);
            Log.w(str, stringBuilder.toString());
            return -1010;
        } catch (UnknownServiceException e3) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("readAt ");
            stringBuilder.append(offset);
            stringBuilder.append(" / ");
            stringBuilder.append(size);
            stringBuilder.append(" => ");
            stringBuilder.append(e3);
            Log.w(str, stringBuilder.toString());
            return -1010;
        } catch (IOException e4) {
            return -1;
        } catch (Exception e5) {
            return -1;
        }
    }

    public long getSize() {
        if (this.mConnection == null) {
            try {
                seekTo(0);
            } catch (IOException e) {
                return -1;
            }
        }
        return this.mTotalSize;
    }

    public String getMIMEType() {
        if (this.mConnection == null) {
            try {
                seekTo(0);
            } catch (IOException e) {
                return "application/octet-stream";
            }
        }
        return this.mConnection.getContentType();
    }

    public String getUri() {
        return this.mURL.toString();
    }
}
