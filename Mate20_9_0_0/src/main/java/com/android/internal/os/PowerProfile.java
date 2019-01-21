package com.android.internal.os;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParserException;

public class PowerProfile {
    private static final String ATTR_NAME = "name";
    private static final String CPU_CLUSTER_POWER_COUNT = "cpu.cluster_power.cluster";
    private static final String CPU_CORE_POWER_PREFIX = "cpu.core_power.cluster";
    private static final String CPU_CORE_SPEED_PREFIX = "cpu.core_speeds.cluster";
    private static final String CPU_PER_CLUSTER_CORE_COUNT = "cpu.clusters.cores";
    public static final String POWER_AMBIENT_DISPLAY = "ambient.on";
    public static final String POWER_AUDIO = "audio";
    public static final String POWER_BATTERY_CAPACITY = "battery.capacity";
    @Deprecated
    public static final String POWER_BLUETOOTH_ACTIVE = "bluetooth.active";
    @Deprecated
    public static final String POWER_BLUETOOTH_AT_CMD = "bluetooth.at";
    public static final String POWER_BLUETOOTH_CONTROLLER_IDLE = "bluetooth.controller.idle";
    public static final String POWER_BLUETOOTH_CONTROLLER_OPERATING_VOLTAGE = "bluetooth.controller.voltage";
    public static final String POWER_BLUETOOTH_CONTROLLER_RX = "bluetooth.controller.rx";
    public static final String POWER_BLUETOOTH_CONTROLLER_TX = "bluetooth.controller.tx";
    @Deprecated
    public static final String POWER_BLUETOOTH_ON = "bluetooth.on";
    public static final String POWER_CAMERA = "camera.avg";
    public static final String POWER_CPU_ACTIVE = "cpu.active";
    public static final String POWER_CPU_IDLE = "cpu.idle";
    public static final String POWER_CPU_SUSPEND = "cpu.suspend";
    public static final String POWER_FLASHLIGHT = "camera.flashlight";
    public static final String POWER_GPS_ON = "gps.on";
    public static final String POWER_GPS_OPERATING_VOLTAGE = "gps.voltage";
    public static final String POWER_GPS_SIGNAL_QUALITY_BASED = "gps.signalqualitybased";
    public static final String POWER_MEMORY = "memory.bandwidths";
    public static final String POWER_MODEM_CONTROLLER_IDLE = "modem.controller.idle";
    public static final String POWER_MODEM_CONTROLLER_OPERATING_VOLTAGE = "modem.controller.voltage";
    public static final String POWER_MODEM_CONTROLLER_RX = "modem.controller.rx";
    public static final String POWER_MODEM_CONTROLLER_SLEEP = "modem.controller.sleep";
    public static final String POWER_MODEM_CONTROLLER_TX = "modem.controller.tx";
    public static final String POWER_RADIO_ACTIVE = "radio.active";
    public static final String POWER_RADIO_ON = "radio.on";
    public static final String POWER_RADIO_SCANNING = "radio.scanning";
    public static final String POWER_SCREEN_FULL = "screen.full";
    public static final String POWER_SCREEN_ON = "screen.on";
    public static final String POWER_VIDEO = "video";
    public static final String POWER_WIFI_ACTIVE = "wifi.active";
    public static final String POWER_WIFI_BATCHED_SCAN = "wifi.batchedscan";
    public static final String POWER_WIFI_CONTROLLER_IDLE = "wifi.controller.idle";
    public static final String POWER_WIFI_CONTROLLER_OPERATING_VOLTAGE = "wifi.controller.voltage";
    public static final String POWER_WIFI_CONTROLLER_RX = "wifi.controller.rx";
    public static final String POWER_WIFI_CONTROLLER_TX = "wifi.controller.tx";
    public static final String POWER_WIFI_CONTROLLER_TX_LEVELS = "wifi.controller.tx_levels";
    public static final String POWER_WIFI_ON = "wifi.on";
    public static final String POWER_WIFI_SCAN = "wifi.scan";
    private static final String TAG = "PowerProfile";
    private static final String TAG_ARRAY = "array";
    private static final String TAG_ARRAYITEM = "value";
    private static final String TAG_DEVICE = "device";
    private static final String TAG_ITEM = "item";
    private static final Object sLock = new Object();
    static final HashMap<String, Double[]> sPowerArrayMap = new HashMap();
    static final HashMap<String, Double> sPowerItemMap = new HashMap();
    private CpuClusterKey[] mCpuClusters;

