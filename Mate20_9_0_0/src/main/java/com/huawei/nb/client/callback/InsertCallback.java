package com.huawei.nb.client.callback;

import android.os.RemoteException;
import com.huawei.nb.callback.IInsertCallback.Stub;
import com.huawei.nb.container.ObjectContainer;
import java.util.Collections;
import java.util.List;

public class InsertCallback extends Stub implements WaitableCallback<List> {
    private final CallbackManager callbackManager;
    private final CallbackWaiter<List> callbackWaiter = new CallbackWaiter(Collections.EMPTY_LIST);

    InsertCallback(CallbackManager callbackManager) {
        this.callbackManager = callbackManager;
    }

    public void onResult(int transactionId, ObjectContainer container) throws RemoteException {
        this.callbackWaiter.set(transactionId, container == null ? Collections.EMPTY_LIST : container.get());
    }

    public List await(int transactionId, long timeout) {
        this.callbackManager.startWaiting(this);
        List result = (List) this.callbackWaiter.await(transactionId, timeout);
        this.callbackManager.stopWaiting(this);
        return result;
    }

    public void interrupt() {
        this.callbackWaiter.interrupt();
    }
}
