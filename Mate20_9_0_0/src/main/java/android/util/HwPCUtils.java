package android.util;

import android.app.ActivityOptions;
import android.app.ContextImpl;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.ContextWrapper;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.pc.HwPCManager;
import android.pc.IHwPCManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.inputmethod.InputMethodInfo;
import java.util.ArrayList;
import java.util.List;

public final class HwPCUtils {
    public static final String ACTION_CHANGE_PC_WALLPAPER = "com.huawei.pcfilemanager.change_wallpaper";
    public static final String APP_BRIGHTNESS_NAME = "com.android.systemui/.settings.BrightnessDialog";
    public static final String APP_GLOBALACTIONS_NAME = "HwGlobalActions";
    public static final String APP_GMS_NAME = "com.google.android.gms";
    public static final String APP_TOUCHPAD_COMPONENT_NAME = "com.huawei.desktop.systemui/com.huawei.systemui.mk.activity.ImitateActivity";
    public static final String APP_VOLUME_NAME = "VolumeDialog";
    public static final String CLOUD_ACTIVITY_NAME = "com.huawei.ahdp.session.VmActivity";
    public static final String CLOUD_PKG_NAME = "com.huawei.cloud";
    private static final boolean ENABLED = SystemProperties.getBoolean("ro.config.hw_emui_desktop_mode", false);
    private static final boolean ENABLED_IN_PAD = SystemProperties.getBoolean("ro.config.hw_emui_pad_pc_mode", false);
    public static final int FIRST_PC_DYNAMIC_STACK_ID = 1000000008;
    public static final int FORCED_PC_DISPLAY_DENSITY_160 = 1001;
    public static final int FORCED_PC_DISPLAY_DENSITY_240 = 1002;
    public static final int FORCED_PC_DISPLAY_DENSITY_DEFAULT_240 = 1004;
    public static final int FORCED_PC_DISPLAY_DENSITY_GET = 1000;
    public static final int FORCED_PC_DISPLAY_DENSITY_OTHER = 1003;
    public static final int FORCED_PC_DISPLAY_SIZE_GET_OVERSCAN_MODE = 1005;
    public static final int FORCED_PC_DISPLAY_SIZE_OVERSCAN_MODE = 1006;
    public static final int FORCED_PC_DISPLAY_SIZE_UNOVERSCAN1_MODE = 1008;
    public static final int FORCED_PC_DISPLAY_SIZE_UNOVERSCAN_MODE = 1007;
    public static final int HAPPYCAST_DPI = 240;
    private static boolean IS_WIFI_PC_MODE = false;
    private static final boolean LOG_ENABLED = SystemProperties.getBoolean("hw.pc.log.enabled", true);
    public static final String NAME_LIGHTER_DRAWER_VIEW = "com.huawei.systemui.mk.lighterdrawer.LighterDrawView";
    public static final int OTHER_DISPLAY_NOT_STARTED_APP = -1;
    public static final int OTHER_DISPLAY_SPECIAL_APP = 1;
    public static final int OTHER_DISPLAY_STARTED_APP = 0;
    public static final String PC_CAST_MODE_IN_SERVER = "hw.pc.cast.mode";
    private static int PC_DISPLAYID = -1;
    private static boolean PC_MODE = false;
    private static volatile boolean PC_MODE_EARLY = false;
    private static int PHONE_DISPLAYID = -1;
    public static final String PHONE_PROCESS = "com.android.phone";
    public static final String PKG_ARTICLE_NEWS = "com.ss.android.article.news";
    public static final String PKG_BLUETOOTH = "com.android.bluetooth";
    public static final String PKG_CHINA_CCB = "com.chinamworld.main";
    public static final String PKG_DESKTOP_EXPLORER = "com.huawei.desktop.explorer";
    public static final String PKG_DESKTOP_INSTRUCTION = "com.huawei.filemanager.desktopinstruction.EasyProjection";
    public static final String PKG_DESKTOP_SYSTEMUI = "com.huawei.desktop.systemui";
    public static final String PKG_HIMOVIE = "com.huawei.himovie";
    public static final String PKG_HIMOVIE_OVERSEAS = "com.huawei.himovie.overseas";
    public static final String PKG_HPPLAY_HAPPYCAST = "com.hpplay.happycast";
    public static final String PKG_HWPAY = "com.huawei.android.hwpay";
    public static final String PKG_PHONE_CONTACTS = "com.android.contacts";
    public static final String PKG_PHONE_CONTACTS_2 = "com.android.incallui";
    public static final String PKG_PHONE_PROJECTMENU = "com.huawei.android.projectmenu";
    public static final String PKG_PHONE_SYSTEMUI = "com.android.systemui";
    public static final String PKG_PHONE_WFDFT = "com.huawei.android.wfdft";
    public static final String PKG_SCREEN_RECORDER = "com.huawei.screenrecorder";
    public static final String PKG_SETTINGS = "com.android.settings";
    public static final String PKG_TALK_BACK = "com.google.android.marvin.talkback";
    public static final String PKG_WELINK = "com.huawei.works";
    public static final String PROCESS_DESKTOP_INSTRUCTION = "com.huawei.filemanager.desktopinstruction";
    public static final String PROP_PC_DISPLAY_HEIGHT = "hw.pc.display.height";
    public static final String PROP_PC_DISPLAY_MODE = "hw.pc.display.mode";
    public static final String PROP_PC_DISPLAY_WIDTH = "hw.pc.display.width";
    public static final String REASON_RELAUNCH_IME = "relaunchIME";
    public static final String REASON_RELAUNCH_IN_DIFF_DISPLAY = "relaunch due to in diff display";
    public static final int REPORT_CANCEL_CLOSE_BLUETOOTH = 10034;
    public static final int REPORT_CANCEL_OPEN_BLUETOOTH = 10037;
    public static final int REPORT_CHECKBOX_CHECKED_FOR_CLOSE_BLUETOOTH = 10032;
    public static final int REPORT_CHECKBOX_CHECKED_FOR_OPEN_BLUETOOTH = 10035;
    public static final int REPORT_CHECK_PROJECTION_SWITCH = 10049;
    public static final int REPORT_CLICK_HOME = 10000;
    public static final int REPORT_CLICK_KEYBOARD = 10006;
    public static final int REPORT_CLICK_MUTETILE = 10007;
    public static final int REPORT_CLICK_NOTIFY_SWITCH = 10003;
    public static final int REPORT_CLICK_SEARCH = 10012;
    public static final int REPORT_CLOSE_BLUETOOTH = 10033;
    public static final int REPORT_CONNECT_DISPLAY_BY_WIRED = 10008;
    public static final int REPORT_CONNECT_DISPLAY_BY_WIRELESS = 10057;
    public static final int REPORT_CONNECT_DISPLAY_RATIO = 10011;
    public static final int REPORT_CONNECT_DISPLAY_SIZE = 10010;
    public static final int REPORT_CONNECT_DISPLAY_TYPE = 10009;
    public static final int REPORT_CONNECT_KEYBOARD = 10005;
    public static final int REPORT_CONNECT_MOUSE = 10004;
    public static final int REPORT_CONNECT_PAD_KEYBORAD = 10027;
    public static final int REPORT_CURRENT_TASK_SIZE = 10017;
    public static final int REPORT_EASY_PROJECTION_CONNECT = 10052;
    public static final int REPORT_EASY_PROJECTION_DESKTOP_MODE = 10050;
    public static final int REPORT_EASY_PROJECTION_DISCONNECT = 10053;
    public static final int REPORT_EASY_PROJECTION_PHONE_MODE = 10051;
    public static final int REPORT_EASY_PROJECTION_SEARCH = 10054;
    public static final int REPORT_EASY_PROJECTION_STOP_SEARCH = 10055;
    public static final int REPORT_ENTER_HELP_PAGE = 10019;
    public static final int REPORT_ENTER_HELP_PAGE_CONNECT = 10020;
    public static final int REPORT_ENTER_HELP_PAGE_GUIDE = 10021;
    public static final int REPORT_ENTER_HELP_PAGE_WIRELESS_CONNECT = 10056;
    public static final int REPORT_ENTER_MY_COMPUTER = 10022;
    public static final int REPORT_ENTER_PROJECTION_FROM_HPPCAST = 10061;
    public static final int REPORT_LASE_HELP = 10043;
    public static final int REPORT_LASE_NOT_PROMPT_AGAIN = 10041;
    public static final int REPORT_LASE_PEN_BLUE = 10040;
    public static final int REPORT_LASE_PEN_ERASE_PEN = 10031;
    public static final int REPORT_LASE_PEN_RED = 10038;
    public static final int REPORT_LASE_PEN_SELECT_LASERS = 10029;
    public static final int REPORT_LASE_PEN_SELECT_MOUSE = 10028;
    public static final int REPORT_LASE_PEN_START_DRAW = 10030;
    public static final int REPORT_LASE_PEN_YELLOW = 10039;
    public static final int REPORT_LASE_PROMPT_KNOWN = 10042;
    public static final int REPORT_LASE_SCREENSHOW = 10044;
    public static final int REPORT_NOTIFICATION_DISCONNECT = 10048;
    public static final int REPORT_NOTIFICATION_OPEN_MK = 10047;
    public static final int REPORT_OPEN_BLUETOOTH = 10036;
    public static final int REPORT_OPEN_EASY_PROJECTION = 10045;
    public static final int REPORT_OPEN_HELP_INFORMATION = 10046;
    public static final int REPORT_PAD_DESKTOP_MODE = 10026;
    public static final int REPORT_PINED_APP = 10013;
    public static final int REPORT_SKIP_GUIDE = 10018;
    public static final int REPORT_START_APP_AT_PC = 10015;
    public static final int REPORT_START_APP_FROM_DOCK = 10014;
    public static final int REPORT_STOP_APP_AT_PC = 10016;
    public static final int REPORT_TIME_DIFF_SRC = 10002;
    public static final int REPORT_TIME_SAME_SRC = 10001;
    public static final int REPORT_TOUCHPAD_ON_CREATE = 10024;
    public static final int REPORT_TOUCHPAD_ON_STOP = 10025;
    public static final int REPORT_TOUCHPAD_SHOW_INPUT = 10023;
    public static final int REPORT_VASSIST_LAUNCH_VASSIST = 10060;
    public static final int REPORT_VASSIST_OP_TYPE = 10059;
    public static final int REPORT_VASSIST_STARTED_APP = 10058;
    public static final float SCALE_PC_SCREEN_MINOR = 0.95f;
    public static final float SCALE_PC_SCREEN_NORMAL = 1.0f;
    public static final float SCALE_PC_SCREEN_SMALLER = 0.9f;
    public static final int START_SUCCESS_BUT_STARTED_DEFAULT_DISPLAY = 98;
    public static final int START_SUCCESS_BUT_STARTED_OTHER_DISPLAY = 99;
    private static final String TAG = "HwPCUtils#";
    public static final int TYPE_LAUNCHER_LIKE = 2103;
    public static final int TYPE_LIGHT_DRAW = 2104;
    public static final int TYPE_PC_SCREEN_MINOR = 1;
    public static final int TYPE_PC_SCREEN_NORMAL = 0;
    public static final int TYPE_PC_SCREEN_SMALLER = 2;
    private static final boolean WIRELESS_PROJECTION_ENABLED = SystemProperties.getBoolean("ro.config.hw_emui_wfd_pc_mode", false);
    private static final boolean WIRELESS_PROJECTION_OPTIMIZE_ENABLED = SystemProperties.getBoolean("ro.config.hw_wfd_optimize", false);
    private static boolean mSupportOverlay = SystemProperties.getBoolean("hw_pc_support_overlay", false);
    private static boolean sIsFactoryOrMmi = false;
    private static ArrayList<InputMethodInfo> sMethodList = new ArrayList();
    public static ArrayList<String> sPackagesCanStartedInPCMode = new ArrayList();

