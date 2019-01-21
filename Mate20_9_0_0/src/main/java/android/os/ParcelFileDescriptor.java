package android.os;

import android.os.MessageQueue.OnFileDescriptorEventListener;
import android.os.Parcelable.Creator;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.ByteOrder;
import libcore.io.IoUtils;
import libcore.io.Memory;

public class ParcelFileDescriptor implements Parcelable, Closeable {
    public static final Creator<ParcelFileDescriptor> CREATOR = new Creator<ParcelFileDescriptor>() {
        public ParcelFileDescriptor createFromParcel(Parcel in) {
            int hasCommChannel = in.readInt();
            FileDescriptor fd = in.readRawFileDescriptor();
            FileDescriptor commChannel = null;
            if (hasCommChannel != 0) {
                commChannel = in.readRawFileDescriptor();
            }
            return new ParcelFileDescriptor(fd, commChannel);
        }

        public ParcelFileDescriptor[] newArray(int size) {
            return new ParcelFileDescriptor[size];
        }
    };
    private static final int MAX_STATUS = 1024;
    public static final int MODE_APPEND = 33554432;
    public static final int MODE_CREATE = 134217728;
    public static final int MODE_READ_ONLY = 268435456;
    public static final int MODE_READ_WRITE = 805306368;
    public static final int MODE_TRUNCATE = 67108864;
    @Deprecated
    public static final int MODE_WORLD_READABLE = 1;
    @Deprecated
    public static final int MODE_WORLD_WRITEABLE = 2;
    public static final int MODE_WRITE_ONLY = 536870912;
    private static final String TAG = "ParcelFileDescriptor";
    private volatile boolean mClosed;
    private FileDescriptor mCommFd;
    private final FileDescriptor mFd;
    private final CloseGuard mGuard;
    private Status mStatus;
    private byte[] mStatusBuf;
    private final ParcelFileDescriptor mWrapped;

    public static class AutoCloseInputStream extends FileInputStream {
        private final ParcelFileDescriptor mPfd;

        public AutoCloseInputStream(ParcelFileDescriptor pfd) {
            super(pfd.getFileDescriptor());
            this.mPfd = pfd;
        }

        public void close() throws IOException {
            try {
                this.mPfd.close();
            } finally {
                super.close();
            }
        }

        public int read() throws IOException {
            int result = super.read();
            if (result == -1 && this.mPfd.canDetectErrors()) {
                this.mPfd.checkError();
            }
            return result;
        }

        public int read(byte[] b) throws IOException {
            int result = super.read(b);
            if (result == -1 && this.mPfd.canDetectErrors()) {
                this.mPfd.checkError();
            }
            return result;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int result = super.read(b, off, len);
            if (result == -1 && this.mPfd.canDetectErrors()) {
                this.mPfd.checkError();
            }
            return result;
        }
    }

    public static class AutoCloseOutputStream extends FileOutputStream {
        private final ParcelFileDescriptor mPfd;

        public AutoCloseOutputStream(ParcelFileDescriptor pfd) {
            super(pfd.getFileDescriptor());
            this.mPfd = pfd;
        }

        public void close() throws IOException {
            try {
                this.mPfd.close();
            } finally {
                super.close();
            }
        }
    }

    public static class FileDescriptorDetachedException extends IOException {
        private static final long serialVersionUID = 955542466045L;

        public FileDescriptorDetachedException() {
            super("Remote side is detached");
        }
    }

    public interface OnCloseListener {
        void onClose(IOException iOException);
    }

    private static class Status {
        public static final int DEAD = -2;
        public static final int DETACHED = 2;
        public static final int ERROR = 1;
        public static final int LEAKED = 3;
        public static final int OK = 0;
        public static final int SILENCE = -1;
        public final String msg;
        public final int status;

        public Status(int status) {
            this(status, null);
        }

        public Status(int status, String msg) {
            this.status = status;
            this.msg = msg;
        }

