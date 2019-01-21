package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccProfile;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.ProcessAction;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimDisableProcessor extends HwVSimProcessor {
    public static final String LOG_TAG = "VSimDisableProcess";
    protected Handler mHandler = this.mVSimController.getHandler();
    protected HwVSimController mVSimController;

    public HwVSimDisableProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    public void onEnter() {
        logd("onEnter");
        cmdSem_release();
        int subId = 0;
        if (this.mRequest == null) {
            transitionToState(0);
            return;
        }
        this.mVSimController.setProcessAction(ProcessAction.PROCESS_ACTION_DISABLE);
        HwVSimRequest request = this.mRequest;
        request.createGotCardType(HwVSimModemAdapter.PHONE_COUNT);
        request.createCardTypes(HwVSimModemAdapter.PHONE_COUNT);
        request.setGotSimSlotMark(false);
        while (subId < HwVSimModemAdapter.MAX_SUB_COUNT) {
            if (this.mModemAdapter.getCiBySub(subId) != null && HwVSimUtilsInner.isRadioAvailable(subId)) {
                this.mModemAdapter.getSimSlot(this, request.clone(), subId);
            }
            subId++;
        }
        this.mVSimController.setOnRadioAvaliable(this.mHandler, 83, null);
        this.mVSimController.clearRecoverMarkToFalseMessage();
    }

    public void onExit() {
        logd("onExit");
        cleanMainSlotIfDisableSucceed();
        this.mVSimController.setBlockPinFlag(false);
        this.mModemAdapter.setHwVSimPowerOff(this, this.mRequest);
        allowDefaultData();
        clearCardReloadMarkIfSimLoaded();
        this.mVSimController.setSubActivationUpdate(true);
        this.mVSimController.updateSubActivation();
        this.mVSimController.unSetOnRadioAvaliable(this.mHandler);
    }

    public boolean processMessage(Message msg) {
        int i = msg.what;
        if (i == 54) {
            onGetSimSlotDone(msg);
            return true;
        } else if (i != 56) {
            return false;
        } else {
            onQueryCardTypeDone(msg);
            return true;
        }
    }

    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doDisableProcessException(ar, request);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private void onGetSimSlotDone(Message msg) {
        logd("onGetSimSlotDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = ar.userObj;
            if (request.isGotSimSlot()) {
                logd("already got sim slot, just skip");
                return;
            }
            this.mModemAdapter.onGetSimSlotDone(this, ar);
            if (request.getIsVSimOnM0()) {
                this.mModemAdapter.getAllCardTypes(this, this.mRequest);
            } else {
                notifyResult(request, Boolean.valueOf(true));
                transitionToState(0);
            }
        }
    }

    protected void onQueryCardTypeDone(Message msg) {
        logd("onQueryCardTypeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 1);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onQueryCardTypeDone(this, ar);
            if (this.mRequest.isGotAllCardTypes()) {
                logd("onQueryCardTypeDone : isGotAllCardTypes");
                this.mModemAdapter.checkDisableSimCondition(this, this.mRequest);
            }
        }
    }

    private void cmdSem_release() {
        if (this.mVSimController != null) {
            this.mVSimController.cmdSem_release();
        }
    }

    private void allowDefaultData() {
        if (this.mVSimController != null) {
            this.mVSimController.allowDefaultData();
        }
    }

    private void cleanMainSlotIfDisableSucceed() {
        boolean iResult = false;
        if (this.mRequest != null) {
            Object oResult = this.mRequest.getResult();
            if (oResult != null) {
                iResult = ((Boolean) oResult).booleanValue();
            }
        }
        if (iResult) {
            this.mVSimController.setVSimSavedMainSlot(-1);
            HwVSimPhoneFactory.setVSimSavedNetworkMode(0, -1);
            HwVSimPhoneFactory.setVSimSavedNetworkMode(1, -1);
            return;
        }
        logd("leave saved main slot untouched");
    }

    private void clearCardReloadMarkIfSimLoaded() {
        for (int subId = 0; subId < HwVSimModemAdapter.PHONE_COUNT; subId++) {
            boolean loaded = false;
            UiccCard uiccCard = this.mVSimController.getUiccCard(subId);
            if (uiccCard != null) {
                UiccProfile profile = uiccCard.getUiccProfile();
                if (profile != null) {
                    IccRecords records = profile.getIccRecordsHw();
                    boolean z = records != null && records.isLoaded();
                    loaded = z;
                }
            }
            if (loaded) {
                logd("clearCardReloadMarkIfSimLoaded, sim card loaded, clear the mark.");
                this.mVSimController.setMarkForCardReload(subId, false);
            }
        }
    }
}
