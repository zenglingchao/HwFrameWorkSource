package android.view;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class PixelCopy {
    public static final int ERROR_DESTINATION_INVALID = 5;
    public static final int ERROR_SOURCE_INVALID = 4;
    public static final int ERROR_SOURCE_NO_DATA = 3;
    public static final int ERROR_TIMEOUT = 2;
    public static final int ERROR_UNKNOWN = 1;
    public static final int SUCCESS = 0;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CopyResultStatus {
    }

    public interface OnPixelCopyFinishedListener {
        void onPixelCopyFinished(int i);
    }

    public static void request(SurfaceView source, Bitmap dest, OnPixelCopyFinishedListener listener, Handler listenerThread) {
        request(source.getHolder().getSurface(), dest, listener, listenerThread);
    }

    public static void request(SurfaceView source, Rect srcRect, Bitmap dest, OnPixelCopyFinishedListener listener, Handler listenerThread) {
        request(source.getHolder().getSurface(), srcRect, dest, listener, listenerThread);
    }

    public static void request(Surface source, Bitmap dest, OnPixelCopyFinishedListener listener, Handler listenerThread) {
        request(source, null, dest, listener, listenerThread);
    }

    public static void request(Surface source, Rect srcRect, Bitmap dest, final OnPixelCopyFinishedListener listener, Handler listenerThread) {
        validateBitmapDest(dest);
        if (!source.isValid()) {
            throw new IllegalArgumentException("Surface isn't valid, source.isValid() == false");
        } else if (srcRect == null || !srcRect.isEmpty()) {
            final int result = ThreadedRenderer.copySurfaceInto(source, srcRect, dest);
            listenerThread.post(new Runnable() {
                public void run() {
                    listener.onPixelCopyFinished(result);
                }
            });
        } else {
            throw new IllegalArgumentException("sourceRect is empty");
        }
    }

    public static void request(Window source, Bitmap dest, OnPixelCopyFinishedListener listener, Handler listenerThread) {
        request(source, null, dest, listener, listenerThread);
    }

    public static void request(Window source, Rect srcRect, Bitmap dest, OnPixelCopyFinishedListener listener, Handler listenerThread) {
        validateBitmapDest(dest);
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        } else if (source.peekDecorView() != null) {
            Surface surface = null;
            ViewRootImpl root = source.peekDecorView().getViewRootImpl();
            if (root != null) {
                surface = root.mSurface;
                Rect surfaceInsets = root.mWindowAttributes.surfaceInsets;
                if (srcRect == null) {
                    srcRect = new Rect(surfaceInsets.left, surfaceInsets.top, root.mWidth + surfaceInsets.left, root.mHeight + surfaceInsets.top);
                } else {
                    srcRect.offset(surfaceInsets.left, surfaceInsets.top);
                }
            }
            if (surface == null || !surface.isValid()) {
                throw new IllegalArgumentException("Window doesn't have a backing surface!");
            }
            request(surface, srcRect, dest, listener, listenerThread);
        } else {
            throw new IllegalArgumentException("Only able to copy windows with decor views");
        }
    }

    private static void validateBitmapDest(Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("Bitmap cannot be null");
        } else if (bitmap.isRecycled()) {
            throw new IllegalArgumentException("Bitmap is recycled");
        } else if (!bitmap.isMutable()) {
            throw new IllegalArgumentException("Bitmap is immutable");
        }
    }

    private PixelCopy() {
    }
}