        public IOException asIOException() {
            int i = this.status;
            if (i == -2) {
                return new IOException("Remote side is dead");
            }
            StringBuilder stringBuilder;
            switch (i) {
                case 0:
                    return null;
                case 1:
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Remote error: ");
                    stringBuilder.append(this.msg);
                    return new IOException(stringBuilder.toString());
                case 2:
                    return new FileDescriptorDetachedException();
                case 3:
                    return new IOException("Remote side was leaked");
                default:
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Unknown status: ");
                    stringBuilder.append(this.status);
                    return new IOException(stringBuilder.toString());
            }
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{");
            stringBuilder.append(this.status);
            stringBuilder.append(": ");
            stringBuilder.append(this.msg);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
    }

    public ParcelFileDescriptor(ParcelFileDescriptor wrapped) {
        this.mGuard = CloseGuard.get();
        this.mWrapped = wrapped;
        this.mFd = null;
        this.mCommFd = null;
        this.mClosed = true;
    }

    public ParcelFileDescriptor(FileDescriptor fd) {
        this(fd, null);
    }

    public ParcelFileDescriptor(FileDescriptor fd, FileDescriptor commChannel) {
        this.mGuard = CloseGuard.get();
        if (fd != null) {
            this.mWrapped = null;
            this.mFd = fd;
            this.mCommFd = commChannel;
            this.mGuard.open("close");
            return;
        }
        throw new NullPointerException("FileDescriptor must not be null");
    }

    public static ParcelFileDescriptor open(File file, int mode) throws FileNotFoundException {
        FileDescriptor fd = openInternal(file, mode);
        if (fd == null) {
            return null;
        }
        return new ParcelFileDescriptor(fd);
    }

    public static ParcelFileDescriptor open(File file, int mode, Handler handler, OnCloseListener listener) throws IOException {
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null");
        } else if (listener != null) {
            FileDescriptor fd = openInternal(file, mode);
            if (fd == null) {
                return null;
            }
            return fromFd(fd, handler, listener);
        } else {
            throw new IllegalArgumentException("Listener must not be null");
        }
    }

    public static ParcelFileDescriptor fromFd(FileDescriptor fd, Handler handler, final OnCloseListener listener) throws IOException {
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null");
        } else if (listener != null) {
            FileDescriptor[] comm = createCommSocketPair();
            ParcelFileDescriptor pfd = new ParcelFileDescriptor(fd, comm[0]);
            final MessageQueue queue = handler.getLooper().getQueue();
            queue.addOnFileDescriptorEventListener(comm[1], 1, new OnFileDescriptorEventListener() {
                public int onFileDescriptorEvents(FileDescriptor fd, int events) {
                    Status status = null;
                    if ((events & 1) != 0) {
                        status = ParcelFileDescriptor.readCommStatus(fd, new byte[1024]);
                    } else if ((events & 4) != 0) {
                        status = new Status(-2);
                    }
                    if (status == null) {
                        return 1;
                    }
                    queue.removeOnFileDescriptorEventListener(fd);
                    IoUtils.closeQuietly(fd);
                    listener.onClose(status.asIOException());
                    return 0;
                }
            });
            return pfd;
        } else {
            throw new IllegalArgumentException("Listener must not be null");
        }
    }

    private static FileDescriptor openInternal(File file, int mode) throws FileNotFoundException {
        if ((mode & 805306368) != 0) {
            int flags = 0;
            int i = mode & 805306368;
            if (i == 0 || i == 268435456) {
                flags = OsConstants.O_RDONLY;
            } else if (i == 536870912) {
                flags = OsConstants.O_WRONLY;
            } else if (i == 805306368) {
                flags = OsConstants.O_RDWR;
            }
            if ((134217728 & mode) != 0) {
                flags |= OsConstants.O_CREAT;
            }
            if ((67108864 & mode) != 0) {
                flags |= OsConstants.O_TRUNC;
            }
            if ((33554432 & mode) != 0) {
                flags |= OsConstants.O_APPEND;
            }
            int realMode = OsConstants.S_IRWXU | OsConstants.S_IRWXG;
            if ((mode & 1) != 0) {
                realMode |= OsConstants.S_IROTH;
            }
            if ((mode & 2) != 0) {
                realMode |= OsConstants.S_IWOTH;
            }
            try {
                return Os.open(file.getPath(), flags, realMode);
            } catch (ErrnoException e) {
                throw new FileNotFoundException(e.getMessage());
            }
        }
        throw new IllegalArgumentException("Must specify MODE_READ_ONLY, MODE_WRITE_ONLY, or MODE_READ_WRITE");
    }

