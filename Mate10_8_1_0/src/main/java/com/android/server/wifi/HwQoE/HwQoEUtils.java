package com.android.server.wifi.HwQoE;

import android.os.SystemProperties;
import android.util.Log;

public class HwQoEUtils {
    public static final int APP_NO_BGLIMIT = 1000;
    public static final int APP_WIFI_NO_SLEEP = 0;
    public static final int APP_WIFI_SLEEP_UNKNOWN = -1;
    public static final int AP_TYPE_BLACK = 2;
    public static final int AP_TYPE_NORMAL = 0;
    public static final int AP_TYPE_WHITE = 1;
    public static final int BG_LIMIT_FG_LEVEL_1 = 1;
    public static final int BG_LIMIT_FG_LEVEL_2 = 2;
    public static final int BG_LIMIT_FG_LEVEL_3 = 3;
    public static final int BG_LIMIT_NO_FG_LEVEL_1 = 11;
    public static final int BG_LIMIT_NO_FG_LEVEL_2 = 12;
    public static final int BG_LIMIT_NO_FG_LEVEL_3 = 13;
    public static final int CHR_HANDOVER_EVENT_TO_CELLULAR = 1;
    public static final int CHR_HANDOVER_EVENT_TO_WIFI = 2;
    public static final boolean GAME_ASSISIT_ENABLE = "1".equalsIgnoreCase(SystemProperties.get("ro.config.gameassist", ""));
    public static final int GENERAL_USER_BLACK_MIN_COUNTER = 2;
    public static final int HOME_AP = 0;
    public static final boolean MAINLAND_REGION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    public static final int MAX_BLACK_COUNTER = 2;
    public static final int MAX_HOLD_COUNTER = 2;
    public static final int PUBLIC_AP = 1;
    public static final int QOE_MSG_BOOT_COMPLETED = 119;
    public static final int QOE_MSG_BT_SCAN_STARTED = 125;
    public static final int QOE_MSG_CAMERA_OFF = 121;
    public static final int QOE_MSG_CAMERA_ON = 120;
    public static final int QOE_MSG_EVALUATE_OTA_INFO = 106;
    public static final int QOE_MSG_MONITOR_GET_QUALITY_INFO = 105;
    public static final int QOE_MSG_MONITOR_HAVE_INTERNET = 103;
    public static final int QOE_MSG_MONITOR_NO_INTERNET = 104;
    public static final int QOE_MSG_MONITOR_START = 118;
    public static final int QOE_MSG_MONITOR_UPDATE_TCP_INFO = 101;
    public static final int QOE_MSG_MONITOR_UPDATE_UDP_INFO = 102;
    public static final int QOE_MSG_SCAN_RESULTS = 124;
    public static final int QOE_MSG_SCREEN_OFF = 127;
    public static final int QOE_MSG_SCREEN_ON = 126;
    public static final int QOE_MSG_UPDATE_QUALITY_INFO = 122;
    public static final int QOE_MSG_UPDATE_UID_TCP_RTT = 123;
    public static final int QOE_MSG_WIFI_CHECK_TIMEOUT = 112;
    public static final int QOE_MSG_WIFI_CONNECTED = 109;
    public static final int QOE_MSG_WIFI_DELAY_DISCONNECT = 116;
    public static final int QOE_MSG_WIFI_DISABLE = 108;
    public static final int QOE_MSG_WIFI_DISCONNECT = 110;
    public static final int QOE_MSG_WIFI_ENABLED = 107;
    public static final int QOE_MSG_WIFI_EVALUATE_TIMEOUT = 114;
    public static final int QOE_MSG_WIFI_INTERNET = 111;
    public static final int QOE_MSG_WIFI_ROAMING = 115;
    public static final int QOE_MSG_WIFI_RSSI_CHANGED = 117;
    public static final int QOE_MSG_WIFI_START_EVALUATE = 113;
    public static final int SENSITIVE_APP_SCORE = 2;
    public static final int SENSITIVE_UPD_PACKETS = 10;
    public static final String TAG = "HiDATA";
    public static final int USER_TYPE_GENERAL = 0;
    public static final int USER_TYPE_WEALTHY = 1;
    public static final int VOWIFI_STATE_CALL_BEGIN = 1;
    public static final int VOWIFI_STATE_CALL_END = 2;
    public static final int VOWIFI_STATE_CALL_IDLE = 0;
    public static final int WEALTHY_USER_BLACK_MIN_COUNTER = 1;
    public static final int WECHAT_BACK_GROUND_CHANG = 2;
    public static final int WECHAT_CALL_OFF = 0;
    public static final int WECHAT_CALL_ON = 1;
    public static final int WECHAT_STATE_OFF = 0;
    public static final int WECHAT_STATE_ON = 1;
    public static final int WECHAT_TYPE_AUDIO = 2;
    public static final int WECHAT_TYPE_UNKOWN = 0;
    public static final int WECHAT_TYPE_VIDEO = 1;
    public static final int WHITE_LIST_TYPE_WIFI_SLEEP = 7;
    public static final int WIFI_CHECK_TIMEOUT = 4000;

    public static void logD(String info) {
        Log.d(TAG, info);
    }

    public static void logW(String info) {
        Log.w(TAG, info);
    }

    public static void logE(String info) {
        Log.e(TAG, info);
    }
}