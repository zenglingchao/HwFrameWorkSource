package libcore.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import sun.net.www.ParseUtil;
import sun.net.www.protocol.jar.Handler;

public class ClassPathURLStreamHandler extends Handler {
    private final String fileUri;
    private final JarFile jarFile;

    private class ClassPathURLConnection extends JarURLConnection {
        private boolean closed;
        private JarFile connectionJarFile;
        private ZipEntry jarEntry;
        private InputStream jarInput;
        private boolean useCachedJarFile;

        public ClassPathURLConnection(URL url) throws MalformedURLException {
            super(url);
        }

        public void connect() throws IOException {
            if (!this.connected) {
                this.jarEntry = ClassPathURLStreamHandler.findEntryWithDirectoryFallback(ClassPathURLStreamHandler.this.jarFile, getEntryName());
                if (this.jarEntry != null) {
                    this.useCachedJarFile = getUseCaches();
                    this.connected = true;
                    return;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("URL does not correspond to an entry in the zip file. URL=");
                stringBuilder.append(this.url);
                stringBuilder.append(", zipfile=");
                stringBuilder.append(ClassPathURLStreamHandler.this.jarFile.getName());
                throw new FileNotFoundException(stringBuilder.toString());
            }
        }

        public JarFile getJarFile() throws IOException {
            connect();
            if (this.useCachedJarFile) {
                this.connectionJarFile = ClassPathURLStreamHandler.this.jarFile;
            } else {
                this.connectionJarFile = new JarFile(ClassPathURLStreamHandler.this.jarFile.getName());
            }
            return this.connectionJarFile;
        }

        public InputStream getInputStream() throws IOException {
            if (this.closed) {
                throw new IllegalStateException("JarURLConnection InputStream has been closed");
            }
            connect();
            if (this.jarInput != null) {
                return this.jarInput;
            }
            AnonymousClass1 anonymousClass1 = new FilterInputStream(ClassPathURLStreamHandler.this.jarFile.getInputStream(this.jarEntry)) {
                public void close() throws IOException {
                    super.close();
                    if (ClassPathURLConnection.this.connectionJarFile != null && !ClassPathURLConnection.this.useCachedJarFile) {
                        ClassPathURLConnection.this.connectionJarFile.close();
                        ClassPathURLConnection.this.closed = true;
                    }
                }
            };
            this.jarInput = anonymousClass1;
            return anonymousClass1;
        }

        public String getContentType() {
            String cType = guessContentTypeFromName(getEntryName());
            if (cType == null) {
                return "content/unknown";
            }
            return cType;
        }

        public int getContentLength() {
            try {
                connect();
                return (int) getJarEntry().getSize();
            } catch (IOException e) {
                return -1;
            }
        }
    }

    public ClassPathURLStreamHandler(String jarFileName) throws IOException {
        this.jarFile = new JarFile(jarFileName);
        this.fileUri = new File(jarFileName).toURI().toString();
    }

    public URL getEntryUrlOrNull(String entryName) {
        if (findEntryWithDirectoryFallback(this.jarFile, entryName) == null) {
            return null;
        }
        try {
            String encodedName = ParseUtil.encodePath(entryName, null);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.fileUri);
            stringBuilder.append("!/");
            stringBuilder.append(encodedName);
            return new URL("jar", null, -1, stringBuilder.toString(), this);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid entry name", e);
        }
    }

    public boolean isEntryStored(String entryName) {
        ZipEntry entry = this.jarFile.getEntry(entryName);
        return entry != null && entry.getMethod() == 0;
    }

    protected URLConnection openConnection(URL url) throws IOException {
        return new ClassPathURLConnection(url);
    }

    public void close() throws IOException {
        this.jarFile.close();
    }

    static ZipEntry findEntryWithDirectoryFallback(JarFile jarFile, String entryName) {
        ZipEntry entry = jarFile.getEntry(entryName);
        if (entry != null || entryName.endsWith("/")) {
            return entry;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(entryName);
        stringBuilder.append("/");
        return jarFile.getEntry(stringBuilder.toString());
    }
}
