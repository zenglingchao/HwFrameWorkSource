package android.net.http;

import android.content.Context;
import android.os.SystemClock;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

abstract class Connection {
    private static final int DONE = 3;
    private static final int DRAIN = 2;
    private static final String HTTP_CONNECTION = "http.connection";
    private static final int MAX_PIPE = 3;
    private static final int MIN_PIPE = 2;
    private static final int READ = 1;
    private static final int RETRY_REQUEST_LIMIT = 2;
    private static final int SEND = 0;
    static final int SOCKET_TIMEOUT = 60000;
    private static int STATE_CANCEL_REQUESTED = 1;
    private static int STATE_NORMAL = 0;
    private static final String[] states = new String[]{"SEND", "READ", "DRAIN", "DONE"};
    private int mActive = STATE_NORMAL;
    private byte[] mBuf;
    private boolean mCanPersist;
    protected SslCertificate mCertificate = null;
    Context mContext;
    HttpHost mHost;
    protected AndroidHttpClientConnection mHttpClientConnection = null;
    private HttpContext mHttpContext;
    RequestFeeder mRequestFeeder;

    abstract void closeConnection();

    abstract String getScheme();

    abstract AndroidHttpClientConnection openConnection(Request request) throws IOException;

    protected Connection(Context context, HttpHost host, RequestFeeder requestFeeder) {
        this.mContext = context;
        this.mHost = host;
        this.mRequestFeeder = requestFeeder;
        this.mCanPersist = false;
        this.mHttpContext = new BasicHttpContext(null);
    }

    HttpHost getHost() {
        return this.mHost;
    }

    static Connection getConnection(Context context, HttpHost host, HttpHost proxy, RequestFeeder requestFeeder) {
        if (host.getSchemeName().equals(HttpHost.DEFAULT_SCHEME_NAME)) {
            return new HttpConnection(context, host, requestFeeder);
        }
        return new HttpsConnection(context, host, proxy, requestFeeder);
    }

    SslCertificate getCertificate() {
        return this.mCertificate;
    }

    void cancel() {
        this.mActive = STATE_CANCEL_REQUESTED;
        closeConnection();
    }