    public enum ProjectionMode {
        DESKTOP_MODE,
        PHONE_MODE
    }

    static {
        sPackagesCanStartedInPCMode.add("com.amazon.avod.thirdpartyclient");
    }

    public static void setFactoryOrMmiState(boolean state) {
        sIsFactoryOrMmi = state;
    }

    public static final boolean enabled() {
        if (sIsFactoryOrMmi) {
            return false;
        }
        return ENABLED;
    }

    public static final boolean enabledInPad() {
        return ENABLED_IN_PAD && enabled();
    }

    public static final boolean isWirelessProjectionEnabled() {
        return WIRELESS_PROJECTION_OPTIMIZE_ENABLED && WIRELESS_PROJECTION_ENABLED;
    }

    public static final boolean isPcCastModeInServer() {
        return PC_MODE && enabled();
    }

    public static void setPcCastModeInServer(boolean enabled) {
        PC_MODE = enabled;
        String str = PC_CAST_MODE_IN_SERVER;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("");
        stringBuilder.append(enabled);
        SystemProperties.set(str, stringBuilder.toString());
    }

    public static final boolean isPcCastModeInServerEarly() {
        return enabled() && PC_MODE_EARLY;
    }

    public static final void setPcCastModeInServerEarly(ProjectionMode mode) {
        PC_MODE_EARLY = ProjectionMode.DESKTOP_MODE == mode;
    }

