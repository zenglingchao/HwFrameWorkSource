package android.hardware.camera2.params;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.legacy.LegacyCameraDevice;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.hardware.camera2.utils.SurfaceUtils;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.renderscript.Allocation;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.util.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public final class StreamConfigurationMap {
    private static final long DURATION_20FPS_NS = 50000000;
    private static final int DURATION_MIN_FRAME = 0;
    private static final int DURATION_STALL = 1;
    private static final int HAL_DATASPACE_DEPTH = 4096;
    private static final int HAL_DATASPACE_RANGE_SHIFT = 27;
    private static final int HAL_DATASPACE_STANDARD_SHIFT = 16;
    private static final int HAL_DATASPACE_TRANSFER_SHIFT = 22;
    private static final int HAL_DATASPACE_UNKNOWN = 0;
    private static final int HAL_DATASPACE_V0_JFIF = 146931712;
    private static final int HAL_PIXEL_FORMAT_BLOB = 33;
    private static final int HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED = 34;
    private static final int HAL_PIXEL_FORMAT_RAW10 = 37;
    private static final int HAL_PIXEL_FORMAT_RAW12 = 38;
    private static final int HAL_PIXEL_FORMAT_RAW16 = 32;
    private static final int HAL_PIXEL_FORMAT_RAW_OPAQUE = 36;
    private static final int HAL_PIXEL_FORMAT_Y16 = 540422489;
    private static final int HAL_PIXEL_FORMAT_YCbCr_420_888 = 35;
    private static final String TAG = "StreamConfigurationMap";
    private final SparseIntArray mAllOutputFormats = new SparseIntArray();
    private final StreamConfiguration[] mConfigurations;
    private final StreamConfiguration[] mDepthConfigurations;
    private final StreamConfigurationDuration[] mDepthMinFrameDurations;
    private final SparseIntArray mDepthOutputFormats = new SparseIntArray();
    private final StreamConfigurationDuration[] mDepthStallDurations;
    private final SparseIntArray mHighResOutputFormats = new SparseIntArray();
    private final HighSpeedVideoConfiguration[] mHighSpeedVideoConfigurations;
    private final HashMap<Range<Integer>, Integer> mHighSpeedVideoFpsRangeMap = new HashMap();
    private final HashMap<Size, Integer> mHighSpeedVideoSizeMap = new HashMap();
    private final SparseIntArray mInputFormats = new SparseIntArray();
    private final ReprocessFormatsMap mInputOutputFormatsMap;
    private final boolean mListHighResolution;
    private final StreamConfigurationDuration[] mMinFrameDurations;
    private final SparseIntArray mOutputFormats = new SparseIntArray();
    private final StreamConfigurationDuration[] mStallDurations;

    public StreamConfigurationMap(StreamConfiguration[] configurations, StreamConfigurationDuration[] minFrameDurations, StreamConfigurationDuration[] stallDurations, StreamConfiguration[] depthConfigurations, StreamConfigurationDuration[] depthMinFrameDurations, StreamConfigurationDuration[] depthStallDurations, HighSpeedVideoConfiguration[] highSpeedVideoConfigurations, ReprocessFormatsMap inputOutputFormatsMap, boolean listHighResolution) {
        int length;
        int i;
        StreamConfiguration[] streamConfigurationArr = configurations;
        StreamConfiguration[] streamConfigurationArr2 = depthConfigurations;
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurationArr = highSpeedVideoConfigurations;
        if (streamConfigurationArr == null) {
            Preconditions.checkArrayElementsNotNull(streamConfigurationArr2, "depthConfigurations");
            this.mConfigurations = new StreamConfiguration[0];
            this.mMinFrameDurations = new StreamConfigurationDuration[0];
            this.mStallDurations = new StreamConfigurationDuration[0];
            StreamConfigurationDuration[] streamConfigurationDurationArr = minFrameDurations;
            StreamConfigurationDuration[] streamConfigurationDurationArr2 = stallDurations;
        } else {
            this.mConfigurations = (StreamConfiguration[]) Preconditions.checkArrayElementsNotNull(streamConfigurationArr, "configurations");
            this.mMinFrameDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(minFrameDurations, "minFrameDurations");
            this.mStallDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(stallDurations, "stallDurations");
        }
        this.mListHighResolution = listHighResolution;
        if (streamConfigurationArr2 == null) {
            this.mDepthConfigurations = new StreamConfiguration[0];
            this.mDepthMinFrameDurations = new StreamConfigurationDuration[0];
            this.mDepthStallDurations = new StreamConfigurationDuration[0];
            StreamConfigurationDuration[] streamConfigurationDurationArr3 = depthMinFrameDurations;
            StreamConfigurationDuration[] streamConfigurationDurationArr4 = depthStallDurations;
        } else {
            this.mDepthConfigurations = (StreamConfiguration[]) Preconditions.checkArrayElementsNotNull(streamConfigurationArr2, "depthConfigurations");
            this.mDepthMinFrameDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(depthMinFrameDurations, "depthMinFrameDurations");
            this.mDepthStallDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(depthStallDurations, "depthStallDurations");
        }
        if (highSpeedVideoConfigurationArr == null) {
            this.mHighSpeedVideoConfigurations = new HighSpeedVideoConfiguration[0];
        } else {
            this.mHighSpeedVideoConfigurations = (HighSpeedVideoConfiguration[]) Preconditions.checkArrayElementsNotNull(highSpeedVideoConfigurationArr, "highSpeedVideoConfigurations");
        }
        StreamConfiguration[] streamConfigurationArr3 = this.mConfigurations;
        int length2 = streamConfigurationArr3.length;
        int i2 = 0;
        while (i2 < length2) {
            boolean z;
            SparseIntArray map;
            StreamConfiguration config = streamConfigurationArr3[i2];
            int fmt = config.getFormat();
            if (config.isOutput()) {
                this.mAllOutputFormats.put(fmt, this.mAllOutputFormats.get(fmt) + 1);
                long duration = 0;
                if (this.mListHighResolution) {
                    StreamConfigurationDuration[] streamConfigurationDurationArr5 = this.mMinFrameDurations;
                    length = streamConfigurationDurationArr5.length;
                    i = 0;
                    while (i < length) {
                        int i3 = length;
                        StreamConfigurationDuration configurationDuration = streamConfigurationDurationArr5[i];
                        StreamConfigurationDuration[] streamConfigurationDurationArr6 = streamConfigurationDurationArr5;
                        if (configurationDuration.getFormat() == fmt && configurationDuration.getWidth() == config.getSize().getWidth() && configurationDuration.getHeight() == config.getSize().getHeight()) {
                            duration = configurationDuration.getDuration();
                            break;
                        }
                        i++;
                        length = i3;
                        streamConfigurationDurationArr5 = streamConfigurationDurationArr6;
                        z = listHighResolution;
                    }
                }
                map = duration <= DURATION_20FPS_NS ? this.mOutputFormats : this.mHighResOutputFormats;
            } else {
                map = this.mInputFormats;
            }
            map.put(fmt, map.get(fmt) + 1);
            i2++;
            streamConfigurationArr2 = depthConfigurations;
            highSpeedVideoConfigurationArr = highSpeedVideoConfigurations;
            z = listHighResolution;
        }
        for (StreamConfiguration config2 : this.mDepthConfigurations) {
            if (config2.isOutput()) {
                this.mDepthOutputFormats.put(config2.getFormat(), this.mDepthOutputFormats.get(config2.getFormat()) + 1);
            }
        }
        if (streamConfigurationArr == null || this.mOutputFormats.indexOfKey(34) >= 0) {
            for (HighSpeedVideoConfiguration config3 : this.mHighSpeedVideoConfigurations) {
                Size size = config3.getSize();
                Range<Integer> fpsRange = config3.getFpsRange();
                Integer fpsRangeCount = (Integer) this.mHighSpeedVideoSizeMap.get(size);
                if (fpsRangeCount == null) {
                    fpsRangeCount = Integer.valueOf(0);
                }
                this.mHighSpeedVideoSizeMap.put(size, Integer.valueOf(fpsRangeCount.intValue() + 1));
                Integer sizeCount = (Integer) this.mHighSpeedVideoFpsRangeMap.get(fpsRange);
                if (sizeCount == null) {
                    sizeCount = Integer.valueOf(0);
                }
                this.mHighSpeedVideoFpsRangeMap.put(fpsRange, Integer.valueOf(sizeCount.intValue() + 1));
            }
            this.mInputOutputFormatsMap = inputOutputFormatsMap;
            return;
        }
        throw new AssertionError("At least one stream configuration for IMPLEMENTATION_DEFINED must exist");
    }

    public final int[] getOutputFormats() {
        return getPublicFormats(true);
    }

    public final int[] getValidOutputFormatsForInput(int inputFormat) {
        if (this.mInputOutputFormatsMap == null) {
            return new int[0];
        }
        return this.mInputOutputFormatsMap.getOutputs(inputFormat);
    }

    public final int[] getInputFormats() {
        return getPublicFormats(false);
    }

    public Size[] getInputSizes(int format) {
        return getPublicFormatSizes(format, false, false);
    }

    public boolean isOutputSupportedFor(int format) {
        checkArgumentFormat(format);
        int internalFormat = imageFormatToInternal(format);
        boolean z = false;
        if (imageFormatToDataspace(format) == 4096) {
            if (this.mDepthOutputFormats.indexOfKey(internalFormat) >= 0) {
                z = true;
            }
            return z;
        }
        if (getFormatsMap(true).indexOfKey(internalFormat) >= 0) {
            z = true;
        }
        return z;
    }

    public static <T> boolean isOutputSupportedFor(Class<T> klass) {
        Preconditions.checkNotNull(klass, "klass must not be null");
        if (klass == ImageReader.class || klass == MediaRecorder.class || klass == MediaCodec.class || klass == Allocation.class || klass == SurfaceHolder.class || klass == SurfaceTexture.class) {
            return true;
        }
        return false;
    }

    public boolean isOutputSupportedFor(Surface surface) {
        Preconditions.checkNotNull(surface, "surface must not be null");
        Size surfaceSize = SurfaceUtils.getSurfaceSize(surface);
        int surfaceFormat = SurfaceUtils.getSurfaceFormat(surface);
        int surfaceDataspace = SurfaceUtils.getSurfaceDataspace(surface);
        boolean isFlexible = SurfaceUtils.isFlexibleConsumer(surface);
        for (StreamConfiguration config : surfaceDataspace != 4096 ? this.mConfigurations : this.mDepthConfigurations) {
            if (config.getFormat() == surfaceFormat && config.isOutput()) {
                if (config.getSize().equals(surfaceSize)) {
                    return true;
                }
                if (isFlexible && config.getSize().getWidth() <= LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING) {
                    return true;
                }
            }
        }
        return false;
    }

    public <T> Size[] getOutputSizes(Class<T> klass) {
        if (isOutputSupportedFor((Class) klass)) {
            return getInternalFormatSizes(34, 0, true, false);
        }
        return null;
    }

    public Size[] getOutputSizes(int format) {
        return getPublicFormatSizes(format, true, false);
    }

    public Size[] getHighSpeedVideoSizes() {
        Set<Size> keySet = this.mHighSpeedVideoSizeMap.keySet();
        return (Size[]) keySet.toArray(new Size[keySet.size()]);
    }

    public Range<Integer>[] getHighSpeedVideoFpsRangesFor(Size size) {
        Integer fpsRangeCount = (Integer) this.mHighSpeedVideoSizeMap.get(size);
        int i = 0;
        if (fpsRangeCount == null || fpsRangeCount.intValue() == 0) {
            throw new IllegalArgumentException(String.format("Size %s does not support high speed video recording", new Object[]{size}));
        }
        Range<Integer>[] fpsRanges = new Range[fpsRangeCount.intValue()];
        int i2 = 0;
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurationArr = this.mHighSpeedVideoConfigurations;
        int length = highSpeedVideoConfigurationArr.length;
        while (i < length) {
            HighSpeedVideoConfiguration config = highSpeedVideoConfigurationArr[i];
            if (size.equals(config.getSize())) {
                int i3 = i2 + 1;
                fpsRanges[i2] = config.getFpsRange();
                i2 = i3;
            }
            i++;
        }
        return fpsRanges;
    }

    public Range<Integer>[] getHighSpeedVideoFpsRanges() {
        Set<Range<Integer>> keySet = this.mHighSpeedVideoFpsRangeMap.keySet();
        return (Range[]) keySet.toArray(new Range[keySet.size()]);
    }

    public Size[] getHighSpeedVideoSizesFor(Range<Integer> fpsRange) {
        Integer sizeCount = (Integer) this.mHighSpeedVideoFpsRangeMap.get(fpsRange);
        int i = 0;
        if (sizeCount == null || sizeCount.intValue() == 0) {
            throw new IllegalArgumentException(String.format("FpsRange %s does not support high speed video recording", new Object[]{fpsRange}));
        }
        Size[] sizes = new Size[sizeCount.intValue()];
        int i2 = 0;
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurationArr = this.mHighSpeedVideoConfigurations;
        int length = highSpeedVideoConfigurationArr.length;
        while (i < length) {
            HighSpeedVideoConfiguration config = highSpeedVideoConfigurationArr[i];
            if (fpsRange.equals(config.getFpsRange())) {
                int i3 = i2 + 1;
                sizes[i2] = config.getSize();
                i2 = i3;
            }
            i++;
        }
        return sizes;
    }

    public Size[] getHighResolutionOutputSizes(int format) {
        if (this.mListHighResolution) {
            return getPublicFormatSizes(format, true, true);
        }
        return null;
    }

    public long getOutputMinFrameDuration(int format, Size size) {
        Preconditions.checkNotNull(size, "size must not be null");
        checkArgumentFormatSupported(format, true);
        return getInternalFormatDuration(imageFormatToInternal(format), imageFormatToDataspace(format), size, 0);
    }

    public <T> long getOutputMinFrameDuration(Class<T> klass, Size size) {
        if (isOutputSupportedFor((Class) klass)) {
            return getInternalFormatDuration(34, 0, size, 0);
        }
        throw new IllegalArgumentException("klass was not supported");
    }

    public long getOutputStallDuration(int format, Size size) {
        checkArgumentFormatSupported(format, true);
        return getInternalFormatDuration(imageFormatToInternal(format), imageFormatToDataspace(format), size, 1);
    }

    public <T> long getOutputStallDuration(Class<T> klass, Size size) {
        if (isOutputSupportedFor((Class) klass)) {
            return getInternalFormatDuration(34, 0, size, 1);
        }
        throw new IllegalArgumentException("klass was not supported");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StreamConfigurationMap)) {
            return false;
        }
        StreamConfigurationMap other = (StreamConfigurationMap) obj;
        if (Arrays.equals(this.mConfigurations, other.mConfigurations) && Arrays.equals(this.mMinFrameDurations, other.mMinFrameDurations) && Arrays.equals(this.mStallDurations, other.mStallDurations) && Arrays.equals(this.mDepthConfigurations, other.mDepthConfigurations) && Arrays.equals(this.mHighSpeedVideoConfigurations, other.mHighSpeedVideoConfigurations)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCodeGeneric(this.mConfigurations, this.mMinFrameDurations, this.mStallDurations, this.mDepthConfigurations, this.mHighSpeedVideoConfigurations);
    }

    private int checkArgumentFormatSupported(int format, boolean output) {
        checkArgumentFormat(format);
        int internalFormat = imageFormatToInternal(format);
        int internalDataspace = imageFormatToDataspace(format);
        if (output) {
            if (internalDataspace == 4096) {
                if (this.mDepthOutputFormats.indexOfKey(internalFormat) >= 0) {
                    return format;
                }
            } else if (this.mAllOutputFormats.indexOfKey(internalFormat) >= 0) {
                return format;
            }
        } else if (this.mInputFormats.indexOfKey(internalFormat) >= 0) {
            return format;
        }
        throw new IllegalArgumentException(String.format("format %x is not supported by this stream configuration map", new Object[]{Integer.valueOf(format)}));
    }

    static int checkArgumentFormatInternal(int format) {
        if (format != 36) {
            if (format == 256) {
                throw new IllegalArgumentException("ImageFormat.JPEG is an unknown internal format");
            } else if (format != 540422489) {
                switch (format) {
                    case 33:
                    case 34:
                        break;
                    default:
                        return checkArgumentFormat(format);
                }
            }
        }
        return format;
    }

    static int checkArgumentFormat(int format) {
        if (ImageFormat.isPublicFormat(format) || PixelFormat.isPublicFormat(format)) {
            return format;
        }
        throw new IllegalArgumentException(String.format("format 0x%x was not defined in either ImageFormat or PixelFormat", new Object[]{Integer.valueOf(format)}));
    }

    static int imageFormatToPublic(int format) {
        if (format == 33) {
            return 256;
        }
        if (format != 256) {
            return format;
        }
        throw new IllegalArgumentException("ImageFormat.JPEG is an unknown internal format");
    }

    static int depthFormatToPublic(int format) {
        if (format == 256) {
            throw new IllegalArgumentException("ImageFormat.JPEG is an unknown internal format");
        } else if (format == 540422489) {
            return ImageFormat.DEPTH16;
        } else {
            switch (format) {
                case 32:
                    return 4098;
                case 33:
                    return 257;
                case 34:
                    throw new IllegalArgumentException("IMPLEMENTATION_DEFINED must not leak to public API");
                default:
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unknown DATASPACE_DEPTH format ");
                    stringBuilder.append(format);
                    throw new IllegalArgumentException(stringBuilder.toString());
            }
        }
    }

    static int[] imageFormatToPublic(int[] formats) {
        if (formats == null) {
            return null;
        }
        for (int i = 0; i < formats.length; i++) {
            formats[i] = imageFormatToPublic(formats[i]);
        }
        return formats;
    }

    static int imageFormatToInternal(int format) {
        if (format == 4098) {
            return 32;
        }
        if (format == ImageFormat.DEPTH16) {
            return 540422489;
        }
        switch (format) {
            case 256:
            case 257:
                return 33;
            default:
                return format;
        }
    }

    static int imageFormatToDataspace(int format) {
        if (!(format == 4098 || format == ImageFormat.DEPTH16)) {
            switch (format) {
                case 256:
                    return HAL_DATASPACE_V0_JFIF;
                case 257:
                    break;
                default:
                    return 0;
            }
        }
        return 4096;
    }

    public static int[] imageFormatToInternal(int[] formats) {
        if (formats == null) {
            return null;
        }
        for (int i = 0; i < formats.length; i++) {
            formats[i] = imageFormatToInternal(formats[i]);
        }
        return formats;
    }

    private Size[] getPublicFormatSizes(int format, boolean output, boolean highRes) {
        try {
            checkArgumentFormatSupported(format, output);
            return getInternalFormatSizes(imageFormatToInternal(format), imageFormatToDataspace(format), output, highRes);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Size[] getInternalFormatSizes(int format, int dataspace, boolean output, boolean highRes) {
        StreamConfigurationMap streamConfigurationMap = this;
        int i = format;
        int i2 = dataspace;
        boolean z = output;
        boolean z2 = highRes;
        Object obj = 4096;
        if (i2 == 4096 && z2) {
            return new Size[0];
        }
        SparseIntArray formatsMap;
        if (!z) {
            formatsMap = streamConfigurationMap.mInputFormats;
        } else if (i2 == 4096) {
            formatsMap = streamConfigurationMap.mDepthOutputFormats;
        } else if (z2) {
            formatsMap = streamConfigurationMap.mHighResOutputFormats;
        } else {
            formatsMap = streamConfigurationMap.mOutputFormats;
        }
        int sizesCount = formatsMap.get(i);
        if (((!z || i2 == 4096) && sizesCount == 0) || (z && i2 != 4096 && streamConfigurationMap.mAllOutputFormats.get(i) == 0)) {
            throw new IllegalArgumentException("format not available");
        }
        Size[] sizes = new Size[sizesCount];
        StreamConfiguration[] configurations = i2 == 4096 ? streamConfigurationMap.mDepthConfigurations : streamConfigurationMap.mConfigurations;
        StreamConfigurationDuration[] minFrameDurations = i2 == 4096 ? streamConfigurationMap.mDepthMinFrameDurations : streamConfigurationMap.mMinFrameDurations;
        int length = configurations.length;
        int sizeIndex = 0;
        int sizeIndex2 = 0;
        while (sizeIndex2 < length) {
            Object obj2;
            StreamConfiguration config = configurations[sizeIndex2];
            int fmt = config.getFormat();
            if (fmt != i) {
                obj2 = obj;
            } else if (config.isOutput() == z) {
                if (z && streamConfigurationMap.mListHighResolution) {
                    long duration = 0;
                    int i3 = 0;
                    while (i3 < minFrameDurations.length) {
                        StreamConfigurationDuration d = minFrameDurations[i3];
                        if (d.getFormat() == fmt && d.getWidth() == config.getSize().getWidth() && d.getHeight() == config.getSize().getHeight()) {
                            duration = d.getDuration();
                            break;
                        }
                        i3++;
                        i = format;
                        z = output;
                    }
                    obj2 = 4096;
                    if (i2 != 4096) {
                        if (z2 != (duration > DURATION_20FPS_NS)) {
                        }
                    }
                } else {
                    obj2 = 4096;
                }
                i = sizeIndex + 1;
                sizes[sizeIndex] = config.getSize();
                sizeIndex = i;
            } else {
                obj2 = 4096;
            }
            sizeIndex2++;
            obj = obj2;
            streamConfigurationMap = this;
            i = format;
            z = output;
        }
        if (sizeIndex == sizesCount) {
            return sizes;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Too few sizes (expected ");
        stringBuilder.append(sizesCount);
        stringBuilder.append(", actual ");
        stringBuilder.append(sizeIndex);
        stringBuilder.append(")");
        throw new AssertionError(stringBuilder.toString());
    }

    private int[] getPublicFormats(boolean output) {
        int[] formats = new int[getPublicFormatCount(output)];
        SparseIntArray map = getFormatsMap(output);
        int j = 0;
        int i = 0;
        int j2 = 0;
        while (j2 < map.size()) {
            int i2 = i + 1;
            formats[i] = imageFormatToPublic(map.keyAt(j2));
            j2++;
            i = i2;
        }
        if (output) {
            while (true) {
                j2 = j;
                if (j2 >= this.mDepthOutputFormats.size()) {
                    break;
                }
                j = i + 1;
                formats[i] = depthFormatToPublic(this.mDepthOutputFormats.keyAt(j2));
                i = j;
                j = j2 + 1;
            }
        }
        if (formats.length == i) {
            return formats;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Too few formats ");
        stringBuilder.append(i);
        stringBuilder.append(", expected ");
        stringBuilder.append(formats.length);
        throw new AssertionError(stringBuilder.toString());
    }

    private SparseIntArray getFormatsMap(boolean output) {
        return output ? this.mAllOutputFormats : this.mInputFormats;
    }

    private long getInternalFormatDuration(int format, int dataspace, Size size, int duration) {
        if (isSupportedInternalConfiguration(format, dataspace, size)) {
            for (StreamConfigurationDuration configurationDuration : getDurations(duration, dataspace)) {
                if (configurationDuration.getFormat() == format && configurationDuration.getWidth() == size.getWidth() && configurationDuration.getHeight() == size.getHeight()) {
                    return configurationDuration.getDuration();
                }
            }
            return 0;
        }
        throw new IllegalArgumentException("size was not supported");
    }

    private StreamConfigurationDuration[] getDurations(int duration, int dataspace) {
        switch (duration) {
            case 0:
                return dataspace == 4096 ? this.mDepthMinFrameDurations : this.mMinFrameDurations;
            case 1:
                return dataspace == 4096 ? this.mDepthStallDurations : this.mStallDurations;
            default:
                throw new IllegalArgumentException("duration was invalid");
        }
    }

    private int getPublicFormatCount(boolean output) {
        int size = getFormatsMap(output).size();
        if (output) {
            return size + this.mDepthOutputFormats.size();
        }
        return size;
    }

    private static <T> boolean arrayContains(T[] array, T element) {
        if (array == null) {
            return false;
        }
        for (T el : array) {
            if (Objects.equals(el, element)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSupportedInternalConfiguration(int format, int dataspace, Size size) {
        StreamConfiguration[] configurations = dataspace == 4096 ? this.mDepthConfigurations : this.mConfigurations;
        int i = 0;
        while (i < configurations.length) {
            if (configurations[i].getFormat() == format && configurations[i].getSize().equals(size)) {
                return true;
            }
            i++;
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("StreamConfiguration(");
        appendOutputsString(sb);
        sb.append(", ");
        appendHighResOutputsString(sb);
        sb.append(", ");
        appendInputsString(sb);
        sb.append(", ");
        appendValidOutputFormatsForInputString(sb);
        sb.append(", ");
        appendHighSpeedVideoConfigurationsString(sb);
        sb.append(")");
        return sb.toString();
    }

    private void appendOutputsString(StringBuilder sb) {
        StringBuilder stringBuilder = sb;
        stringBuilder.append("Outputs(");
        int[] formats = getOutputFormats();
        int length = formats.length;
        for (int i = 0; i < length; i++) {
            int format = formats[i];
            Size[] sizes = getOutputSizes(format);
            int length2 = sizes.length;
            int i2 = 0;
            while (i2 < length2) {
                Size size = sizes[i2];
                long minFrameDuration = getOutputMinFrameDuration(format, size);
                long stallDuration = getOutputStallDuration(format, size);
                r4 = new Object[6];
                int[] formats2 = formats;
                r4[0] = Integer.valueOf(size.getWidth());
                r4[1] = Integer.valueOf(size.getHeight());
                r4[2] = formatToString(format);
                r4[3] = Integer.valueOf(format);
                r4[4] = Long.valueOf(minFrameDuration);
                r4[5] = Long.valueOf(stallDuration);
                stringBuilder.append(String.format("[w:%d, h:%d, format:%s(%d), min_duration:%d, stall:%d], ", r4));
                i2++;
                formats = formats2;
            }
        }
        if (stringBuilder.charAt(sb.length() - 1) == ' ') {
            stringBuilder.delete(sb.length() - 2, sb.length());
        }
        stringBuilder.append(")");
    }

    private void appendHighResOutputsString(StringBuilder sb) {
        StringBuilder stringBuilder = sb;
        stringBuilder.append("HighResolutionOutputs(");
        int[] formats = getOutputFormats();
        int length = formats.length;
        int i = 0;
        while (i < length) {
            int format = formats[i];
            Size[] sizes = getHighResolutionOutputSizes(format);
            if (sizes != null) {
                int length2 = sizes.length;
                int i2 = 0;
                while (i2 < length2) {
                    Size size = sizes[i2];
                    long minFrameDuration = getOutputMinFrameDuration(format, size);
                    long stallDuration = getOutputStallDuration(format, size);
                    r4 = new Object[6];
                    int[] formats2 = formats;
                    r4[0] = Integer.valueOf(size.getWidth());
                    r4[1] = Integer.valueOf(size.getHeight());
                    r4[2] = formatToString(format);
                    r4[3] = Integer.valueOf(format);
                    r4[4] = Long.valueOf(minFrameDuration);
                    r4[5] = Long.valueOf(stallDuration);
                    stringBuilder.append(String.format("[w:%d, h:%d, format:%s(%d), min_duration:%d, stall:%d], ", r4));
                    i2++;
                    formats = formats2;
                }
            }
            i++;
            formats = formats;
        }
        if (stringBuilder.charAt(sb.length() - 1) == ' ') {
            stringBuilder.delete(sb.length() - 2, sb.length());
        }
        stringBuilder.append(")");
    }

    private void appendInputsString(StringBuilder sb) {
        StringBuilder stringBuilder = sb;
        stringBuilder.append("Inputs(");
        for (int format : getInputFormats()) {
            for (Size size : getInputSizes(format)) {
                stringBuilder.append(String.format("[w:%d, h:%d, format:%s(%d)], ", new Object[]{Integer.valueOf(size.getWidth()), Integer.valueOf(size.getHeight()), formatToString(format), Integer.valueOf(format)}));
            }
        }
        if (stringBuilder.charAt(sb.length() - 1) == ' ') {
            stringBuilder.delete(sb.length() - 2, sb.length());
        }
        stringBuilder.append(")");
    }

    private void appendValidOutputFormatsForInputString(StringBuilder sb) {
        sb.append("ValidOutputFormatsForInput(");
        for (int inputFormat : getInputFormats()) {
            sb.append(String.format("[in:%s(%d), out:", new Object[]{formatToString(inputFormat), Integer.valueOf(inputFormats[r3])}));
            int[] outputFormats = getValidOutputFormatsForInput(inputFormat);
            for (int i = 0; i < outputFormats.length; i++) {
                sb.append(String.format("%s(%d)", new Object[]{formatToString(outputFormats[i]), Integer.valueOf(outputFormats[i])}));
                if (i < outputFormats.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("], ");
        }
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(")");
    }

    private void appendHighSpeedVideoConfigurationsString(StringBuilder sb) {
        StringBuilder stringBuilder = sb;
        stringBuilder.append("HighSpeedVideoConfigurations(");
        for (Size size : getHighSpeedVideoSizes()) {
            for (Range<Integer> range : getHighSpeedVideoFpsRangesFor(size)) {
                stringBuilder.append(String.format("[w:%d, h:%d, min_fps:%d, max_fps:%d], ", new Object[]{Integer.valueOf(size.getWidth()), Integer.valueOf(size.getHeight()), range.getLower(), range.getUpper()}));
            }
        }
        if (stringBuilder.charAt(sb.length() - 1) == ' ') {
            stringBuilder.delete(sb.length() - 2, sb.length());
        }
        stringBuilder.append(")");
    }

    private String formatToString(int format) {
        switch (format) {
            case 1:
                return "RGBA_8888";
            case 2:
                return "RGBX_8888";
            case 3:
                return "RGB_888";
            case 4:
                return "RGB_565";
            case 16:
                return "NV16";
            case 17:
                return "NV21";
            case 20:
                return "YUY2";
            case 32:
                return "RAW_SENSOR";
            case 34:
                return "PRIVATE";
            case 35:
                return "YUV_420_888";
            case 36:
                return "RAW_PRIVATE";
            case 37:
                return "RAW10";
            case 256:
                return "JPEG";
            case 257:
                return "DEPTH_POINT_CLOUD";
            case 4098:
                return "RAW_DEPTH";
            case ImageFormat.Y8 /*538982489*/:
                return "Y8";
            case 540422489:
                return "Y16";
            case ImageFormat.YV12 /*842094169*/:
                return "YV12";
            case ImageFormat.DEPTH16 /*1144402265*/:
                return "DEPTH16";
            default:
                return "UNKNOWN";
        }
    }
}