    public static class CpuClusterKey {
        private final String clusterPowerKey;
        private final String corePowerKey;
        private final String freqKey;
        private final int numCpus;

        private CpuClusterKey(String freqKey, String clusterPowerKey, String corePowerKey, int numCpus) {
            this.freqKey = freqKey;
            this.clusterPowerKey = clusterPowerKey;
            this.corePowerKey = corePowerKey;
            this.numCpus = numCpus;
        }
    }

    @VisibleForTesting
    public PowerProfile(Context context) {
        this(context, false);
    }

    @VisibleForTesting
    public PowerProfile(Context context, boolean forTest) {
        synchronized (sLock) {
            if (sPowerItemMap.size() == 0 && sPowerArrayMap.size() == 0 && !HwFrameworkFactory.getHwPowerProfileManager().readHwPowerValuesFromXml(sPowerItemMap, sPowerArrayMap)) {
                readPowerValuesFromXml(context, forTest);
            }
            initCpuClusters();
        }
    }

    private void readPowerValuesFromXml(Context context, boolean forTest) {
        int i;
        if (forTest) {
            i = 18284564;
        } else {
            i = 18284563;
        }
        int id = i;
        Resources resources = context.getResources();
        XmlResourceParser parser = resources.getXml(id);
        boolean parsingArray = false;
        ArrayList<Double> array = new ArrayList();
        String str = null;
        Object arrayName = null;
        try {
            String element;
            double d;
            XmlUtils.beginDocument(parser, TAG_DEVICE);
            while (true) {
                XmlUtils.nextElement(parser);
                element = parser.getName();
                d = 0.0d;
                if (element == null) {
                    break;
                }
                if (parsingArray) {
                    if (!element.equals(TAG_ARRAYITEM)) {
                        sPowerArrayMap.put(arrayName, (Double[]) array.toArray(new Double[array.size()]));
                        parsingArray = false;
                    }
                }
                if (element.equals(TAG_ARRAY)) {
                    parsingArray = true;
                    array.clear();
                    arrayName = parser.getAttributeValue(str, ATTR_NAME);
                } else if (element.equals(TAG_ITEM) || element.equals(TAG_ARRAYITEM)) {
                    String name = null;
                    if (!parsingArray) {
                        name = parser.getAttributeValue(str, ATTR_NAME);
                    }
                    if (parser.next() == 4) {
                        double value = 0.0d;
                        try {
                            value = Double.valueOf(parser.getText()).doubleValue();
                        } catch (NumberFormatException e) {
                            String str2 = TAG;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("NumberFormatException: value = ");
                            stringBuilder.append(0.0d);
                            Log.e(str2, stringBuilder.toString());
                        }
                        if (element.equals(TAG_ITEM)) {
                            sPowerItemMap.put(name, Double.valueOf(value));
                        } else if (parsingArray) {
                            array.add(Double.valueOf(value));
                        }
                    }
                }
                str = null;
            }
            if (parsingArray) {
                sPowerArrayMap.put(arrayName, (Double[]) array.toArray(new Double[array.size()]));
            }
            parser.close();
            int[] configResIds = new int[]{17694738, 17694743, 17694744, 17694742};
            String[] configResIdKeys = new String[]{POWER_BLUETOOTH_CONTROLLER_IDLE, POWER_BLUETOOTH_CONTROLLER_RX, POWER_BLUETOOTH_CONTROLLER_TX, POWER_BLUETOOTH_CONTROLLER_OPERATING_VOLTAGE};
            element = null;
            while (element < configResIds.length) {
                String key = configResIdKeys[element];
                if (!sPowerItemMap.containsKey(key) || ((Double) sPowerItemMap.get(key)).doubleValue() <= d) {
                    int value2 = resources.getInteger(configResIds[element]);
                    if (value2 > 0) {
                        sPowerItemMap.put(key, Double.valueOf((double) value2));
                    }
                }
                element++;
                d = 0.0d;
            }
        } catch (XmlPullParserException e2) {
            throw new RuntimeException(e2);
        } catch (IOException e3) {
            throw new RuntimeException(e3);
        } catch (Throwable th) {
            parser.close();
        }
    }