    public static void setPCDisplayID(int displayId) {
        PC_DISPLAYID = displayId;
    }

    public static int getPCDisplayID() {
        return PC_DISPLAYID;
    }

    public static void setIsWifiMode(boolean isWifiMode) {
        IS_WIFI_PC_MODE = isWifiMode;
    }

    public static boolean getIsWifiMode() {
        return IS_WIFI_PC_MODE;
    }

    public static void setPhoneDisplayID(int displayId) {
        PHONE_DISPLAYID = displayId;
    }

    public static int getPhoneDisplayID() {
        return PHONE_DISPLAYID;
    }

    public static final boolean isValidExtDisplayId(int displayId) {
        return enabled() && displayId == PC_DISPLAYID && displayId != -1 && displayId != 0;
    }

    public static final boolean isValidExtDisplayId(Context context) {
        if (!enabled() || context == null) {
            return false;
        }
        int count = 0;
        if (!((context instanceof ContextWrapper) || (context instanceof ContextImpl))) {
            context = context.getApplicationContext();
            if (context == null) {
                return false;
            }
        }
        while (context instanceof ContextWrapper) {
            Context baseContext = ((ContextWrapper) context).getBaseContext();
            Context nextContext = baseContext;
            if (baseContext != null && context != nextContext) {
                context = nextContext;
                count++;
                if (count > 12) {
                    break;
                }
            }
            break;
        }
        if (context instanceof ContextImpl) {
            return isValidExtDisplayId(((ContextImpl) context).peekHwPCDisplayId());
        }
        return false;
    }

