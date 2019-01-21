package android.app;

import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ServiceConnectionUtils {
    static void connected(Object serviceConnection, ComponentName name, IBinder service, boolean dead) {
        try {
            if (serviceConnection instanceof IServiceConnection) {
                ((IServiceConnection) serviceConnection).connected(name, service, dead);
            }
        } catch (RemoteException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("connected Exception:");
            stringBuilder.append(e.getMessage());
            Log.i("ServiceConnectionUtils", stringBuilder.toString());
        }
    }
}