    private void initCpuClusters() {
        int cluster = 0;
        if (sPowerArrayMap.containsKey(CPU_PER_CLUSTER_CORE_COUNT)) {
            Double[] data = (Double[]) sPowerArrayMap.get(CPU_PER_CLUSTER_CORE_COUNT);
            this.mCpuClusters = new CpuClusterKey[data.length];
            while (cluster < data.length) {
                int numCpusInCluster = (int) Math.round(data[cluster].doubleValue());
                CpuClusterKey[] cpuClusterKeyArr = this.mCpuClusters;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(CPU_CORE_SPEED_PREFIX);
                stringBuilder.append(cluster);
                String stringBuilder2 = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                stringBuilder.append(CPU_CLUSTER_POWER_COUNT);
                stringBuilder.append(cluster);
                String stringBuilder3 = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                stringBuilder.append(CPU_CORE_POWER_PREFIX);
                stringBuilder.append(cluster);
                cpuClusterKeyArr[cluster] = new CpuClusterKey(stringBuilder2, stringBuilder3, stringBuilder.toString(), numCpusInCluster);
                cluster++;
            }
            return;
        }
        this.mCpuClusters = new CpuClusterKey[1];
        int numCpus = 1;
        if (sPowerItemMap.containsKey(CPU_PER_CLUSTER_CORE_COUNT)) {
            numCpus = (int) Math.round(((Double) sPowerItemMap.get(CPU_PER_CLUSTER_CORE_COUNT)).doubleValue());
        }
        this.mCpuClusters[0] = new CpuClusterKey("cpu.core_speeds.cluster0", "cpu.cluster_power.cluster0", "cpu.core_power.cluster0", numCpus);
    }

    public int getNumCpuClusters() {
        return this.mCpuClusters.length;
    }

    public int getNumCoresInCpuCluster(int cluster) {
        return this.mCpuClusters[cluster].numCpus;
    }

    public int getNumSpeedStepsInCpuCluster(int cluster) {
        if (cluster < 0 || cluster >= this.mCpuClusters.length) {
            return 0;
        }
        if (sPowerArrayMap.containsKey(this.mCpuClusters[cluster].freqKey)) {
            return ((Double[]) sPowerArrayMap.get(this.mCpuClusters[cluster].freqKey)).length;
        }
        return 1;
    }

    public double getAveragePowerForCpuCluster(int cluster) {
        if (cluster < 0 || cluster >= this.mCpuClusters.length) {
            return 0.0d;
        }
        return getAveragePower(this.mCpuClusters[cluster].clusterPowerKey);
    }

    public double getAveragePowerForCpuCore(int cluster, int step) {
        if (cluster < 0 || cluster >= this.mCpuClusters.length) {
            return 0.0d;
        }
        return getAveragePower(this.mCpuClusters[cluster].corePowerKey, step);
    }

    public int getNumElements(String key) {
        if (sPowerItemMap.containsKey(key)) {
            return 1;
        }
        if (sPowerArrayMap.containsKey(key)) {
            return ((Double[]) sPowerArrayMap.get(key)).length;
        }
        return 0;
    }

    public double getAveragePowerOrDefault(String type, double defaultValue) {
        if (sPowerItemMap.containsKey(type)) {
            return ((Double) sPowerItemMap.get(type)).doubleValue();
        }
        if (sPowerArrayMap.containsKey(type)) {
            return ((Double[]) sPowerArrayMap.get(type))[0].doubleValue();
        }
        return defaultValue;
    }

    public double getAveragePower(String type) {
        return getAveragePowerOrDefault(type, 0.0d);
    }

    public double getAveragePower(String type, int level) {
        if (sPowerItemMap.containsKey(type)) {
            return ((Double) sPowerItemMap.get(type)).doubleValue();
        }
        if (!sPowerArrayMap.containsKey(type)) {
            return 0.0d;
        }
        Double[] values = (Double[]) sPowerArrayMap.get(type);
        if (values.length > level && level >= 0) {
            return values[level].doubleValue();
        }
        if (level < 0 || values.length == 0) {
            return 0.0d;
        }
        return values[values.length - 1].doubleValue();
    }

    public double getBatteryCapacity() {
        return getAveragePower(POWER_BATTERY_CAPACITY);
    }
}