    public static boolean isPcDynamicStack(int stackId) {
        return enabled() && stackId >= FIRST_PC_DYNAMIC_STACK_ID && stackId < HwVRUtils.VR_DYNAMIC_STACK_ID;
    }

    public static boolean isExtDynamicStack(int stackId) {
        return isPcDynamicStack(stackId) || HwVRUtils.isVRDynamicStack(stackId);
    }

    public static boolean isPcCastMode() {
        boolean z = false;
        try {
            HwPCManager hwPcManager = HwFrameworkFactory.getHwPCManager();
            if (hwPcManager != null) {
                IHwPCManager mService = hwPcManager.getService();
                if (mService != null && mService.getCastMode()) {
                    z = true;
                }
                return z;
            }
        } catch (RemoteException e) {
            Log.e("HwPCUtils", "call isPcCastMode error");
        }
        return false;
    }

    public static IHwPCManager getHwPCManager() {
        try {
            HwPCManager hwPcManager = HwFrameworkFactory.getHwPCManager();
            if (hwPcManager != null) {
                return hwPcManager.getService();
            }
            return null;
        } catch (Exception e) {
            Log.e("HwPCUtils", "getHwPCManager error");
            return null;
        }
    }

    private static boolean supportHwPCCastFeature(Display display) {
        boolean z = false;
        if (display == null) {
            return false;
        }
        int type = display.getType();
        if (type == 5 && PKG_HPPLAY_HAPPYCAST.equals(display.getOwnerPackageName())) {
            return true;
        }
        if (type == 5 && PKG_WELINK.equals(display.getOwnerPackageName())) {
            return true;
        }
        if (isWirelessProjectionEnabled()) {
            if (type == 2 || type == 3 || ((type == 5 || type == 4) && mSupportOverlay)) {
                z = true;
            }
            return z;
        }
        if (type == 2 || ((type == 5 || type == 4) && mSupportOverlay)) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:18:0x0049, code skipped:
            return r8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Bundle hookStartActivityOptions(Context context, Bundle options) {
        if (!enabled()) {
            return options;
        }
        Display display = context.getDisplay();
        int validDiplayid = HwVRUtils.getVRDisplayID(context);
        if (validDiplayid == -1) {
            if (display == null || !supportHwPCCastFeature(display) || display.getDisplayId() == -1 || display.getDisplayId() == 0) {
                return options;
            }
            DisplayInfo disInfo = new DisplayInfo();
            display.getDisplayInfo(disInfo);
            if (HwVRUtils.isVRDisplay(display.getDisplayId(), disInfo.getNaturalWidth(), disInfo.getNaturalHeight())) {
                return options;
            }
            validDiplayid = display.getDisplayId();
        }
        if (enabledInPad() && PKG_DESKTOP_EXPLORER.equals(context.getPackageName()) && !isPcCastMode()) {
            log(TAG, "hookStartActivityOptions not in PC mode and return");
            return options;
        }
        ActivityOptions activityOptions = ActivityOptions.fromBundle(options);
        if (activityOptions == null) {
            activityOptions = ActivityOptions.makeBasic();
        }
        if (activityOptions.getLaunchDisplayId() == -1) {
            activityOptions.setLaunchDisplayId(validDiplayid);
        }
        if (isValidExtDisplayId(validDiplayid)) {
            activityOptions.setLaunchWindowingMode(10);
        }
        return activityOptions.toBundle();
    }

    public static int log(String subTag, String msg) {
        if (!LOG_ENABLED) {
            return 0;
        }
        String tag = TAG;
        if (!TextUtils.isEmpty(subTag)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(tag);
            stringBuilder.append(subTag);
            tag = stringBuilder.toString();
        }
        return Log.i(tag, msg);
    }

    public static Context getDisplayContext(Context context, int displayId) {
        Display targetDisplay = ((DisplayManager) context.getSystemService("display")).getDisplay(displayId);
        if (targetDisplay == null) {
            return null;
        }
        return context.createDisplayContext(targetDisplay);
    }

    public static void bdReport(Context context, int eventID, String eventMsg) {
        HwFrameworkFactory.getHwFlogManager().bdReport(context, eventID, eventMsg);
    }

    public static void showImeStatusIcon(int iconResId, String pkgName) {
        String PCTAG = "HwPCUtils";
        try {
            Log.i(PCTAG, "PCU showImeStatusIcon start");
            IHwPCManager pcManager = getHwPCManager();
            if (pcManager != null) {
                pcManager.showImeStatusIcon(iconResId, pkgName);
            }
            Log.i(PCTAG, "PCU showImeStatusIcon end");
        } catch (RemoteException e) {
            Log.e(PCTAG, "call showImeStatusIcon error");
        }
    }

    public static void hideImeStatusIcon(String pkgName) {
        String PCTAG = "HwPCUtils";
        try {
            Log.i(PCTAG, "PCU hideImeStatusIcon start");
            IHwPCManager pcManager = getHwPCManager();
            if (pcManager != null) {
                pcManager.hideImeStatusIcon(pkgName);
            }
            Log.i(PCTAG, "PCU hideImeStatusIcon end");
        } catch (RemoteException e) {
            Log.e(PCTAG, "call hideImeStatusIcon error");
        }
    }

    public static List<String> getMutiResumeAppList() {
        try {
            IHwPCManager pcManager = getHwPCManager();
            if (pcManager != null) {
                return pcManager.getMutiResumeAppList();
            }
        } catch (RemoteException e) {
            log(TAG, "call getMutiResumeAppList error");
        }
        return null;
    }

    public static void setInputMethodList(ArrayList<InputMethodInfo> methodList) {
        sMethodList = methodList;
    }

    public static ArrayList<InputMethodInfo> getInputMethodList() {
        return sMethodList;
    }
}
