package android.graphics;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.BitmapFactory.Options;

public interface IAwareBitmapCacher {
    void cacheBitmap(Resources resources, int i, Bitmap bitmap, Options options);

    void cacheBitmap(String str, Bitmap bitmap, Options options);

    Bitmap getCachedBitmap(Resources resources, int i);

    Bitmap getCachedBitmap(String str);

    void init(String str, Application application);
}