    void processRequests(Request firstRequest) {
        Exception exception = null;
        LinkedList<Request> pipe = new LinkedList();
        int maxPipe = 3;
        int minPipe = 2;
        int error = 0;
        Request req = null;
        Request firstRequest2 = firstRequest;
        int state = 0;
        while (true) {
            boolean z = true;
            if (state != 3) {
                if (this.mActive == STATE_CANCEL_REQUESTED) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    this.mActive = STATE_NORMAL;
                }
                switch (state) {
                    case 0:
                        if (pipe.size() != maxPipe) {
                            if (firstRequest2 == null) {
                                req = this.mRequestFeeder.getRequest(this.mHost);
                            } else {
                                req = firstRequest2;
                                firstRequest2 = null;
                            }
                            if (req != null) {
                                req.setConnection(this);
                                if (!req.mCancelled) {
                                    if ((this.mHttpClientConnection != null && this.mHttpClientConnection.isOpen()) || openHttpConnection(req)) {
                                        req.mEventHandler.certificate(this.mCertificate);
                                        try {
                                            req.sendRequest(this.mHttpClientConnection);
                                        } catch (HttpException e2) {
                                            exception = e2;
                                            error = -1;
                                        } catch (IOException e22) {
                                            exception = e22;
                                            error = -7;
                                        } catch (IllegalStateException e222) {
                                            exception = e222;
                                            error = -7;
                                        }
                                        if (exception == null) {
                                            pipe.addLast(req);
                                            if (!this.mCanPersist) {
                                                state = 1;
                                                break;
                                            }
                                            break;
                                        }
                                        if (httpFailure(req, error, exception) && !req.mCancelled) {
                                            pipe.addLast(req);
                                        }
                                        exception = null;
                                        if (!clearPipe(pipe)) {
                                            z = false;
                                        }
                                        state = z;
                                        maxPipe = 1;
                                        minPipe = 1;
                                        break;
                                    }
                                    state = 3;
                                    break;
                                }
                                req.complete();
                                break;
                            }
                            state = 2;
                            break;
                        }
                        state = 1;
                        break;
                    case 1:
                    case 2:
                        boolean empty = this.mRequestFeeder.haveRequest(this.mHost) ^ true;
                        int pipeSize = pipe.size();
                        if (state == 2 || pipeSize >= minPipe || empty || !this.mCanPersist) {
                            if (pipeSize != 0) {
                                req = (Request) pipe.removeFirst();
                                try {
                                    req.readResponse(this.mHttpClientConnection);
                                } catch (ParseException e3) {
                                    exception = e3;
                                    error = -7;
                                } catch (IOException e32) {
                                    exception = e32;
                                    error = -7;
                                } catch (IllegalStateException e322) {
                                    exception = e322;
                                    error = -7;
                                }
                                if (exception != null) {
                                    if (httpFailure(req, error, exception) && !req.mCancelled) {
                                        req.reset();
                                        pipe.addFirst(req);
                                    }
                                    exception = null;
                                    this.mCanPersist = false;
                                }
                                if (!this.mCanPersist) {
                                    closeConnection();
                                    this.mHttpContext.removeAttribute("http.connection");
                                    clearPipe(pipe);
                                    maxPipe = 1;
                                    minPipe = 1;
                                    state = 0;
                                    break;
                                }
                                break;
                            }
                            if (!empty) {
                                z = false;
                            }
                            state = z;
                            break;
                        }
                        state = 0;
                        break;
                    default:
                        break;
                }
            }
            return;
        }
    }

    private boolean clearPipe(LinkedList<Request> pipe) {
        boolean empty = true;
        synchronized (this.mRequestFeeder) {
            while (!pipe.isEmpty()) {
                this.mRequestFeeder.requeueRequest((Request) pipe.removeLast());
                empty = false;
            }
            if (empty) {
                empty = this.mRequestFeeder.haveRequest(this.mHost) ^ 1;
            }
        }
        return empty;
    }

    private boolean openHttpConnection(Request req) {
        long now = SystemClock.uptimeMillis();
        int error = 0;
        Exception exception = null;
        try {
            this.mCertificate = null;
            this.mHttpClientConnection = openConnection(req);
            if (this.mHttpClientConnection != null) {
                this.mHttpClientConnection.setSocketTimeout(SOCKET_TIMEOUT);
                this.mHttpContext.setAttribute("http.connection", this.mHttpClientConnection);
                boolean z = true;
                if (error == 0) {
                    return true;
                }
                if (req.mFailCount < 2) {
                    this.mRequestFeeder.requeueRequest(req);
                    req.mFailCount++;
                } else {
                    httpFailure(req, error, exception);
                }
                if (error != 0) {
                    z = false;
                }
                return z;
            }
            req.mFailCount = 2;
            return false;
        } catch (UnknownHostException e) {
            error = -2;
            exception = e;
        } catch (IllegalArgumentException e2) {
            error = -6;
            req.mFailCount = 2;
            exception = e2;
        } catch (SSLConnectionClosedByUserException e3) {
            req.mFailCount = 2;
            return false;
        } catch (SSLHandshakeException e22) {
            req.mFailCount = 2;
            error = -11;
            exception = e22;
        } catch (IOException e222) {
            error = -6;
            exception = e222;
        }
    }

    private boolean httpFailure(Request req, int errorId, Exception e) {
        boolean ret = true;
        int i = req.mFailCount + 1;
        req.mFailCount = i;
        if (i >= 2) {
            String error;
            ret = false;
            if (errorId < 0) {
                error = getEventHandlerErrorString(errorId);
            } else {
                Throwable cause = e.getCause();
                error = cause != null ? cause.toString() : e.getMessage();
            }
            req.mEventHandler.error(errorId, error);
            req.complete();
        }
        closeConnection();
        this.mHttpContext.removeAttribute("http.connection");
        return ret;
    }

    private static String getEventHandlerErrorString(int errorId) {
        switch (errorId) {
            case EventHandler.TOO_MANY_REQUESTS_ERROR /*-15*/:
                return "TOO_MANY_REQUESTS_ERROR";
            case EventHandler.FILE_NOT_FOUND_ERROR /*-14*/:
                return "FILE_NOT_FOUND_ERROR";
            case EventHandler.FILE_ERROR /*-13*/:
                return "FILE_ERROR";
            case EventHandler.ERROR_BAD_URL /*-12*/:
                return "ERROR_BAD_URL";
            case EventHandler.ERROR_FAILED_SSL_HANDSHAKE /*-11*/:
                return "ERROR_FAILED_SSL_HANDSHAKE";
            case EventHandler.ERROR_UNSUPPORTED_SCHEME /*-10*/:
                return "ERROR_UNSUPPORTED_SCHEME";
            case EventHandler.ERROR_REDIRECT_LOOP /*-9*/:
                return "ERROR_REDIRECT_LOOP";
            case EventHandler.ERROR_TIMEOUT /*-8*/:
                return "ERROR_TIMEOUT";
            case EventHandler.ERROR_IO /*-7*/:
                return "ERROR_IO";
            case EventHandler.ERROR_CONNECT /*-6*/:
                return "ERROR_CONNECT";
            case EventHandler.ERROR_PROXYAUTH /*-5*/:
                return "ERROR_PROXYAUTH";
            case EventHandler.ERROR_AUTH /*-4*/:
                return "ERROR_AUTH";
            case EventHandler.ERROR_UNSUPPORTED_AUTH_SCHEME /*-3*/:
                return "ERROR_UNSUPPORTED_AUTH_SCHEME";
            case -2:
                return "ERROR_LOOKUP";
            case -1:
                return "ERROR";
            case 0:
                return "OK";
            default:
                return "UNKNOWN_ERROR";
        }
    }

    HttpContext getHttpContext() {
        return this.mHttpContext;
    }

    /* JADX WARNING: Missing block: B:12:0x002c, code skipped:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean keepAlive(HttpEntity entity, ProtocolVersion ver, int connType, HttpContext context) {
        HttpConnection conn = (HttpConnection) context.getAttribute("http.connection");
        if (conn != null && !conn.isOpen()) {
            return false;
        }
        if ((entity != null && entity.getContentLength() < 0 && (!entity.isChunked() || ver.lessEquals(HttpVersion.HTTP_1_0))) || connType == 1) {
            return false;
        }
        if (connType == 2) {
            return true;
        }
        return ver.lessEquals(HttpVersion.HTTP_1_0) ^ 1;
    }

    void setCanPersist(HttpEntity entity, ProtocolVersion ver, int connType) {
        this.mCanPersist = keepAlive(entity, ver, connType, this.mHttpContext);
    }

    void setCanPersist(boolean canPersist) {
        this.mCanPersist = canPersist;
    }

    boolean getCanPersist() {
        return this.mCanPersist;
    }

    public synchronized String toString() {
        return this.mHost.toString();
    }

    byte[] getBuf() {
        if (this.mBuf == null) {
            this.mBuf = new byte[8192];
        }
        return this.mBuf;
    }
}