    public static ParcelFileDescriptor dup(FileDescriptor orig) throws IOException {
        try {
            return new ParcelFileDescriptor(Os.dup(orig));
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public ParcelFileDescriptor dup() throws IOException {
        if (this.mWrapped != null) {
            return this.mWrapped.dup();
        }
        return dup(getFileDescriptor());
    }

    public static ParcelFileDescriptor fromFd(int fd) throws IOException {
        FileDescriptor original = new FileDescriptor();
        original.setInt$(fd);
        try {
            return new ParcelFileDescriptor(Os.dup(original));
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public static ParcelFileDescriptor adoptFd(int fd) {
        FileDescriptor fdesc = new FileDescriptor();
        fdesc.setInt$(fd);
        return new ParcelFileDescriptor(fdesc);
    }

    public static ParcelFileDescriptor fromSocket(Socket socket) {
        FileDescriptor fd = socket.getFileDescriptor$();
        return fd != null ? new ParcelFileDescriptor(fd) : null;
    }

    public static ParcelFileDescriptor fromDatagramSocket(DatagramSocket datagramSocket) {
        FileDescriptor fd = datagramSocket.getFileDescriptor$();
        return fd != null ? new ParcelFileDescriptor(fd) : null;
    }

    public static ParcelFileDescriptor[] createPipe() throws IOException {
        try {
            FileDescriptor[] fds = Os.pipe();
            return new ParcelFileDescriptor[]{new ParcelFileDescriptor(fds[0]), new ParcelFileDescriptor(fds[1])};
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public static ParcelFileDescriptor[] createReliablePipe() throws IOException {
        try {
            FileDescriptor[] comm = createCommSocketPair();
            FileDescriptor[] fds = Os.pipe();
            return new ParcelFileDescriptor[]{new ParcelFileDescriptor(fds[0], comm[0]), new ParcelFileDescriptor(fds[1], comm[1])};
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public static ParcelFileDescriptor[] createSocketPair() throws IOException {
        return createSocketPair(OsConstants.SOCK_STREAM);
    }

    public static ParcelFileDescriptor[] createSocketPair(int type) throws IOException {
        try {
            Os.socketpair(OsConstants.AF_UNIX, type, 0, new FileDescriptor(), new FileDescriptor());
            return new ParcelFileDescriptor[]{new ParcelFileDescriptor(fd0), new ParcelFileDescriptor(fd1)};
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public static ParcelFileDescriptor[] createReliableSocketPair() throws IOException {
        return createReliableSocketPair(OsConstants.SOCK_STREAM);
    }

    public static ParcelFileDescriptor[] createReliableSocketPair(int type) throws IOException {
        try {
            FileDescriptor[] comm = createCommSocketPair();
            Os.socketpair(OsConstants.AF_UNIX, type, 0, new FileDescriptor(), new FileDescriptor());
            return new ParcelFileDescriptor[]{new ParcelFileDescriptor(fd0, comm[0]), new ParcelFileDescriptor(fd1, comm[1])};
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    private static FileDescriptor[] createCommSocketPair() throws IOException {
        try {
            FileDescriptor comm1 = new FileDescriptor();
            FileDescriptor comm2 = new FileDescriptor();
            Os.socketpair(OsConstants.AF_UNIX, OsConstants.SOCK_SEQPACKET, 0, comm1, comm2);
            IoUtils.setBlocking(comm1, false);
            IoUtils.setBlocking(comm2, false);
            return new FileDescriptor[]{comm1, comm2};
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    @Deprecated
    public static ParcelFileDescriptor fromData(byte[] data, String name) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = null;
        if (data == null) {
            return null;
        }
        MemoryFile file = new MemoryFile(name, data.length);
        if (data.length > 0) {
            file.writeBytes(data, 0, 0, data.length);
        }
        file.deactivate();
        FileDescriptor fd = file.getFileDescriptor();
        if (fd != null) {
            parcelFileDescriptor = new ParcelFileDescriptor(fd);
        }
        return parcelFileDescriptor;
    }

    public static int parseMode(String mode) {
        if ("r".equals(mode)) {
            return 268435456;
        }
        if ("w".equals(mode) || "wt".equals(mode)) {
            return 738197504;
        }
        if ("wa".equals(mode)) {
            return 704643072;
        }
        if ("rw".equals(mode)) {
            return 939524096;
        }
        if ("rwt".equals(mode)) {
            return 1006632960;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Bad mode '");
        stringBuilder.append(mode);
        stringBuilder.append("'");
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public static File getFile(FileDescriptor fd) throws IOException {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("/proc/self/fd/");
            stringBuilder.append(fd.getInt$());
            String path = Os.readlink(stringBuilder.toString());
            if (OsConstants.S_ISREG(Os.stat(path).st_mode)) {
                return new File(path);
            }
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Not a regular file: ");
            stringBuilder2.append(path);
            throw new IOException(stringBuilder2.toString());
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public FileDescriptor getFileDescriptor() {
        if (this.mWrapped != null) {
            return this.mWrapped.getFileDescriptor();
        }
        return this.mFd;
    }

    public long getStatSize() {
        if (this.mWrapped != null) {
            return this.mWrapped.getStatSize();
        }
        try {
            StructStat st = Os.fstat(this.mFd);
            if (!OsConstants.S_ISREG(st.st_mode)) {
                if (!OsConstants.S_ISLNK(st.st_mode)) {
                    return -1;
                }
            }
            return st.st_size;
        } catch (ErrnoException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("fstat() failed: ");
            stringBuilder.append(e);
            Log.w(str, stringBuilder.toString());
            return -1;
        }
    }

    public long seekTo(long pos) throws IOException {
        if (this.mWrapped != null) {
            return this.mWrapped.seekTo(pos);
        }
        try {
            return Os.lseek(this.mFd, pos, OsConstants.SEEK_SET);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public int getFd() {
        if (this.mWrapped != null) {
            return this.mWrapped.getFd();
        }
        if (!this.mClosed) {
            return this.mFd.getInt$();
        }
        throw new IllegalStateException("Already closed");
    }

    public int detachFd() {
        if (this.mWrapped != null) {
            return this.mWrapped.detachFd();
        }
        if (this.mClosed) {
            throw new IllegalStateException("Already closed");
        }
        int fd = getFd();
        this.mFd.setInt$(-1);
        writeCommStatusAndClose(2, null);
        this.mClosed = true;
        this.mGuard.close();
        releaseResources();
        return fd;
    }

    public void close() throws IOException {
        if (this.mWrapped != null) {
            try {
                this.mWrapped.close();
            } finally {
                releaseResources();
            }
        } else {
            closeWithStatus(0, null);
        }
    }

    public void closeWithError(String msg) throws IOException {
        if (this.mWrapped != null) {
            try {
                this.mWrapped.closeWithError(msg);
            } finally {
                releaseResources();
            }
        } else if (msg != null) {
            closeWithStatus(1, msg);
        } else {
            throw new IllegalArgumentException("Message must not be null");
        }
    }

    private void closeWithStatus(int status, String msg) {
        if (!this.mClosed) {
            this.mClosed = true;
            if (this.mGuard != null) {
                this.mGuard.close();
            }
            writeCommStatusAndClose(status, msg);
            IoUtils.closeQuietly(this.mFd);
            releaseResources();
        }
    }

    public void releaseResources() {
    }

    private byte[] getOrCreateStatusBuffer() {
        if (this.mStatusBuf == null) {
            this.mStatusBuf = new byte[1024];
        }
        return this.mStatusBuf;
    }

    private void writeCommStatusAndClose(int status, String msg) {
        String str;
        StringBuilder stringBuilder;
        if (this.mCommFd == null) {
            if (msg != null) {
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Unable to inform peer: ");
                stringBuilder2.append(msg);
                Log.w(str2, stringBuilder2.toString());
            }
            return;
        }
        if (status == 2) {
            Log.w(TAG, "Peer expected signal when closed; unable to deliver after detach");
        }
        if (status == -1) {
            IoUtils.closeQuietly(this.mCommFd);
            this.mCommFd = null;
            return;
        }
        try {
            this.mStatus = readCommStatus(this.mCommFd, getOrCreateStatusBuffer());
            if (this.mStatus != null) {
                IoUtils.closeQuietly(this.mCommFd);
                this.mCommFd = null;
                return;
            }
            byte[] buf = getOrCreateStatusBuffer();
            Memory.pokeInt(buf, 0, status, ByteOrder.BIG_ENDIAN);
            int writePtr = 0 + 4;
            if (msg != null) {
                byte[] rawMsg = msg.getBytes();
                int len = Math.min(rawMsg.length, buf.length - writePtr);
                System.arraycopy(rawMsg, 0, buf, writePtr, len);
                writePtr += len;
            }
            Os.write(this.mCommFd, buf, 0, writePtr);
            IoUtils.closeQuietly(this.mCommFd);
            this.mCommFd = null;
        } catch (ErrnoException e) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to report status: ");
            stringBuilder.append(e);
            Log.w(str, stringBuilder.toString());
        } catch (InterruptedIOException e2) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to report status: ");
            stringBuilder.append(e2);
            Log.w(str, stringBuilder.toString());
        } catch (Throwable th) {
            IoUtils.closeQuietly(this.mCommFd);
            this.mCommFd = null;
        }
    }

    private static Status readCommStatus(FileDescriptor comm, byte[] buf) {
        String str;
        StringBuilder stringBuilder;
        try {
            int n = Os.read(comm, buf, 0, buf.length);
            if (n == 0) {
                return new Status(-2);
            }
            int status = Memory.peekInt(buf, 0, ByteOrder.BIG_ENDIAN);
            if (status == 1) {
                return new Status(status, new String(buf, 4, n - 4));
            }
            return new Status(status);
        } catch (ErrnoException e) {
            if (e.errno == OsConstants.EAGAIN) {
                return null;
            }
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to read status; assuming dead: ");
            stringBuilder.append(e);
            Log.d(str, stringBuilder.toString());
            return new Status(-2);
        } catch (InterruptedIOException e2) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to read status; assuming dead: ");
            stringBuilder.append(e2);
            Log.d(str, stringBuilder.toString());
            return new Status(-2);
        }
    }

    public boolean canDetectErrors() {
        if (this.mWrapped != null) {
            return this.mWrapped.canDetectErrors();
        }
        return this.mCommFd != null;
    }

    public void checkError() throws IOException {
        if (this.mWrapped != null) {
            this.mWrapped.checkError();
            return;
        }
        if (this.mStatus == null) {
            if (this.mCommFd == null) {
                Log.w(TAG, "Peer didn't provide a comm channel; unable to check for errors");
                return;
            }
            this.mStatus = readCommStatus(this.mCommFd, getOrCreateStatusBuffer());
        }
        if (this.mStatus != null && this.mStatus.status != 0) {
            throw this.mStatus.asIOException();
        }
    }

    public String toString() {
        if (this.mWrapped != null) {
            return this.mWrapped.toString();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{ParcelFileDescriptor: ");
        stringBuilder.append(this.mFd);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    protected void finalize() throws Throwable {
        if (this.mWrapped != null) {
            releaseResources();
        }
        if (this.mGuard != null) {
            this.mGuard.warnIfOpen();
        }
        try {
            if (!this.mClosed) {
                closeWithStatus(3, null);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public int describeContents() {
        if (this.mWrapped != null) {
            return this.mWrapped.describeContents();
        }
        return 1;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.mWrapped != null) {
            try {
                this.mWrapped.writeToParcel(out, flags);
            } finally {
                releaseResources();
            }
        } else {
            if (this.mCommFd != null) {
                out.writeInt(1);
                out.writeFileDescriptor(this.mFd);
                out.writeFileDescriptor(this.mCommFd);
            } else {
                out.writeInt(0);
                out.writeFileDescriptor(this.mFd);
            }
            if ((flags & 1) != 0 && !this.mClosed) {
                closeWithStatus(-1, null);
            }
        }
    }
}
